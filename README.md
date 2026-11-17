# UsahaPilot AI - Asisten Bisnis & Keuangan untuk UMKM (Android Native)

Aplikasi Android native yang dirancang khusus untuk pemilik Usaha Mikro, Kecil, dan Menengah (UMKM) di Indonesia. **UsahaPilot AI** membantu pencatatan transaksi kasir (POS), manajemen inventori dan stok, pembuatan materi pemasaran digital otomatis, laporan keuangan, serta asisten percakapan cerdas yang terhubung (*grounded*) langsung dengan data bisnis nyata tanpa halusinasi.

---

## 🎨 Karakteristik Desain (Swiss Minimalist Premium)
- **Background**: `#F4F2EC`
- **Surface**: `#FBFAF6`
- **Primary Text**: `#171715`
- **Secondary Text**: `#77766F`
- **Border / Outline**: `#DCD9D0`
- **Accent**: `#E04B2F` (Swiss Red/Orange)
- Mengedepankan hierarki tipografi tegas, konsistensi grid 8dp/16dp, kartu statistik minimalis, dan visualisasi Canvas murni tanpa gradien neon berlebihan.

---

## 🚀 Fitur Utama

### 1. Dashboard Bisnis & Analitik Real-Time
- **Metrik Utama (KPI)**: Total Penjualan (IDR), Jumlah Transaksi, Rata-rata Nilai Transaksi (*Average Order Value / AOV*), dan Produk Terlaris.
- **Filter Periode**: *Semua Waktu*, *7 Hari Terakhir*, dan *30 Hari Terakhir*.
- **Grafik Batang Penjualan Interaktif**: Digambar menggunakan Jetpack Compose `Canvas` yang merefleksikan transaksi riil dari database.
- **Peringatan Stok Menipis**: Banner otomatis yang menandai produk yang telah mencapai batas stok minimum.

### 2. Manajemen Produk & Inventori (CRUD)
- Tambah, ubah, dan hapus produk dengan field: Nama, Kategori, Harga Jual, HPP (Modal), Stok Saat Ini, dan Batas Stok Minimum.
- Validasi data ketat: mencegah nama kosong, harga jual Rp 0, atau stok negatif.
- Pencarian produk instan dan filter berbasis chip kategori.

### 3. Transaksi & Kasir POS
- Pemilihan multi-produk dengan kalkulator kuantitas dan total otomatis.
- Pilihan metode pembayaran: **QRIS**, **Tunai**, dan **Transfer Bank**.
- **Atomic Stock Deduction**: Pemotongan stok otomatis saat transaksi dicatat (`@androidx.room.Transaction`).
- Validasi stok: mencegah transaksi jika kuantitas melebihi stok yang tersedia.
- **Ekspor & Impor CSV**: Mendukung backup transaksi ke CSV dan impor data transaksi massal.

### 4. AI Chat Assistant (Grounding Data Nyata)
- Chatbot cerdas yang menganalisis data bisnis pengguna yang sedang aktif (Omzet, AOV, Top 5 Produk, Stok Rendah, dan Transaksi Terbaru).
- **Struktur Respon Editorial**:
  - `[FAKTA DATA]`: Angka transaksi faktual.
  - `[ESTIMASI & ANALISIS]`: Evaluasi persentase penjualan dan tren.
  - `[REKOMENDASI AKSI]`: Rekomendasi promosi, bundling, atau restock.
- **Zero Hallucination Guarantee**: Jika transaksi masih kosong, AI secara jujur mengonfirmasi kondisi 0 transaksi dan mengarahkan pengguna untuk mulai mencatat penjualan atau memuat demo.
- Riwayat percakapan tersimpan di Room Database dan dapat disalin ke clipboard.

### 5. AI Marketing Generator
- Menghasilkan materi promosi instan untuk produk UMKM:
  1. Caption Instagram (Hook, Value, CTA, Hashtags).
  2. Deskripsi Produk Marketplace (Shopee / Tokopedia).
  3. Teks Promosi WhatsApp Broadcast.
  4. Ide Kalender Konten 7 Hari.
  5. 5 Variasi Judul Promosi Menarik.

### 6. Laporan Bisnis Eksekutif
- Ringkasan performa berkala otomatis yang merangkum omzet, produk unggulan, temuan penting, dan rekomendasi langkah berikutnya.

### 7. Multi-Bisnis & Data Demo Siap Pakai
- Pengguna dapat mengelola beberapa unit bisnis sekaligus.
- Tombol **"Muat Data Demo"** (bisnis *Kopi & Roti Nusantara* dengan 8 produk dan 14 transaksi) untuk uji coba langsung tanpa perlu input manual.

---

## 🛠️ Arsitektur & Teknologi

- **Bahasa**: Kotlin (100% Jetpack Compose)
- **Arsitektur**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Database Lokal**: Android Room Database (SQLite) dengan Kotlin Coroutines & Flow
- **AI Backend**: Google Gemini 3.5 Flash via REST API (OkHttpClient dengan timeout 60 detik) & Local Fallback Engine
- **Secret Management**: Google Secrets Gradle Plugin (`BuildConfig.GEMINI_API_KEY`) atau input manual via menu Pengaturan
- **Testing**: JUnit & Robolectric untuk verifikasi kalkulasi database dan prompt logic

---

## 🧪 Hasil Pengujian (Unit & Robolectric Tests)

Pengujian otomatis telah dijalankan dan lulus 100%:
- `testBusinessAggregatesCalculation`: Memverifikasi kalkulasi total penjualan, AOV, produk terlaris, dan identifikasi stok menipis.
- `testAiAssistantAnswersBestSellingProductAccurately`: Memverifikasi bahwa respons AI menjawab nama produk dan kuantitas faktual sesuai data.
- `testAiAssistantHandlesEmptyTransactionsHonestly`: Memverifikasi bahwa AI tidak berhalusinasi saat data transaksi kosong.
- `testMarketingGeneratorProducesAllRequiredSections`: Memverifikasi kelengkapan output marketing copy (Instagram, Marketplace, WhatsApp, Kalender 7 Hari, 5 Variasi Judul).
- `testAiAssistantPromotionRecommendationForSlowMovingProducts`: Memverifikasi pembuatan strategi promosi untuk produk dengan penjualan rendah.
- `read string from context`: Pengujian Robolectric context dan string resource.
