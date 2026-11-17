package com.example.data.repository

import android.content.Context
import com.example.data.api.BusinessAggregates
import com.example.data.api.BusinessContextBuilder
import com.example.data.api.FallbackAiEngine
import com.example.data.api.GeminiClient
import com.example.data.dao.AppDao
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class BusinessRepository(
    private val appDao: AppDao,
    private val geminiClient: GeminiClient,
    private val context: Context
) {
    private val sharedPrefs = context.getSharedPreferences("usahapilot_prefs", Context.MODE_PRIVATE)

    fun getSavedApiKey(): String {
        return sharedPrefs.getString("gemini_api_key", "").orEmpty()
    }

    fun saveApiKey(key: String) {
        sharedPrefs.edit().putString("gemini_api_key", key.trim()).apply()
        geminiClient.setApiKey(key.trim())
    }

    fun getActiveBusinessId(): String {
        return sharedPrefs.getString("active_business_id", "demo_biz_1").orEmpty()
    }

    fun setActiveBusinessId(id: String) {
        sharedPrefs.edit().putString("active_business_id", id).apply()
    }

    // --- Profiles & Businesses ---
    fun getAllBusinesses(): Flow<List<BusinessEntity>> = appDao.getAllBusinesses()

    suspend fun getBusiness(id: String): BusinessEntity? = appDao.getBusiness(id)

    suspend fun createBusiness(name: String, category: String, address: String, phone: String): BusinessEntity {
        val bizId = "biz_" + UUID.randomUUID().toString().take(8)
        val profileId = "prof_default"
        val profile = appDao.getProfile(profileId) ?: ProfileEntity(
            id = profileId,
            name = "Pemilik Usaha",
            email = "pemilik@usahapilot.id"
        ).also { appDao.insertProfile(it) }

        val business = BusinessEntity(
            id = bizId,
            profileId = profile.id,
            name = name.trim(),
            category = category.trim(),
            address = address.trim(),
            phone = phone.trim(),
            isDemo = false
        )
        appDao.insertBusiness(business)
        setActiveBusinessId(bizId)
        return business
    }

    suspend fun updateBusiness(business: BusinessEntity) {
        appDao.updateBusiness(business)
    }

    // --- Products ---
    fun getProducts(businessId: String): Flow<List<ProductEntity>> = appDao.getProductsByBusiness(businessId)

    suspend fun addProduct(
        businessId: String,
        name: String,
        category: String,
        price: Long,
        costPrice: Long,
        stock: Int,
        minStock: Int,
        description: String
    ): Result<ProductEntity> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Nama produk tidak boleh kosong"))
        if (price <= 0) return Result.failure(IllegalArgumentException("Harga jual harus lebih dari Rp 0"))
        if (stock < 0) return Result.failure(IllegalArgumentException("Stok tidak boleh bernilai negatif"))

        val product = ProductEntity(
            id = "prod_" + UUID.randomUUID().toString().take(8),
            businessId = businessId,
            name = name.trim(),
            category = category.trim().ifBlank { "Umum" },
            price = price,
            costPrice = costPrice,
            stock = stock,
            minStock = minStock.coerceAtLeast(0),
            description = description.trim()
        )
        appDao.insertProduct(product)
        return Result.success(product)
    }

    suspend fun updateProduct(product: ProductEntity): Result<Unit> {
        if (product.name.isBlank()) return Result.failure(IllegalArgumentException("Nama produk tidak boleh kosong"))
        if (product.price <= 0) return Result.failure(IllegalArgumentException("Harga jual harus lebih dari Rp 0"))
        if (product.stock < 0) return Result.failure(IllegalArgumentException("Stok tidak boleh bernilai negatif"))

        appDao.updateProduct(product)
        return Result.success(Unit)
    }

    suspend fun deleteProduct(productId: String) {
        appDao.deleteProduct(productId)
    }

    // --- Transactions ---
    fun getTransactions(businessId: String): Flow<List<TransactionWithItems>> =
        appDao.getTransactionsWithItems(businessId)

    suspend fun recordSale(
        businessId: String,
        customerName: String,
        paymentMethod: String,
        notes: String,
        itemsToSell: List<Pair<ProductEntity, Int>> // Product & quantity
    ): Result<TransactionEntity> {
        if (itemsToSell.isEmpty()) {
            return Result.failure(IllegalArgumentException("Pilih minimal satu produk untuk transaksi."))
        }

        // Validate stock
        for ((prod, qty) in itemsToSell) {
            if (qty <= 0) {
                return Result.failure(IllegalArgumentException("Jumlah pesanan untuk ${prod.name} harus minimal 1."))
            }
            if (prod.stock < qty) {
                return Result.failure(IllegalArgumentException("Stok ${prod.name} tidak mencukupi (Tersedia: ${prod.stock}, Diminta: $qty)."))
            }
        }

        val txId = "tx_" + UUID.randomUUID().toString().take(8)
        val sdf = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US)
        val invoiceNum = "INV-" + sdf.format(Date()) + "-" + (100..999).random()

        var grandTotal = 0L
        val txItems = mutableListOf<TransactionItemEntity>()

        for ((prod, qty) in itemsToSell) {
            val subtotal = prod.price * qty
            grandTotal += subtotal
            txItems.add(
                TransactionItemEntity(
                    id = "item_" + UUID.randomUUID().toString().take(8),
                    transactionId = txId,
                    productId = prod.id,
                    productName = prod.name,
                    quantity = qty,
                    unitPrice = prod.price,
                    subtotal = subtotal
                )
            )
        }

        val transaction = TransactionEntity(
            id = txId,
            businessId = businessId,
            invoiceNumber = invoiceNum,
            timestamp = System.currentTimeMillis(),
            customerName = customerName.trim().ifBlank { "Pelanggan Umum" },
            totalAmount = grandTotal,
            paymentMethod = paymentMethod,
            notes = notes.trim()
        )

        appDao.recordSaleTransaction(transaction, txItems)
        return Result.success(transaction)
    }

    suspend fun deleteTransaction(txId: String) {
        appDao.deleteTransaction(txId)
    }

    // --- AI Chat Assistant ---
    fun getChatMessages(businessId: String): Flow<List<ChatMessageEntity>> =
        appDao.getChatMessages(businessId)

    suspend fun sendChatMessage(businessId: String, userPrompt: String): String {
        if (userPrompt.isBlank()) return ""

        val business = appDao.getBusiness(businessId) ?: BusinessEntity(
            id = businessId,
            profileId = "prof_default",
            name = "UMKM Saya",
            category = "Umum"
        )
        val products = appDao.getProductsListByBusiness(businessId)
        val transactions = appDao.getTransactionsWithItemsList(businessId)

        // Save User Message
        val userMsgId = "msg_" + UUID.randomUUID().toString().take(8)
        appDao.insertChatMessage(
            ChatMessageEntity(
                id = userMsgId,
                businessId = businessId,
                sender = "user",
                message = userPrompt.trim(),
                timestamp = System.currentTimeMillis()
            )
        )

        // Prepare System Context
        val systemContext = BusinessContextBuilder.buildSystemContext(business, products, transactions)

        // Generate response
        val responseText = geminiClient.generateContent(
            systemInstruction = systemContext,
            userPrompt = userPrompt.trim(),
            fallbackGenerator = {
                FallbackAiEngine.answerBusinessQuestion(userPrompt, business, products, transactions)
            }
        )

        // Save Assistant Message
        val assistantMsgId = "msg_" + UUID.randomUUID().toString().take(8)
        appDao.insertChatMessage(
            ChatMessageEntity(
                id = assistantMsgId,
                businessId = businessId,
                sender = "assistant",
                message = responseText,
                timestamp = System.currentTimeMillis() + 10L
            )
        )

        return responseText
    }

    suspend fun clearChat(businessId: String) {
        appDao.clearChatMessages(businessId)
    }

    // --- AI Marketing Content Generation ---
    fun getAiGenerations(businessId: String): Flow<List<AiGenerationEntity>> =
        appDao.getAiGenerations(businessId)

    suspend fun generateMarketingContent(
        businessId: String,
        productName: String,
        category: String,
        price: Long,
        targetAudience: String,
        usp: String,
        platform: String
    ): String {
        val userPrompt = """
Buatkan materi konten pemasaran lengkap dalam Bahasa Indonesia untuk UMKM:
- Produk: $productName
- Kategori: $category
- Harga: ${BusinessContextBuilder.formatRupiah(price)}
- Target Pelanggan: $targetAudience
- Keunggulan Utama (USP): $usp
- Platform Prioritas: $platform

Format luaran harus terstruktur rapi:
1. Caption Instagram (Hook, Value, CTA, Hashtags)
2. Deskripsi Produk Marketplace (Shopee/Tokopedia)
3. Teks Promosi WhatsApp Broadcast
4. Ide Konten 7 Hari (Content Calendar)
5. 5 Variasi Judul Promosi Menarik
        """.trimIndent()

        val systemInstruction = "Anda adalah Copywriter dan Digital Marketing Specialist senior untuk UMKM Indonesia. Gunakan bahasa Indonesia yang persuasif, natural, dan sesuai target pasar."

        val result = geminiClient.generateContent(
            systemInstruction = systemInstruction,
            userPrompt = userPrompt,
            fallbackGenerator = {
                FallbackAiEngine.generateMarketing(productName, category, price, targetAudience, usp, platform)
            }
        )

        val gen = AiGenerationEntity(
            id = "gen_" + UUID.randomUUID().toString().take(8),
            businessId = businessId,
            featureType = "MARKETING_SUITE",
            prompt = "$productName ($platform)",
            resultContent = result
        )
        appDao.insertAiGeneration(gen)
        return result
    }

    // --- Business Reports ---
    fun getBusinessReports(businessId: String): Flow<List<BusinessReportEntity>> =
        appDao.getBusinessReports(businessId)

    suspend fun generateBusinessReport(businessId: String, periodLabel: String): BusinessReportEntity {
        val business = appDao.getBusiness(businessId) ?: BusinessEntity(
            id = businessId,
            profileId = "prof_default",
            name = "UMKM Saya",
            category = "Umum"
        )
        val products = appDao.getProductsListByBusiness(businessId)
        val transactions = appDao.getTransactionsWithItemsList(businessId)
        val agg = BusinessContextBuilder.computeAggregates(products, transactions)

        val prompt = "Buat laporan analisis penjualan dan evaluasi bisnis resmi untuk periode $periodLabel."
        val systemContext = BusinessContextBuilder.buildSystemContext(business, products, transactions)

        val aiSummary = geminiClient.generateContent(
            systemInstruction = systemContext,
            userPrompt = prompt,
            fallbackGenerator = {
                FallbackAiEngine.answerBusinessQuestion("evaluasi dan laporan penjualan periode $periodLabel", business, products, transactions)
            }
        )

        val topProductsStr = agg.topSellingProducts.joinToString("; ") { "${it.first} (${it.second} pcs)" }
        val findingsStr = "Total penjualan: ${BusinessContextBuilder.formatRupiah(agg.totalRevenue)}, ${agg.totalTransactions} transaksi, AOV: ${BusinessContextBuilder.formatRupiah(agg.averageOrderValue)}. Stok menipis: ${agg.lowStockProducts.size} item."
        val recommendationsStr = if (agg.lowStockProducts.isNotEmpty()) {
            "Restock segera: ${agg.lowStockProducts.joinToString { it.name }}. Buat program bundling produk terlaris."
        } else {
            "Pertahankan ketersediaan produk utama dan lakukan kampanye promosi di akhir pekan."
        }

        val report = BusinessReportEntity(
            id = "rep_" + UUID.randomUUID().toString().take(8),
            businessId = businessId,
            title = "Laporan Performa Bisnis - $periodLabel",
            period = periodLabel,
            summary = aiSummary,
            topProductsJson = topProductsStr,
            findingsJson = findingsStr,
            recommendationsJson = recommendationsStr
        )
        appDao.insertBusinessReport(report)
        return report
    }

    // --- CSV Import & Export ---
    suspend fun importTransactionsCsv(businessId: String, csvContent: String): Pair<Int, String> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return Pair(0, "File CSV kosong.")
        }

        val existingProducts = appDao.getProductsListByBusiness(businessId).associateBy { it.name.lowercase(Locale.ROOT) }.toMutableMap()
        var importedCount = 0
        var errorCount = 0

        // Expected CSV format:
        // Invoice,Tanggal,Pelanggan,Metode,Produk,Jumlah,HargaSatuan
        val startIndex = if (lines[0].contains("invoice", ignoreCase = true) || lines[0].contains("produk", ignoreCase = true)) 1 else 0

        val groupedByInvoice = mutableMapOf<String, MutableList<Pair<ProductEntity, Int>>>()
        val invoiceMeta = mutableMapOf<String, Triple<String, String, Long>>() // Customer, PaymentMethod, Timestamp

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (i in startIndex until lines.size) {
            val line = lines[i]
            val cols = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (cols.size < 6) {
                errorCount++
                continue
            }

            try {
                val invNum = cols[0].ifBlank { "INV-CSV-" + UUID.randomUUID().toString().take(6) }
                val dateStr = cols.getOrNull(1).orEmpty()
                val parsedTime = try {
                    if (dateStr.isNotBlank()) sdf.parse(dateStr)?.time ?: System.currentTimeMillis() else System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
                val custName = cols.getOrNull(2).orEmpty().ifBlank { "Pelanggan CSV" }
                val payMethod = cols.getOrNull(3).orEmpty().ifBlank { "Tunai" }
                val prodName = cols.getOrNull(4).orEmpty()
                val qty = cols.getOrNull(5)?.toIntOrNull() ?: 1
                val unitPrice = cols.getOrNull(6)?.toLongOrNull() ?: 20000L

                if (prodName.isBlank() || qty <= 0) {
                    errorCount++
                    continue
                }

                var product = existingProducts[prodName.lowercase(Locale.ROOT)]
                if (product == null) {
                    // Auto-create product from CSV line
                    product = ProductEntity(
                        id = "prod_" + UUID.randomUUID().toString().take(8),
                        businessId = businessId,
                        name = prodName,
                        category = "Impor CSV",
                        price = unitPrice,
                        costPrice = (unitPrice * 0.6).toLong(),
                        stock = 50,
                        minStock = 5
                    )
                    appDao.insertProduct(product)
                    existingProducts[prodName.lowercase(Locale.ROOT)] = product
                }

                groupedByInvoice.getOrPut(invNum) { mutableListOf() }.add(Pair(product, qty))
                invoiceMeta[invNum] = Triple(custName, payMethod, parsedTime)
            } catch (e: Exception) {
                errorCount++
            }
        }

        for ((invNum, items) in groupedByInvoice) {
            val meta = invoiceMeta[invNum] ?: Triple("Pelanggan CSV", "Tunai", System.currentTimeMillis())
            val txId = "tx_" + UUID.randomUUID().toString().take(8)
            var total = 0L
            val txItems = items.map { (prod, qty) ->
                val sub = prod.price * qty
                total += sub
                TransactionItemEntity(
                    id = "item_" + UUID.randomUUID().toString().take(8),
                    transactionId = txId,
                    productId = prod.id,
                    productName = prod.name,
                    quantity = qty,
                    unitPrice = prod.price,
                    subtotal = sub
                )
            }

            val tx = TransactionEntity(
                id = txId,
                businessId = businessId,
                invoiceNumber = invNum,
                timestamp = meta.third,
                customerName = meta.first,
                totalAmount = total,
                paymentMethod = meta.second,
                notes = "Diimpor dari file CSV"
            )
            appDao.recordSaleTransaction(tx, txItems)
            importedCount++
        }

        val message = "Berhasil mengimpor $importedCount transaksi." + (if (errorCount > 0) " Terdapat $errorCount baris tidak valid dilewati." else "")
        return Pair(importedCount, message)
    }

    suspend fun exportTransactionsToCsv(businessId: String): String {
        val transactions = appDao.getTransactionsWithItemsList(businessId)
        val sb = StringBuilder()
        sb.append("Invoice,Tanggal,Pelanggan,Metode,TotalTransaksi,Produk,Jumlah,HargaSatuan,Subtotal\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

        for (tx in transactions) {
            val t = tx.transaction
            val dateStr = sdf.format(Date(t.timestamp))
            for (item in tx.items) {
                sb.append("\"${t.invoiceNumber}\",")
                sb.append("\"$dateStr\",")
                sb.append("\"${t.customerName}\",")
                sb.append("\"${t.paymentMethod}\",")
                sb.append("${t.totalAmount},")
                sb.append("\"${item.productName}\",")
                sb.append("${item.quantity},")
                sb.append("${item.unitPrice},")
                sb.append("${item.subtotal}\n")
            }
        }
        return sb.toString()
    }

    // --- Seed Demo Data ---
    suspend fun seedDemoData(): BusinessEntity {
        val profile = ProfileEntity(
            id = "prof_demo_1",
            name = "Rizky Firmansyah",
            email = "rizky.umkm@usahapilot.id",
            role = "Pemilik Usaha"
        )
        appDao.insertProfile(profile)

        val bizId = "demo_biz_kopi"
        val business = BusinessEntity(
            id = bizId,
            profileId = profile.id,
            name = "Kopi & Roti Nusantara (DEMO)",
            category = "Makanan & Minuman",
            currency = "IDR",
            address = "Jl. Senopati No. 45, Jakarta Selatan",
            phone = "0812-3456-7890",
            isDemo = true
        )
        appDao.insertBusiness(business)

        // Clear existing demo products & tx to ensure clean seed
        appDao.clearProductsForBusiness(bizId)
        appDao.clearTransactionsForBusiness(bizId)

        val demoProducts = listOf(
            ProductEntity("p1", bizId, "Es Kopi Susu Gula Aren", "KOP-01", "Kopi", 22000L, 8000L, 45, 10, "Kopi susu espresso blend dengan gula aren murni"),
            ProductEntity("p2", bizId, "Croissant Butter", "ROTI-01", "Pastry", 28000L, 12000L, 18, 5, "Croissant renyah berlapis dengan mentega Prancis"),
            ProductEntity("p3", bizId, "Americano Dingin", "KOP-02", "Kopi", 20000L, 6000L, 30, 8, "Espresso ganda segar dengan es batu"),
            ProductEntity("p4", bizId, "Matcha Latte Creamy", "NON-01", "Non-Kopi", 26000L, 11000L, 22, 5, "Matcha Jepang otentik dengan susu segar"),
            ProductEntity("p5", bizId, "Toast Srikaya Pandan", "ROTI-02", "Pastry", 24000L, 9000L, 3, 5, "Roti panggang lembut dengan selai srikaya pandan wangi (Stok Menipis)"),
            ProductEntity("p6", bizId, "Roti Cokelat Keju Lumer", "ROTI-03", "Pastry", 18000L, 7000L, 25, 6, "Roti manis isi cokelat Belgia dan keju cheddar"),
            ProductEntity("p7", bizId, "Earl Grey Milk Tea", "NON-02", "Non-Kopi", 25000L, 10000L, 15, 5, "Teh Earl Grey harum berpadu susu gurih"),
            ProductEntity("p8", bizId, "Sandwich Ayam Panggang", "SNK-01", "Makanan Ringan", 35000L, 16000L, 8, 4, "Roti gandum dengan dada ayam panggang saus mustard")
        )
        appDao.insertProducts(demoProducts)

        // Seed 14 realistic transactions spread over last 10 days
        val now = System.currentTimeMillis()
        val oneDayMs = 24L * 60 * 60 * 1000

        val txDefinitions = listOf(
            Triple("Andi Wijaya", "QRIS", listOf("p1" to 2, "p2" to 1)), // 72.000
            Triple("Siti Nurhaliza", "Tunai", listOf("p1" to 1, "p5" to 2)), // 70.000
            Triple("Budi Santoso", "QRIS", listOf("p3" to 2, "p8" to 1)), // 75.000
            Triple("Dian Pratama", "Transfer Bank", listOf("p4" to 3, "p2" to 2)), // 134.000
            Triple("Mega Utami", "QRIS", listOf("p1" to 4, "p6" to 2)), // 124.000
            Triple("Farhan Maulana", "Tunai", listOf("p3" to 1, "p6" to 1)), // 38.000
            Triple("Nadya Safira", "QRIS", listOf("p1" to 2, "p4" to 1, "p2" to 1)), // 98.000
            Triple("Reza Rahadian", "QRIS", listOf("p8" to 2, "p3" to 2)), // 110.000
            Triple("Putri Ayu", "Tunai", listOf("p7" to 2, "p5" to 1)), // 74.000
            Triple("Eko Prasetyo", "QRIS", listOf("p1" to 3, "p2" to 2)), // 122.000
            Triple("Gita Gutawa", "QRIS", listOf("p4" to 2, "p6" to 1)), // 70.000
            Triple("Hendra Kurnia", "Tunai", listOf("p1" to 1, "p3" to 1)), // 42.000
            Triple("Indah Permata", "QRIS", listOf("p1" to 2, "p8" to 1)), // 79.000
            Triple("Joko Susilo", "QRIS", listOf("p2" to 3, "p4" to 1))  // 110.000
        )

        val prodMap = demoProducts.associateBy { it.id }

        for ((idx, txDef) in txDefinitions.withIndex()) {
            val daysAgo = (idx % 8)
            val txTime = now - (daysAgo * oneDayMs) - (idx * 3600000L)
            val txId = "tx_demo_$idx"
            val invNum = "INV-202610" + String.format(Locale.US, "%02d", 20 - daysAgo) + "-00" + (idx + 1)

            var subTotalSum = 0L
            val items = mutableListOf<TransactionItemEntity>()

            for ((prodId, qty) in txDef.third) {
                val prod = prodMap[prodId] ?: continue
                val lineTotal = prod.price * qty
                subTotalSum += lineTotal
                items.add(
                    TransactionItemEntity(
                        id = "item_demo_${idx}_$prodId",
                        transactionId = txId,
                        productId = prod.id,
                        productName = prod.name,
                        quantity = qty,
                        unitPrice = prod.price,
                        subtotal = lineTotal
                    )
                )
            }

            val txEntity = TransactionEntity(
                id = txId,
                businessId = bizId,
                invoiceNumber = invNum,
                timestamp = txTime,
                customerName = txDef.first,
                totalAmount = subTotalSum,
                paymentMethod = txDef.second,
                notes = "Transaksi Pelanggan Kasir"
            )
            appDao.insertTransaction(txEntity)
            appDao.insertTransactionItems(items)
        }

        // Seed initial welcoming message in Chatbot
        appDao.clearChatMessages(bizId)
        appDao.insertChatMessage(
            ChatMessageEntity(
                id = "msg_welcome",
                businessId = bizId,
                sender = "assistant",
                message = """
Halo! Saya **UsahaPilot AI**, asisten bisnis Anda untuk **${business.name}**.

Data penjualan dan produk bisnis Anda sudah terhubung secara langsung. Anda dapat menanyakan apa saja, seperti:
• *"Produk mana yang paling laku?"*
• *"Bagaimana perbandingan penjualan minggu ini?"*
• *"Buatkan ide promosi untuk produk dengan penjualan rendah"*
• *"Apa yang perlu saya evaluasi dari stok saat ini?"*

Silakan pilih tombol pintasan di bawah atau ketik pertanyaan Anda! 😊
                """.trimIndent(),
                timestamp = System.currentTimeMillis()
            )
        )

        setActiveBusinessId(bizId)
        return business
    }

    suspend fun clearBusinessData(businessId: String) {
        appDao.clearProductsForBusiness(businessId)
        appDao.clearTransactionsForBusiness(businessId)
        appDao.clearChatMessages(businessId)
    }
}
