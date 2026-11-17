package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.BusinessAggregates
import com.example.data.api.BusinessContextBuilder
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.BusinessRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    LANDING,
    DASHBOARD,
    PRODUCTS,
    TRANSACTIONS,
    CHAT_ASSISTANT,
    SALES_ANALYTICS,
    MARKETING_GENERATOR,
    BUSINESS_REPORTS,
    SETTINGS
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val geminiClient = GeminiClient()
    val repository = BusinessRepository(database.appDao(), geminiClient, application)

    // Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Active Business ID
    private val _activeBusinessId = MutableStateFlow(repository.getActiveBusinessId())
    val activeBusinessId: StateFlow<String> = _activeBusinessId.asStateFlow()

    // Active Business Entity
    val activeBusiness: StateFlow<BusinessEntity?> = _activeBusinessId
        .flatMapLatest { id ->
            flow {
                emit(repository.getBusiness(id))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All Businesses
    val allBusinesses: StateFlow<List<BusinessEntity>> = repository.getAllBusinesses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Products of Active Business
    val products: StateFlow<List<ProductEntity>> = _activeBusinessId
        .flatMapLatest { id ->
            repository.getProducts(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions of Active Business
    val transactions: StateFlow<List<TransactionWithItems>> = _activeBusinessId
        .flatMapLatest { id ->
            repository.getTransactions(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat Messages
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _activeBusinessId
        .flatMapLatest { id ->
            repository.getChatMessages(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Generations
    val aiGenerations: StateFlow<List<AiGenerationEntity>> = _activeBusinessId
        .flatMapLatest { id ->
            repository.getAiGenerations(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Business Reports
    val businessReports: StateFlow<List<BusinessReportEntity>> = _activeBusinessId
        .flatMapLatest { id ->
            repository.getBusinessReports(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Thinking state
    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // UI Message / Toast Banner
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Dashboard Date Filter
    private val _dashboardPeriod = MutableStateFlow("ALL") // "ALL", "7_DAYS", "30_DAYS"
    val dashboardPeriod: StateFlow<String> = _dashboardPeriod.asStateFlow()

    fun setDashboardPeriod(period: String) {
        _dashboardPeriod.value = period
    }

    // Filtered Transactions for Dashboard & Analytics
    val filteredTransactions: StateFlow<List<TransactionWithItems>> = combine(
        transactions,
        dashboardPeriod
    ) { txList, period ->
        val now = System.currentTimeMillis()
        val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000

        when (period) {
            "7_DAYS" -> txList.filter { it.transaction.timestamp >= (now - sevenDaysMs) }
            "30_DAYS" -> txList.filter { it.transaction.timestamp >= (now - thirtyDaysMs) }
            else -> txList
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Computed Aggregates
    val aggregates: StateFlow<BusinessAggregates> = combine(
        products,
        filteredTransactions
    ) { prods, txs ->
        BusinessContextBuilder.computeAggregates(prods, txs)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        BusinessAggregates(0, 0, 0, emptyList(), emptyMap(), emptyList(), emptyList(), 0, 0)
    )

    init {
        // Load saved API key into GeminiClient
        val savedKey = repository.getSavedApiKey()
        if (savedKey.isNotBlank()) {
            geminiClient.setApiKey(savedKey)
        }

        // Check if database is empty; if so, automatically seed demo data so user has immediate rich data
        viewModelScope.launch {
            val businesses = repository.getAllBusinesses().first()
            if (businesses.isEmpty()) {
                val demoBiz = repository.seedDemoData()
                _activeBusinessId.value = demoBiz.id
            } else {
                val currentActive = repository.getActiveBusinessId()
                if (businesses.none { it.id == currentActive }) {
                    _activeBusinessId.value = businesses.first().id
                    repository.setActiveBusinessId(businesses.first().id)
                }
            }
        }
    }

    // --- Actions ---

    fun sendChatMessage(prompt: String) {
        if (prompt.isBlank() || _isAiThinking.value) return
        val bizId = _activeBusinessId.value
        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                repository.sendChatMessage(bizId, prompt)
            } catch (e: Exception) {
                _userMessage.value = "Gagal memproses AI: ${e.message}"
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun clearChat() {
        val bizId = _activeBusinessId.value
        viewModelScope.launch {
            repository.clearChat(bizId)
            _userMessage.value = "Riwayat chat telah dibersihkan."
        }
    }

    fun addProduct(
        name: String,
        category: String,
        price: Long,
        costPrice: Long,
        stock: Int,
        minStock: Int,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.addProduct(
                _activeBusinessId.value,
                name,
                category,
                price,
                costPrice,
                stock,
                minStock,
                description
            )
            if (res.isSuccess) {
                _userMessage.value = "Produk '${name}' berhasil ditambahkan."
                onSuccess()
            } else {
                _userMessage.value = "Gagal: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun updateProduct(product: ProductEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = repository.updateProduct(product)
            if (res.isSuccess) {
                _userMessage.value = "Produk berhasil diperbarui."
                onSuccess()
            } else {
                _userMessage.value = "Gagal: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            _userMessage.value = "Produk berhasil dihapus."
        }
    }

    fun recordSale(
        customerName: String,
        paymentMethod: String,
        notes: String,
        itemsToSell: List<Pair<ProductEntity, Int>>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.recordSale(
                _activeBusinessId.value,
                customerName,
                paymentMethod,
                notes,
                itemsToSell
            )
            if (res.isSuccess) {
                _userMessage.value = "Transaksi ${res.getOrNull()?.invoiceNumber} berhasil dicatat!"
                onSuccess()
            } else {
                _userMessage.value = "Gagal: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun deleteTransaction(txId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(txId)
            _userMessage.value = "Transaksi berhasil dihapus."
        }
    }

    fun importCsv(csvContent: String) {
        viewModelScope.launch {
            val (count, msg) = repository.importTransactionsCsv(_activeBusinessId.value, csvContent)
            _userMessage.value = msg
        }
    }

    suspend fun getCsvExport(): String {
        return repository.exportTransactionsToCsv(_activeBusinessId.value)
    }

    fun generateMarketing(
        productName: String,
        category: String,
        price: Long,
        targetAudience: String,
        usp: String,
        platform: String,
        onGenerated: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                val result = repository.generateMarketingContent(
                    _activeBusinessId.value,
                    productName,
                    category,
                    price,
                    targetAudience,
                    usp,
                    platform
                )
                onGenerated(result)
            } catch (e: Exception) {
                _userMessage.value = "Gagal generate marketing: ${e.message}"
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun generateBusinessReport(periodLabel: String, onGenerated: (BusinessReportEntity) -> Unit) {
        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                val report = repository.generateBusinessReport(_activeBusinessId.value, periodLabel)
                onGenerated(report)
                _userMessage.value = "Laporan bisnis berhasil dibuat!"
            } catch (e: Exception) {
                _userMessage.value = "Gagal membuat laporan: ${e.message}"
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            val demo = repository.seedDemoData()
            _activeBusinessId.value = demo.id
            _userMessage.value = "Data Demo 'Kopi & Roti Nusantara' berhasil dimuat dengan 8 produk dan 14 transaksi!"
        }
    }

    fun clearAllBusinessData() {
        viewModelScope.launch {
            repository.clearBusinessData(_activeBusinessId.value)
            _userMessage.value = "Seluruh data produk dan transaksi bisnis ini telah dibersihkan."
        }
    }

    fun switchBusiness(businessId: String) {
        _activeBusinessId.value = businessId
        repository.setActiveBusinessId(businessId)
        _userMessage.value = "Beralih ke profil bisnis."
    }

    fun createNewBusiness(name: String, category: String, address: String, phone: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val biz = repository.createBusiness(name, category, address, phone)
            _activeBusinessId.value = biz.id
            _userMessage.value = "Bisnis '${biz.name}' berhasil dibuat!"
            onSuccess()
        }
    }

    fun saveApiKey(key: String) {
        repository.saveApiKey(key)
        _userMessage.value = if (key.isBlank()) "API Key dihapus. Menggunakan mesin analisis lokal." else "API Key Gemini berhasil disimpan!"
    }
}
