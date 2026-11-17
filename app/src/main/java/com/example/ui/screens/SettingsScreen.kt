package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SwissCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeBusiness by viewModel.activeBusiness.collectAsState()
    val allBusinesses by viewModel.allBusinesses.collectAsState()
    val products by viewModel.products.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var apiKeyInput by remember { mutableStateOf(viewModel.repository.getSavedApiKey()) }
    var showCreateBusinessDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SwissBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Pengaturan & Akun",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = SwissPrimaryText
                )
                Text(
                    text = "Kelola profil usaha, integrasi AI, dan data penyimpanan",
                    style = MaterialTheme.typography.bodySmall,
                    color = SwissSecondaryText
                )
            }
        }

        // Active Business Card
        item {
            SwissCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeBusiness?.name ?: "Pilih Usaha",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SwissPrimaryText
                            )
                            if (activeBusiness?.isDemo == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SwissWarning.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("DEMO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SwissWarning)
                                }
                            }
                        }
                        Text(
                            text = "Kategori: ${activeBusiness?.category ?: "-"} • Mata Uang: IDR (Rp)",
                            style = MaterialTheme.typography.bodySmall,
                            color = SwissSecondaryText
                        )
                        if (activeBusiness?.address?.isNotBlank() == true) {
                            Text(
                                text = "Alamat: ${activeBusiness?.address}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SwissSecondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SwissBorder.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Total Produk: ${products.size}", style = MaterialTheme.typography.labelMedium, color = SwissPrimaryText)
                    Text(text = "Total Transaksi: ${transactions.size}", style = MaterialTheme.typography.labelMedium, color = SwissPrimaryText)
                }
            }
        }

        // Business Switcher
        item {
            SwissCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ganti atau Tambah Usaha",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SwissPrimaryText
                    )
                    TextButton(onClick = { showCreateBusinessDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = SwissAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Usaha", color = SwissAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                for (b in allBusinesses) {
                    val isActive = b.id == activeBusiness?.id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isActive) SwissPrimaryText else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) SwissPrimaryText else SwissBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.switchBusiness(b.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
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

        // Gemini AI Configuration
        item {
            SwissCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(SwissAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Konfigurasi Gemini AI",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SwissPrimaryText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Aplikasi terintegrasi dengan Google Gemini 3.5 Flash. Kunci API diambil secara otomatis dari Secrets Panel atau Anda dapat memasukkan kunci custom di bawah ini:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SwissSecondaryText
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("api_key_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (apiKeyInput.isNotBlank()) {
                        TextButton(onClick = {
                            apiKeyInput = ""
                            viewModel.saveApiKey("")
                        }) {
                            Text("Hapus Kunci", color = SwissSecondaryText, fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = { viewModel.saveApiKey(apiKeyInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = SwissAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simpan API Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Demo Data & Reset Actions
        item {
            SwissCard {
                Text(
                    text = "Manajemen Data & Demo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SwissPrimaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Muat data contoh UMKM siap pakai untuk menguji dashboard dan AI Chat Assistant, atau bersihkan data untuk memulai usaha baru dari nol.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SwissSecondaryText
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.seedDemoData() },
                    colors = ButtonDefaults.buttonColors(containerColor = SwissPrimaryText),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("seed_demo_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Muat Data Demo (Kopi & Roti)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    border = androidx.compose.foundation.BorderStroke(1.dp, SwissAccent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SwissAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("reset_data_button")
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = SwissAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kosongkan Semua Data Usaha Ini", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
            title = { Text("Tambah Profil Usaha Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newBizName,
                        onValueChange = { newBizName = it },
                        label = { Text("Nama Usaha *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newBizCategory,
                        onValueChange = { newBizCategory = it },
                        label = { Text("Kategori Usaha (e.g. Retail, Fashion, Kafe)") },
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
                        label = { Text("Nomor Telepon / WhatsApp") },
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

    // Reset Data Confirmation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Kosongkan Semua Data?", fontWeight = FontWeight.Bold) },
            text = { Text("Seluruh katalog produk, transaksi, dan riwayat chat pada bisnis '${activeBusiness?.name}' akan dihapus permanen. Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllBusinessData()
                        showResetDialog = false
                    }
                ) {
                    Text("Ya, Hapus Semua", color = SwissAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal", color = SwissSecondaryText)
                }
            }
        )
    }
}
