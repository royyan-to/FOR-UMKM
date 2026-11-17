package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.BusinessContextBuilder
import com.example.data.entity.TransactionWithItems
import com.example.ui.components.SwissCard
import com.example.ui.components.SwissStatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeBusiness by viewModel.activeBusiness.collectAsState()
    val aggregates by viewModel.aggregates.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val currentPeriod by viewModel.dashboardPeriod.collectAsState()
    val products by viewModel.products.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SwissBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Business Header & Welcome
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ringkasan Bisnis",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = SwissPrimaryText
                    )
                    Text(
                        text = activeBusiness?.let { "${it.name} • ${it.category}" } ?: "Memuat data...",
                        style = MaterialTheme.typography.bodySmall,
                        color = SwissSecondaryText
                    )
                }

                // AI Chat Shortcut
                FilledTonalButton(
                    onClick = { viewModel.navigateTo(AppScreen.CHAT_ASSISTANT) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SwissAccentMuted,
                        contentColor = SwissAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("dashboard_ai_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tanya AI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Period Filters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeriodPill(
                    label = "Semua Waktu",
                    isSelected = currentPeriod == "ALL",
                    onClick = { viewModel.setDashboardPeriod("ALL") }
                )
                PeriodPill(
                    label = "7 Hari Terakhir",
                    isSelected = currentPeriod == "7_DAYS",
                    onClick = { viewModel.setDashboardPeriod("7_DAYS") }
                )
                PeriodPill(
                    label = "30 Hari Terakhir",
                    isSelected = currentPeriod == "30_DAYS",
                    onClick = { viewModel.setDashboardPeriod("30_DAYS") }
                )
            }
        }

        // KPI Stat Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SwissStatCard(
                    title = "Total Penjualan",
                    value = BusinessContextBuilder.formatRupiah(aggregates.totalRevenue),
                    icon = Icons.Outlined.MonetizationOn,
                    badgeText = "${aggregates.totalTransactions} Transaksi",
                    badgeColor = SwissSuccess,
                    modifier = Modifier.weight(1f)
                )

                val bestSeller = aggregates.topSellingProducts.firstOrNull()
                SwissStatCard(
                    title = "Rata-rata Order (AOV)",
                    value = BusinessContextBuilder.formatRupiah(aggregates.averageOrderValue),
                    icon = Icons.Outlined.TrendingUp,
                    subtitle = if (bestSeller != null) "Top: ${bestSeller.first}" else "Belum ada penjualan",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Low Stock Warning Banner (if any)
        if (aggregates.lowStockProducts.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SwissWarning.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SwissWarning.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SwissWarning),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Perhatian: ${aggregates.lowStockProducts.size} Produk Stok Menipis",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SwissPrimaryText
                            )
                            Text(
                                text = aggregates.lowStockProducts.joinToString(", ") { "${it.name} (${it.stock})" },
                                style = MaterialTheme.typography.bodySmall,
                                color = SwissSecondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { viewModel.navigateTo(AppScreen.PRODUCTS) }
                        ) {
                            Text("Restock", color = SwissAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Sales Trend Bar Chart
        item {
            SwissCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Grafik Penjualan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SwissPrimaryText
                        )
                        Text(
                            text = "Distribusi nominal transaksi tersimpan",
                            style = MaterialTheme.typography.bodySmall,
                            color = SwissSecondaryText
                        )
                    }
                    Text(
                        text = BusinessContextBuilder.formatRupiah(aggregates.totalRevenue),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SwissAccent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SwissSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada transaksi di periode ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SwissSecondaryText
                        )
                    }
                } else {
                    SalesBarChart(transactions = filteredTransactions)
                }
            }
        }

        // Top 5 Products Table
        item {
            SwissCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "5 Produk Terlaris",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SwissPrimaryText
                    )
                    TextButton(onClick = { viewModel.navigateTo(AppScreen.PRODUCTS) }) {
                        Text("Lihat Semua", color = SwissAccent, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (aggregates.topSellingProducts.isEmpty()) {
                    Text(
                        text = "Belum ada data penjualan produk.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SwissSecondaryText,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    aggregates.topSellingProducts.forEachIndexed { index, pair ->
                        val rev = aggregates.productRevenueMap[pair.first] ?: 0L
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (index == 0) SwissAccent else SwissSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (index == 0) Color.White else SwissPrimaryText
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = pair.first,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SwissPrimaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${pair.second} unit terjual",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SwissSecondaryText
                                    )
                                }
                            }
                            Text(
                                text = BusinessContextBuilder.formatRupiah(rev),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SwissPrimaryText
                            )
                        }
                        if (index < aggregates.topSellingProducts.size - 1) {
                            HorizontalDivider(color = SwissBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.TRANSACTIONS) },
                    colors = ButtonDefaults.buttonColors(containerColor = SwissAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_record_transaction_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Catat Transaksi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.MARKETING_GENERATOR) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SwissPrimaryText),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_marketing_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = SwissAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Marketing AI", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // Recent Transactions Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Transaksi Terbaru",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SwissPrimaryText
                )
                TextButton(onClick = { viewModel.navigateTo(AppScreen.TRANSACTIONS) }) {
                    Text("Kelola", color = SwissAccent, fontSize = 12.sp)
                }
            }
        }

        val recentTxs = filteredTransactions.take(5)
        if (recentTxs.isEmpty()) {
            item {
                SwissCard {
                    Text(
                        text = "Belum ada riwayat transaksi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SwissSecondaryText,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        } else {
            items(recentTxs, key = { it.transaction.id }) { tx ->
                TransactionRowCard(tx = tx)
            }
        }
    }
}

@Composable
fun PeriodPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) SwissPrimaryText else SwissSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SwissPrimaryText else SwissBorder),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else SwissSecondaryText,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun TransactionRowCard(tx: TransactionWithItems) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID")) }
    val dateStr = sdf.format(Date(tx.transaction.timestamp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SwissSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = SwissPrimaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = tx.transaction.invoiceNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SwissPrimaryText
                    )
                    Text(
                        text = "${tx.transaction.customerName} • $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = SwissSecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = BusinessContextBuilder.formatRupiah(tx.transaction.totalAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SwissAccent
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SwissSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tx.transaction.paymentMethod,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = SwissPrimaryText
                    )
                }
            }
        }
    }
}

@Composable
fun SalesBarChart(transactions: List<TransactionWithItems>) {
    // Group transactions by recent chunks (last 7 data points)
    val recentItems = transactions.take(7).reversed()
    val maxAmount = (recentItems.maxOfOrNull { it.transaction.totalAmount } ?: 100000L).coerceAtLeast(10000L)

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val barCount = recentItems.size
            if (barCount == 0) return@Canvas

            val spacing = 16.dp.toPx()
            val totalSpacing = spacing * (barCount + 1)
            val barWidth = ((size.width - totalSpacing) / barCount).coerceIn(12.dp.toPx(), 42.dp.toPx())

            val startX = (size.width - ((barWidth * barCount) + (spacing * (barCount - 1)))) / 2

            for (i in 0 until barCount) {
                val tx = recentItems[i]
                val amount = tx.transaction.totalAmount
                val barHeight = ((amount.toFloat() / maxAmount) * (size.height - 30.dp.toPx())).coerceAtLeast(10f)

                val x = startX + i * (barWidth + spacing)
                val y = size.height - barHeight - 20.dp.toPx()

                // Draw bar background track
                drawRoundRect(
                    color = Color(0xFFF0EEE7),
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, size.height - 20.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Draw filled bar
                drawRoundRect(
                    color = if (i == barCount - 1) Color(0xFFE04B2F) else Color(0xFF171715),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        // X-axis label row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            recentItems.forEachIndexed { idx, tx ->
                val label = tx.transaction.invoiceNumber.takeLast(4)
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = SwissSecondaryText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
