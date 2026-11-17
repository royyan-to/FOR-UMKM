package com.example.data.api

import com.example.data.entity.BusinessEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.TransactionWithItems
import java.text.NumberFormat
import java.util.Locale

data class BusinessAggregates(
    val totalRevenue: Long,
    val totalTransactions: Int,
    val averageOrderValue: Long,
    val topSellingProducts: List<Pair<String, Int>>, // productName to totalQty
    val productRevenueMap: Map<String, Long>,
    val lowStockProducts: List<ProductEntity>,
    val unsoldProducts: List<ProductEntity>,
    val periodRevenue7Days: Long,
    val periodRevenuePrevious7Days: Long
)

object BusinessContextBuilder {

    fun formatRupiah(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }

    fun computeAggregates(
        products: List<ProductEntity>,
        transactions: List<TransactionWithItems>
    ): BusinessAggregates {
        val now = System.currentTimeMillis()
        val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
        val fourteenDaysMs = 14L * 24 * 60 * 60 * 1000

        var totalRev = 0L
        var revLast7Days = 0L
        var revPrev7Days = 0L

        val productQtyMap = mutableMapOf<String, Int>()
        val productRevMap = mutableMapOf<String, Long>()

        for (tx in transactions) {
            val amount = tx.transaction.totalAmount
            totalRev += amount

            val txTime = tx.transaction.timestamp
            if (txTime >= (now - sevenDaysMs)) {
                revLast7Days += amount
            } else if (txTime >= (now - fourteenDaysMs)) {
                revPrev7Days += amount
            }

            for (item in tx.items) {
                productQtyMap[item.productName] = (productQtyMap[item.productName] ?: 0) + item.quantity
                productRevMap[item.productName] = (productRevMap[item.productName] ?: 0L) + item.subtotal
            }
        }

        val aov = if (transactions.isNotEmpty()) totalRev / transactions.size else 0L

        val topSelling = productQtyMap.toList().sortedByDescending { it.second }.take(5)

        val lowStock = products.filter { it.stock <= it.minStock }
        val soldProductNames = productQtyMap.keys
        val unsold = products.filter { it.name !in soldProductNames }

        return BusinessAggregates(
            totalRevenue = totalRev,
            totalTransactions = transactions.size,
            averageOrderValue = aov,
            topSellingProducts = topSelling,
            productRevenueMap = productRevMap,
            lowStockProducts = lowStock,
            unsoldProducts = unsold,
            periodRevenue7Days = revLast7Days,
            periodRevenuePrevious7Days = revPrev7Days
        )
    }

    fun buildSystemContext(
        business: BusinessEntity,
        products: List<ProductEntity>,
        transactions: List<TransactionWithItems>
    ): String {
        val agg = computeAggregates(products, transactions)
        val sb = StringBuilder()

        sb.append("Anda adalah UsahaPilot AI, asisten bisnis cerdas untuk pemilik UMKM di Indonesia.\n")
        sb.append("Profil Usaha:\n")
        sb.append("- Nama Bisnis: ${business.name}\n")
        sb.append("- Kategori: ${business.category}\n")
        sb.append("- Alamat/Lokasi: ${if (business.address.isNotBlank()) business.address else "Belum diatur"}\n\n")

        sb.append("DATA AKTUAL BISNIS (Fakta Real-Time dari Sistem):\n")
        if (transactions.isEmpty()) {
            sb.append("PERHATIAN: Bisnis ini BELUM memiliki riwayat transaksi penjualan (0 transaksi, Rp 0).\n")
            sb.append("Jumlah produk terdaftar: ${products.size}.\n")
            if (products.isNotEmpty()) {
                sb.append("Daftar produk yang tersedia: ")
                sb.append(products.joinToString(", ") { "${it.name} (Stok: ${it.stock}, Harga: ${formatRupiah(it.price)})" })
                sb.append("\n")
            }
            sb.append("Jika pengguna menanyakan penjualan atau produk terlaris, jelaskan secara jujur bahwa belum ada transaksi yang dicatat, dan berikan panduan singkat cara mencatat penjualan di menu Transaksi.\n\n")
        } else {
            sb.append("- Total Penjualan: ${formatRupiah(agg.totalRevenue)}\n")
            sb.append("- Total Transaksi: ${agg.totalTransactions}\n")
            sb.append("- Rata-rata Nilai Transaksi (AOV): ${formatRupiah(agg.averageOrderValue)}\n")
            sb.append("- Penjualan 7 Hari Terakhir: ${formatRupiah(agg.periodRevenue7Days)}\n")
            sb.append("- Penjualan 7 Hari Sebelumnya: ${formatRupiah(agg.periodRevenuePrevious7Days)}\n")

            if (agg.periodRevenuePrevious7Days > 0) {
                val growth = ((agg.periodRevenue7Days - agg.periodRevenuePrevious7Days).toDouble() / agg.periodRevenuePrevious7Days) * 100
                sb.append("- Perubahan 7 Hari Terakhir vs Sebelumnya: ${String.format(Locale.US, "%.1f", growth)}%\n")
            }

            sb.append("\nPRODUK TERLARIS (Top 5):\n")
            for ((index, pair) in agg.topSellingProducts.withIndex()) {
                val rev = agg.productRevenueMap[pair.first] ?: 0L
                sb.append("${index + 1}. ${pair.first} - Terjual: ${pair.second} unit (${formatRupiah(rev)})\n")
            }

            if (agg.lowStockProducts.isNotEmpty()) {
                sb.append("\nPERINGATAN STOK MENIPIS (Harus Restock Segera):\n")
                for (p in agg.lowStockProducts) {
                    sb.append("- ${p.name}: Sisa stok ${p.stock} (Batas minimum: ${p.minStock})\n")
                }
            }

            if (agg.unsoldProducts.isNotEmpty()) {
                sb.append("\nPRODUK BELUM MEMILIKI PENJUALAN:\n")
                sb.append(agg.unsoldProducts.take(5).joinToString(", ") { "${it.name} (Stok: ${it.stock})" })
                sb.append("\n")
            }

            sb.append("\n5 TRANSAKSI TERAKHIR:\n")
            for (tx in transactions.take(5)) {
                val itemSummary = tx.items.joinToString(", ") { "${it.productName} (${it.quantity}x)" }
                sb.append("- ${tx.transaction.invoiceNumber}: ${tx.transaction.customerName} - ${formatRupiah(tx.transaction.totalAmount)} [${tx.transaction.paymentMethod}] Items: $itemSummary\n")
            }
            sb.append("\n")
        }

        sb.append("PETUNJUK JAWABAN:\n")
        sb.append("1. Jawablah langsung, ringkas, solutif, dan ramah dengan gaya Swiss Minimalist yang to-the-point.\n")
        sb.append("2. Gunakan selalu data di atas untuk menjawab. JANGAN MENGARANG angka atau transaksi yang tidak ada.\n")
        sb.append("3. Bedakan antara [FAKTA DATA], [ESTIMASI/ANALISIS], dan [REKOMENDASI AKSI].\n")
        sb.append("4. Jika diminta ide promosi atau caption, gunakan produk nyata dari daftar produk dan harga asli dalam Rupiah.\n")

        return sb.toString()
    }
}
