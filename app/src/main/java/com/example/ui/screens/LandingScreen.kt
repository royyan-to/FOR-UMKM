package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SwissCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LandingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SwissBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        // Hero Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SwissAccentMuted)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🚀 Asisten Bisnis AI No. 1 untuk UMKM Indonesia",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SwissAccent
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Kendalikan Bisnis Anda dengan Pilot Cerdas AI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SwissPrimaryText,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Bantu pemilik toko kecil, warung, kafe, dan penjual online mengurus administrasi kasir, menganalisis penjualan, serta merancang promosi otomatis berbasis data nyata.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SwissSecondaryText,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        colors = ButtonDefaults.buttonColors(containerColor = SwissAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("landing_start_button")
                    ) {
                        Text("Buka Dashboard", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.CHAT_ASSISTANT) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SwissPrimaryText),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("landing_chat_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = SwissAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Coba AI Chat", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Problems Solved
        item {
            SwissCard {
                Text(
                    text = "Tantangan Utama UMKM yang Kami Selesaikan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SwissPrimaryText
                )
                Spacer(modifier = Modifier.height(12.dp))

                ProblemItem(
                    icon = Icons.Outlined.EditNote,
                    title = "Pencatatan Masih Manual",
                    description = "Sering lupa mencatat penjualan harian, stok selisih, dan pembukuan tidak rapi."
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProblemItem(
                    icon = Icons.Outlined.QueryStats,
                    title = "Sulit Membaca Data Penjualan",
                    description = "Bingung produk mana yang benar-benar menghasilkan laba vs produk yang menumpuk di gudang."
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProblemItem(
                    icon = Icons.Outlined.Campaign,
                    title = "Buntu Ide Bikin Konten Promosi",
                    description = "Tidak punya waktu membuat materi copywriting Instagram dan broadcast WhatsApp setiap hari."
                )
            }
        }

        // 4 Core Features
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Fitur Unggulan UsahaPilot AI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SwissPrimaryText
                )

                FeatureCard(
                    title = "Dashboard Bisnis & Stok Real-Time",
                    description = "Pantau total omzet, jumlah transaksi, rata-rata belanja (AOV), dan peringatan stok menipis otomatis.",
                    icon = Icons.Default.Dashboard
                )

                FeatureCard(
                    title = "AI Chat Assistant Grounded Data",
                    description = "Tanyakan langsung ke asisten AI tentang tren penjualan, perbandingan mingguan, dan evaluasi inventori tanpa mengarang data.",
                    icon = Icons.Default.AutoAwesome
                )

                FeatureCard(
                    title = "AI Marketing Copy Generator",
                    description = "Hasilkan caption Instagram, deskripsi produk marketplace, broadcast WhatsApp, dan jadwal konten 7 hari sekali klik.",
                    icon = Icons.Default.Campaign
                )

                FeatureCard(
                    title = "Laporan Bisnis Otomatis & Ekspor CSV",
                    description = "Dapatkan ringkasan performa eksekutif berkala, temuan penting, dan rekomendasi aksi yang siap dibagikan.",
                    icon = Icons.Default.Assessment
                )
            }
        }

        // How it works
        item {
            SwissCard {
                Text(
                    text = "Cara Kerja UsahaPilot AI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SwissPrimaryText
                )
                Spacer(modifier = Modifier.height(12.dp))

                StepRow(step = "1", title = "Input atau Impor Data", desc = "Catat penjualan di kasir atau unggah data transaksi melalui format CSV.")
                Spacer(modifier = Modifier.height(10.dp))
                StepRow(step = "2", title = "AI Memproses Otomatis", desc = "Mesin analitik menghitung metrik, tren produk laku, dan margin usaha Anda.")
                Spacer(modifier = Modifier.height(10.dp))
                StepRow(step = "3", title = "Ambil Keputusan Tepat", desc = "Konsultasikan strategi dengan AI Chatbot dan jalankan promosi terarah.")
            }
        }

        // Pricing Section
        item {
            SwissCard {
                Text(
                    text = "Pilihan Paket Fleksibel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SwissPrimaryText
                )
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SwissSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Paket UMKM Starter", fontWeight = FontWeight.Bold, color = SwissPrimaryText)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SwissSuccess)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = "GRATIS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "• Manajemen Produk & Transaksi Tanpa Batas\n• AI Chat Assistant Berbasis Data Lokal\n• Ekspor & Impor CSV\n• Marketing Generator 5 Platform", fontSize = 12.sp, color = SwissSecondaryText, lineHeight = 20.sp)
                    }
                }
            }
        }

        // Bottom CTA
        item {
            Button(
                onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                colors = ButtonDefaults.buttonColors(containerColor = SwissAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Masuk ke Dashboard Sekarang", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ProblemItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SwissAccentMuted),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = SwissAccent, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SwissPrimaryText)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = SwissSecondaryText)
        }
    }
}

@Composable
fun FeatureCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SwissSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = SwissAccent, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SwissPrimaryText)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = SwissSecondaryText)
            }
        }
    }
}

@Composable
fun StepRow(step: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(SwissPrimaryText),
            contentAlignment = Alignment.Center
        ) {
            Text(text = step, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SwissPrimaryText)
            Text(text = desc, fontSize = 12.sp, color = SwissSecondaryText)
        }
    }
}
