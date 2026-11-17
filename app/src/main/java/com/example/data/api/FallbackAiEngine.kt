package com.example.data.api

import com.example.data.entity.BusinessEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.TransactionWithItems
import java.util.Locale

object FallbackAiEngine {

    fun answerBusinessQuestion(
        question: String,
        business: BusinessEntity,
        products: List<ProductEntity>,
        transactions: List<TransactionWithItems>
    ): String {
        val q = question.lowercase(Locale.ROOT)
        val agg = BusinessContextBuilder.computeAggregates(products, transactions)

        // Case 1: No transactions
        if (transactions.isEmpty()) {
            return """
[FAKTA DATA]
Saat ini bisnis **${business.name}** belum memiliki data transaksi penjualan yang tercatat (0 transaksi, Rp 0).
Jumlah produk terdaftar: ${products.size} produk.

[ESTIMASI & ANALISIS]
Karena belum ada transaksi, performa penjualan belum dapat dievaluasi. Data analitik akan terisi otomatis setelah Anda mencatat penjualan.

[REKOMENDASI AKSI]
1. Buka menu **Transaksi** dan catat transaksi pertama Anda (atau gunakan fitur *Impor CSV*).
2. Anda juga dapat memuat **Data Demo UMKM** melalui menu *Pengaturan* untuk melihat simulasi analisis data secara lengkap.
            """.trimIndent()
        }

        // Case 2: Best selling product question
        if (q.contains("laku") || q.contains("terlaris") || q.contains("paling banyak") || q.contains("bestseller") || q.contains("favorit")) {
            val topList = agg.topSellingProducts
            if (topList.isEmpty()) {
                return "Belum ada produk yang terjual dari transaksi yang tercatat."
            }
            val best = topList.first()
            val bestRev = agg.productRevenueMap[best.first] ?: 0L

            val sb = StringBuilder()
            sb.append("[FAKTA DATA]\n")
            sb.append("Produk paling laku di **${business.name}** adalah **${best.first}** dengan total terjual **${best.second} unit** (omzet: ${BusinessContextBuilder.formatRupiah(bestRev)}).\n\n")
            sb.append("Peringkat 5 Produk Teratas:\n")
            for ((idx, p) in topList.withIndex()) {
                val rev = agg.productRevenueMap[p.first] ?: 0L
                sb.append("${idx + 1}. **${p.first}** - ${p.second} unit (${BusinessContextBuilder.formatRupiah(rev)})\n")
            }
            sb.append("\n[ESTIMASI & ANALISIS]\n")
            val totalSoldUnits = topList.sumOf { it.second }
            val share = if (totalSoldUnits > 0) (best.second.toDouble() / totalSoldUnits * 100).toInt() else 0
            sb.append("Produk **${best.first}** menyumbang sekitar $share% dari volume penjualan produk teratas Anda. Produk ini memiliki daya tarik utama bagi pelanggan Anda.\n\n")
            sb.append("[REKOMENDASI AKSI]\n")
            sb.append("1. Pastikan stok bahan/persediaan **${best.first}** selalu aman agar tidak kehilangan omzet.\n")
            sb.append("2. Buat paket bundling: pasangkan **${best.first}** dengan produk yang penjualannya lebih rendah untuk meningkatkan nilai transaksi rata-rata (AOV).\n")
            sb.append("3. Jadikan foto/review produk ini sebagai materi utama di iklan atau media sosial.")
            return sb.toString()
        }

        // Case 3: Comparison / trends
        if (q.contains("banding") || q.contains("perubahan") || q.contains("tren") || q.contains("kemarin") || q.contains("minggu") || q.contains("bulan") || q.contains("sebelumnya")) {
            val last7 = agg.periodRevenue7Days
            val prev7 = agg.periodRevenuePrevious7Days
            val sb = StringBuilder()
            sb.append("[FAKTA DATA]\n")
            sb.append("- Penjualan 7 Hari Terakhir: ${BusinessContextBuilder.formatRupiah(last7)}\n")
            sb.append("- Penjualan 7 Hari Sebelumnya: ${BusinessContextBuilder.formatRupiah(prev7)}\n")
            sb.append("- Total Seluruh Penjualan: ${BusinessContextBuilder.formatRupiah(agg.totalRevenue)} (${agg.totalTransactions} transaksi)\n")
            sb.append("- Rata-rata Nilai Transaksi: ${BusinessContextBuilder.formatRupiah(agg.averageOrderValue)}\n\n")

            sb.append("[ESTIMASI & ANALISIS]\n")
            if (prev7 > 0) {
                val diff = last7 - prev7
                val pct = (diff.toDouble() / prev7) * 100
                if (diff >= 0) {
                    sb.append("Penjualan Anda mengalami **kenaikan sebesar +${String.format(Locale.US, "%.1f", pct)}%** dibandingkan 7 hari sebelumnya. Tren positif menunjukkan respon pasar yang baik.\n\n")
                } else {
                    sb.append("Penjualan Anda mengalami **penurunan sebesar ${String.format(Locale.US, "%.1f", pct)}%** dibandingkan 7 hari sebelumnya.\n\n")
                }
            } else {
                sb.append("Belum ada cukup data di periode 7 hari sebelumnya untuk menghitung persentase pertumbuhan mingguan.\n\n")
            }

            sb.append("[REKOMENDASI AKSI]\n")
            sb.append("1. Pantau hari-hari puncak transaksi untuk menambah jam operasional atau promosi khusus pada hari sepi.\n")
            sb.append("2. Tingkatkan nilai belanja per pelanggan dengan promo minimal order (contoh: 'Gratis ongkir / diskon 10% untuk belanja di atas ${BusinessContextBuilder.formatRupiah(agg.averageOrderValue + 15000)}').")
            return sb.toString()
        }

        // Case 4: Promotion for slow-moving products
        if (q.contains("promo") || q.contains("promosi") || q.contains("rendah") || q.contains("kurang") || q.contains("sepi") || q.contains("iklan")) {
            val slowProducts = if (agg.unsoldProducts.isNotEmpty()) agg.unsoldProducts else products.filter { it.stock > 0 }
            val target = slowProducts.firstOrNull()
            val targetName = target?.name ?: "Produk Pilihan"
            val targetPrice = target?.price ?: 25000L

            return """
[FAKTA DATA]
- Produk dengan pergerakan lambat atau belum terjual: **$targetName** (Harga: ${BusinessContextBuilder.formatRupiah(targetPrice)}, Sisa stok: ${target?.stock ?: 0}).
- Produk andalan saat ini: **${agg.topSellingProducts.firstOrNull()?.first ?: "Menu Terlaris"}**.

[ESTIMASI & ANALISIS]
Produk ini mungkin kurang terlihat oleh pelanggan atau membutuhkan dorongan nilai tambah agar dicoba untuk pertama kali.

[REKOMENDASI AKSI & DRAF PROMOSI]
**Konsep Promo: 'Paket Kombo Hemat'**
Gunakan pesan WhatsApp atau Instagram berikut:

*"🔥 PROMO SPESIAL UNTUK KAMU DARI ${business.name}!*
*Beli ${agg.topSellingProducts.firstOrNull()?.first ?: "Menu Terfavorit"}, bisa tebus murah $targetName hanya dengan diskon 20%!*
*Stok sangat terbatas. Balas pesan ini sekarang untuk amankan pesananmu! 🚀"*

💡 *Tips:* Gunakan fitur **AI Marketing Generator** di aplikasi ini untuk membuat materi konten 7 hari lengkap!
            """.trimIndent()
        }

        // Case 5: Stock / Evaluation / Report
        if (q.contains("stok") || q.contains("evaluasi") || q.contains("laporan") || q.contains("habis") || q.contains("saran") || q.contains("eval")) {
            val sb = StringBuilder()
            sb.append("[FAKTA DATA]\n")
            sb.append("- Total Produk Terdaftar: ${products.size}\n")
            sb.append("- Produk Stok Menipis: ${agg.lowStockProducts.size} item\n")
            if (agg.lowStockProducts.isNotEmpty()) {
                for (p in agg.lowStockProducts) {
                    sb.append("  * **${p.name}**: Sisa ${p.stock} pcs (Batas aman: ${p.minStock})\n")
                }
            } else {
                sb.append("  * Seluruh persediaan produk berada di atas batas minimum aman.\n")
            }

            sb.append("\n[ESTIMASI & ANALISIS]\n")
            if (agg.lowStockProducts.isNotEmpty()) {
                sb.append("Risiko *stock-out* (kehabisan stok) dapat mengganggu omzet mingguan Anda, khususnya jika produk yang menipis termasuk dalam daftar produk terlaris.\n\n")
            } else {
                sb.append("Kondisi stok stabil. Fokus saat ini dapat dialihkan ke percepatan perputaran inventori (*inventory turnover*).\n\n")
            }

            sb.append("[REKOMENDASI AKSI]\n")
            if (agg.lowStockProducts.isNotEmpty()) {
                sb.append("1. Segera lakukan pemesanan ulang (*reorder*) ke supplier untuk: ${agg.lowStockProducts.joinToString(", ") { it.name }}.\n")
            }
            sb.append("2. Lakukan evaluasi harga modal (HPP) berkala untuk menjaga margin keuntungan kotor tetap di atas 30%.\n")
            sb.append("3. Rekap laporan bulanan di menu **Laporan Bisnis** untuk arsip pembukuan Anda.")
            return sb.toString()
        }

        // General fallback summary
        return """
[FAKTA DATA]
Ringkasan Bisnis **${business.name}**:
- Total Omzet: ${BusinessContextBuilder.formatRupiah(agg.totalRevenue)}
- Total Transaksi: ${agg.totalTransactions}
- Rata-rata Pembelian: ${BusinessContextBuilder.formatRupiah(agg.averageOrderValue)}
- Produk Terlaris: ${agg.topSellingProducts.firstOrNull()?.let { "${it.first} (${it.second} unit)" } ?: "Belum ada"}

[ESTIMASI & ANALISIS]
Data Anda menunjukkan perputaran bisnis yang aktif. Anda dapat menanyakan secara spesifik tentang perbandingan mingguan, analisis stok, atau strategi promosi.

[REKOMENDASI AKSI]
Silakan tanyakan pertanyaan seperti:
- *"Produk mana yang paling laku?"*
- *"Bagaimana perbandingan penjualan minggu ini?"*
- *"Buatkan draf promosi untuk produk dengan penjualan rendah"*
- *"Apa evaluasi stok saat ini?"*
        """.trimIndent()
    }

    fun generateMarketing(
        productName: String,
        category: String,
        price: Long,
        targetAudience: String,
        usp: String,
        platform: String
    ): String {
        val priceStr = BusinessContextBuilder.formatRupiah(price)
        return """
=== HASIL GENERATOR KONTEN PEMASARAN UMKM ===

📌 TARGET: $platform | PRODUK: $productName ($category)
💰 HARGA: $priceStr
🎯 AUDIENCE: $targetAudience
⭐ KEUNGGULAN: $usp

---
1. INSTAGRAM CAPTION
---
Pernah nggak sih ngerasa butuh sesuatu yang praktis tapi tetap berkualitas? ✨

Kenalin nih: $productName! Solusi pas buat kamu yang $targetAudience. Dengan $usp, dijamin bikin harimu lebih mudah dan menyenangkan.

Dapatkan sekarang cuma seharga $priceStr! 

Yuk langsung checkout sekarang lewat link di bio sebelum kehabisan! 🛒💨
.
#$category #$productName #UMKMIndonesia #ProdukLokal #SolusiCerdas #PromoSpesial

---
2. DESKRIPSI PRODUK MARKETPLACE (Shopee / Tokopedia)
---
[$productName - Original & Berkualitas]

Ingin produk yang awet, terpercaya, dan memberikan manfaat maksimal? $productName hadir dengan standar terbaik khusus untuk Anda.

Keunggulan Produk:
✅ $usp
✅ Kualitas terjamin dan lolos kontrol mutu
✅ Harga terjangkau: $priceStr
✅ Pengemasan aman & pengiriman cepat

Cocok untuk: $targetAudience

Catatan:
* Garansi retur jika produk rusak saat diterima
* Stok terbatas, amankan pesanan Anda hari ini!

---
3. TEKS PROMOSI WHATSAPP BROADCAST
---
Halo Kak! 👋
Semoga harinya menyenangkan ya!

Kabar gembira buat Kakak, kini telah tersedia *$productName* dengan keunggulan istimewa: *$usp*.

Khusus minggu ini, Kakak bisa dapatkan dengan harga spesial: *$priceStr*.

Mau tanya-tanya atau pesan sekarang? Cukup balas chat ini: *"Mau pesan $productName"* ya Kak! Kami siap bantu dengan senang hati. 😊🙏

---
4. IDE KONTEN 7 HARI (Content Calendar)
---
• Hari 1: [Reels/TikTok] Masalah umum yang dihadapi $targetAudience dan bagaimana $productName menyelesaikannya.
• Hari 2: [Foto Carousel] 3 Alasan kenapa kamu wajib punya $productName ($usp).
• Hari 3: [Story Tanya-Jawab] Q&A seputar fungsi dan tips penggunaan.
• Hari 4: [Video Singkat] Proses di balik layar (Behind The Scene) penyiapan produk.
• Hari 5: [Social Proof] Review jujur atau testimoni pelanggan.
• Hari 6: [Promo Hook] "Gaji cair? Waktunya manjakan diri dengan $productName cuma $priceStr!"
• Hari 7: [Edukasi/Tips] Tips bermanfaat terkait $category.

---
5. 5 VARIASI JUDUL PROMOSI
---
1. "Solusi Terbaik untuk Kamu: $productName Hadir Lebih Praktis!"
2. "Cuma $priceStr, Rasakan Manfaat Nyata $productName Hari Ini!"
3. "Ketinggalan Jaman Kalau Belum Coba $productName dengan $usp!"
4. "Rahasia $targetAudience Tampil Maksimal Setiap Hari."
5. "Stok Terbatas! Amankan $productName Milikmu Sekarang Juga!"
        """.trimIndent()
    }
}
