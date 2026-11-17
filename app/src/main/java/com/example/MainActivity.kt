package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.SwissBottomNavBar
import com.example.ui.components.SwissTopBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeBusiness by viewModel.activeBusiness.collectAsState()
    val allBusinesses by viewModel.allBusinesses.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showBusinessSwitcherSheet by remember { mutableStateOf(false) }
    var showCreateBusinessDialog by remember { mutableStateOf(false) }

    // Show Snackbar when userMessage changes
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Android Hardware / Gesture BackHandler: Return to Dashboard if on another screen
    if (currentScreen != AppScreen.DASHBOARD) {
        BackHandler {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentScreen != AppScreen.LANDING) {
                SwissTopBar(
                    currentScreen = currentScreen,
                    activeBusiness = activeBusiness,
                    onSwitchBusinessClick = { showBusinessSwitcherSheet = true },
                    onNavigateLanding = { viewModel.navigateTo(AppScreen.LANDING) },
                    onNavigateSettings = { viewModel.navigateTo(AppScreen.SETTINGS) }
                )
            }
        },
        bottomBar = {
            if (currentScreen != AppScreen.LANDING) {
                SwissBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        },
        containerColor = SwissBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.LANDING -> LandingScreen(viewModel = viewModel)
                AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppScreen.PRODUCTS -> ProductsScreen(viewModel = viewModel)
                AppScreen.TRANSACTIONS -> TransactionsScreen(viewModel = viewModel)
                AppScreen.CHAT_ASSISTANT -> ChatAssistantScreen(viewModel = viewModel)
                AppScreen.SALES_ANALYTICS -> DashboardScreen(viewModel = viewModel)
                AppScreen.MARKETING_GENERATOR -> MarketingGeneratorScreen(viewModel = viewModel)
                AppScreen.BUSINESS_REPORTS -> BusinessReportsScreen(viewModel = viewModel)
                AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Business Switcher Modal Bottom Sheet
    if (showBusinessSwitcherSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBusinessSwitcherSheet = false },
            containerColor = SwissSurface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Profil Usaha",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SwissPrimaryText
                    )
                    TextButton(onClick = {
                        showBusinessSwitcherSheet = false
                        showCreateBusinessDialog = true
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = SwissAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", color = SwissAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "Beralih antar unit bisnis untuk mengelola inventori dan data transaksi yang terisolasi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SwissSecondaryText
                )

                Spacer(modifier = Modifier.height(4.dp))

                for (b in allBusinesses) {
                    val isActive = b.id == activeBusiness?.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isActive) SwissPrimaryText else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) SwissPrimaryText else SwissBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.switchBusiness(b.id)
                                showBusinessSwitcherSheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isActive) SwissAccent else SwissSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = if (isActive) Color.White else SwissPrimaryText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = b.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isActive) Color.White else SwissPrimaryText
                                    )
                                    Text(
                                        text = b.category,
                                        fontSize = 11.sp,
                                        color = if (isActive) Color.White.copy(alpha = 0.7f) else SwissSecondaryText
                                    )
                                }
                            }

                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SwissAccent)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("Aktif", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Business Dialog
    if (showCreateBusinessDialog) {
        var newBizName by remember { mutableStateOf("") }
        var newBizCategory by remember { mutableStateOf("Makanan & Minuman") }
        var newBizAddress by remember { mutableStateOf("") }
        var newBizPhone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateBusinessDialog = false },
            title = { Text("Tambah Usaha Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newBizName,
                        onValueChange = { newBizName = it },
                        label = { Text("Nama Usaha *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_biz_name_input")
                    )
                    OutlinedTextField(
                        value = newBizCategory,
                        onValueChange = { newBizCategory = it },
                        label = { Text("Kategori (e.g. Kafe, Fashion, Toko Kelontong)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newBizAddress,
                        onValueChange = { newBizAddress = it },
                        label = { Text("Alamat / Lokasi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newBizPhone,
                        onValueChange = { newBizPhone = it },
                        label = { Text("Nomor Kontak / WhatsApp") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBizName.isNotBlank()) {
                            viewModel.createNewBusiness(newBizName, newBizCategory, newBizAddress, newBizPhone) {
                                showCreateBusinessDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwissAccent),
                    enabled = newBizName.isNotBlank()
                ) {
                    Text("Simpan Usaha", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateBusinessDialog = false }) {
                    Text("Batal", color = SwissSecondaryText)
                }
            }
        )
    }
}
