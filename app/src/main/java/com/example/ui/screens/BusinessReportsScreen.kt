package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BusinessReportEntity
import com.example.ui.components.SwissCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BusinessReportsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val reports by viewModel.businessReports.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    var selectedPeriod by remember { mutableStateOf("30 Hari Terakhir") }
    val clipboardManager = LocalClipboardManager.current
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
                    text = "Laporan Bisnis & Evaluasi AI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = SwissPrimaryText
                )
                Text(
                    text = "Laporan performa otomatis dengan ringkasan penjualan, temuan penting, dan rekomendasi aksi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SwissSecondaryText
                )
            }
        }

        // Generator Card
        item {
            SwissCard {
                Text(
                    text = "Pilih Periode Laporan:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SwissPrimaryText
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("7 Hari Terakhir", "30 Hari Terakhir", "Semua Waktu").forEach { p ->
                        PeriodPill(
                            label = p,
                            isSelected = selectedPeriod == p,
                            onClick = { selectedPeriod = p }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.generateBusinessReport(selectedPeriod) { report ->
                            // Handled via ViewModel state
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwissAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("generate_report_button"),
                    enabled = !isAiThinking
                ) {
                    if (isAiThinking) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Sedang Menyusun Laporan...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buat Laporan Baru", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Reports List
        if (reports.isEmpty()) {
            item {
                SwissCard {
                    Text(
                        text = "Belum ada laporan tersimpan. Tekan tombol di atas untuk membuat laporan performa pertama Anda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SwissSecondaryText,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        } else {
            item {
                Text(
                    text = "Arsip Laporan Tersimpan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SwissPrimaryText
                )
            }

            items(reports, key = { it.id }) { report ->
                ReportCardItem(
                    report = report,
                    onCopy = {
                        val exportText = """
${report.title}
Periode: ${report.period}

--- RINGKASAN & ANALISIS AI ---
${report.summary}

--- TEMUAN PENTING ---
${report.findingsJson}

--- REKOMENDASI TINDAKAN ---
${report.recommendationsJson}
                        """.trimIndent()
                        clipboardManager.setText(AnnotatedString(exportText))
                        Toast.makeText(context, "Laporan disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun ReportCardItem(
    report: BusinessReportEntity,
    onCopy: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID")) }
    val dateStr = sdf.format(Date(report.createdAt))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SwissPrimaryText
                    )
                    Text(
                        text = "Dibuat pada: $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = SwissSecondaryText
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "Salin Laporan", tint = SwissPrimaryText)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = SwissSurfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = report.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SwissPrimaryText,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SwissAccentMuted)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = report.period,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SwissAccent
                    )
                }
            }
        }
    }
}
