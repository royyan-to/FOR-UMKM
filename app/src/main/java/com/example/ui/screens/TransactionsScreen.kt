package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.BusinessContextBuilder
import com.example.data.entity.ProductEntity
import com.example.data.entity.TransactionWithItems
import com.example.ui.components.SwissCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val products by viewModel.products.collectAsState()
    val aggregates by viewModel.aggregates.collectAsState()

    var showRecordSaleDialog by remember { mutableStateOf(false) }
    var showImportCsvDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionWithItems?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SwissBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pencatatan Transaksi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = SwissPrimaryText
                )
                Text(
                    text = "${transactions.size} transaksi • Omzet: ${BusinessContextBuilder.formatRupiah(aggregates.totalRevenue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SwissSecondaryText
                )
            }

            Button(
                onClick = { showRecordSaleDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SwissAccent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("record_sale_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Catat Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CSV Import/Export Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        val csv = viewModel.getCsvExport()
                        clipboardManager.setText(AnnotatedString(csv))
                        Toast.makeText(context, "Data CSV disalin ke clipboard (${transactions.size} transaksi)", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
            ) {
                Icon(imageVector = Icons.Outlined.FileDownload, contentDescription = null, tint = SwissPrimaryText, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ekspor CSV", color = SwissPrimaryText, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { showImportCsvDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
            ) {
                Icon(imageVector = Icons.Outlined.FileUpload, contentDescription = null, tint = SwissPrimaryText, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Impor CSV", color = SwissPrimaryText, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Transaction History List
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = SwissSecondaryText,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum ada transaksi penjualan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SwissSecondaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showRecordSaleDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SwissPrimaryText),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Catat Transaksi Pertama", fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 70.dp)
            ) {
                items(transactions, key = { it.transaction.id }) { tx ->
                    DetailedTransactionCard(
                        tx = tx,
                        onDelete = { transactionToDelete = tx }
                    )
                }
            }
        }
    }

    // Record Sale Dialog
    if (showRecordSaleDialog) {
        RecordSaleDialog(
            products = products,
            onDismiss = { showRecordSaleDialog = false },
            onSave = { customer, method, notes, items ->
                viewModel.recordSale(customer, method, notes, items) {
                    showRecordSaleDialog = false
                }
            }
        )
    }

    // Import CSV Dialog
    if (showImportCsvDialog) {
        ImportCsvDialog(
            onDismiss = { showImportCsvDialog = false },
            onImport = { csvContent ->
                viewModel.importCsv(csvContent)
                showImportCsvDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Hapus Transaksi?", fontWeight = FontWeight.Bold) },
            text = { Text("Hapus transaksi ${tx.transaction.invoiceNumber} (${tx.transaction.customerName} - ${BusinessContextBuilder.formatRupiah(tx.transaction.totalAmount)})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx.transaction.id)
                        transactionToDelete = null
                    }
                ) {
                    Text("Hapus", color = SwissAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Batal", color = SwissSecondaryText)
                }
            }
        )
    }
}

@Composable
fun DetailedTransactionCard(
    tx: TransactionWithItems,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID")) }
    val dateStr = sdf.format(Date(tx.transaction.timestamp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SwissAccentMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = SwissAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tx.transaction.invoiceNumber,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SwissPrimaryText
                        )
                        Text(
                            text = "${tx.transaction.customerName} • $dateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = SwissSecondaryText
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = BusinessContextBuilder.formatRupiah(tx.transaction.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
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

            // Expanded Item Details
            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = SwissBorder.copy(alpha = 0.6f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Rincian Barang:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SwissSecondaryText
                )
                Spacer(modifier = Modifier.height(6.dp))

                for (item in tx.items) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.productName} (${item.quantity}x @ ${BusinessContextBuilder.formatRupiah(item.unitPrice)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = SwissPrimaryText
                        )
                        Text(
                            text = BusinessContextBuilder.formatRupiah(item.subtotal),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SwissPrimaryText
                        )
                    }
                }

                if (tx.transaction.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Catatan: ${tx.transaction.notes}",
                        fontSize = 11.sp,
                        color = SwissSecondaryText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = SwissAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus Transaksi", color = SwissAccent, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RecordSaleDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, List<Pair<ProductEntity, Int>>) -> Unit
) {
    var customerName by remember { mutableStateOf("Pelanggan Umum") }
    var paymentMethod by remember { mutableStateOf("QRIS") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Map of productId to quantity
    val itemQuantities = remember { mutableStateMapOf<String, Int>() }

    val grandTotal = remember(itemQuantities, products) {
        val prodMap = products.associateBy { it.id }
        itemQuantities.entries.sumOf { (prodId, qty) ->
            val p = prodMap[prodId]
            if (p != null) p.price * qty else 0L
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Catat Transaksi Penjualan",
                fontWeight = FontWeight.Bold,
                color = SwissPrimaryText
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMessage != null) {
                    item {
                        Text(text = errorMessage!!, color = SwissAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Nama Pelanggan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("tx_customer_input")
                    )
                }

                item {
                    Text(text = "Metode Pembayaran:", style = MaterialTheme.typography.labelSmall, color = SwissSecondaryText)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("QRIS", "Tunai", "Transfer Bank").forEach { method ->
                            val isSel = paymentMethod == method
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) SwissPrimaryText else SwissSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { paymentMethod = method }
                            ) {
                                Text(
                                    text = method,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else SwissPrimaryText,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Pilih Produk & Kuantitas:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SwissPrimaryText
                    )
                }

                if (products.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada produk di katalog. Tambahkan produk terlebih dahulu di menu Produk.",
                            fontSize = 12.sp,
                            color = SwissAccent
                        )
                    }
                } else {
                    items(products, key = { it.id }) { prod ->
                        val qty = itemQuantities[prod.id] ?: 0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (qty > 0) SwissAccentMuted.copy(alpha = 0.5f) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (qty > 0) SwissAccent else SwissBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SwissPrimaryText
                                    )
                                    Text(
                                        text = "${BusinessContextBuilder.formatRupiah(prod.price)} • Stok: ${prod.stock}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SwissSecondaryText
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (qty > 0) {
                                                itemQuantities[prod.id] = qty - 1
                                                if (itemQuantities[prod.id] == 0) itemQuantities.remove(prod.id)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Kurang", tint = SwissPrimaryText)
                                    }

                                    Text(
                                        text = "$qty",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            if (qty < prod.stock) {
                                                itemQuantities[prod.id] = qty + 1
                                            } else {
                                                errorMessage = "Stok ${prod.name} hanya tersisa ${prod.stock}."
                                            }
                                        },
                                        modifier = Modifier.size(32.dp),
                                        enabled = prod.stock > 0
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah", tint = if (prod.stock > 0) SwissAccent else SwissBorder)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Pesanan (opsional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SwissSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total Transaksi:", fontWeight = FontWeight.Bold, color = SwissPrimaryText)
                            Text(
                                text = BusinessContextBuilder.formatRupiah(grandTotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = SwissAccent
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prodMap = products.associateBy { it.id }
                    val itemsToSell = itemQuantities.mapNotNull { (prodId, qty) ->
                        val p = prodMap[prodId]
                        if (p != null && qty > 0) Pair(p, qty) else null
                    }
                    if (itemsToSell.isEmpty()) {
                        errorMessage = "Pilih minimal 1 produk."
                        return@Button
                    }
                    onSave(customerName, paymentMethod, notes, itemsToSell)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SwissAccent),
                modifier = Modifier.testTag("submit_transaction_button")
            ) {
                Text("Konfirmasi Bayar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = SwissSecondaryText)
            }
        }
    )
}

@Composable
fun ImportCsvDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var csvText by remember { mutableStateOf("") }

    val sampleCsv = """
Invoice,Tanggal,Pelanggan,Metode,Produk,Jumlah,HargaSatuan
INV-20261022-001,2026-10-22,Budi,QRIS,Es Kopi Susu,2,22000
INV-20261022-002,2026-10-22,Siti,Tunai,Croissant,1,28000
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Impor Transaksi CSV", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Format kolom: Invoice, Tanggal, Pelanggan, Metode, Produk, Jumlah, HargaSatuan",
                    fontSize = 11.sp,
                    color = SwissSecondaryText
                )

                TextButton(
                    onClick = { csvText = sampleCsv },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Gunakan Contoh Format CSV", fontSize = 11.sp, color = SwissAccent)
                }

                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    placeholder = { Text("Tempel data CSV di sini...", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (csvText.isNotBlank()) onImport(csvText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SwissAccent),
                enabled = csvText.isNotBlank()
            ) {
                Text("Impor Sekarang", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = SwissSecondaryText)
            }
        }
    )
}
