package com.example

import com.example.data.api.BusinessContextBuilder
import com.example.data.api.FallbackAiEngine
import com.example.data.entity.BusinessEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionItemEntity
import com.example.data.entity.TransactionWithItems
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    private val testBusiness = BusinessEntity(
        id = "biz_test",
        profileId = "prof_1",
        name = "Warung Kopi Sejahtera",
        category = "Makanan & Minuman"
    )

    private val testProducts = listOf(
        ProductEntity("p1", "biz_test", "Kopi Susu", "KOP-01", "Minuman", 20000L, 8000L, 50, 10),
        ProductEntity("p2", "biz_test", "Roti Bakar", "ROTI-01", "Makanan", 15000L, 5000L, 2, 5), // Low stock
        ProductEntity("p3", "biz_test", "Teh Manis", "TEH-01", "Minuman", 8000L, 2000L, 30, 5) // Unsold
    )

    private val testTransactions = listOf(
        TransactionWithItems(
            transaction = TransactionEntity(
                id = "tx1",
                businessId = "biz_test",
                invoiceNumber = "INV-001",
                timestamp = System.currentTimeMillis(),
                customerName = "Budi",
                totalAmount = 55000L,
                paymentMethod = "QRIS"
            ),
            items = listOf(
                TransactionItemEntity("it1", "tx1", "p1", "Kopi Susu", 2, 20000L, 40000L),
                TransactionItemEntity("it2", "tx1", "p2", "Roti Bakar", 1, 15000L, 15000L)
            )
        )
    )

    @Test
    fun testBusinessAggregatesCalculation() {
        val agg = BusinessContextBuilder.computeAggregates(testProducts, testTransactions)
        assertEquals(55000L, agg.totalRevenue)
        assertEquals(1, agg.totalTransactions)
        assertEquals(55000L, agg.averageOrderValue)
        assertEquals("Kopi Susu", agg.topSellingProducts.first().first)
        assertEquals(2, agg.topSellingProducts.first().second)
        assertEquals(1, agg.lowStockProducts.size)
        assertEquals("Roti Bakar", agg.lowStockProducts.first().name)
        assertEquals(1, agg.unsoldProducts.size)
        assertEquals("Teh Manis", agg.unsoldProducts.first().name)
    }

    @Test
    fun testAiAssistantAnswersBestSellingProductAccurately() {
        val response = FallbackAiEngine.answerBusinessQuestion(
            question = "Produk mana yang paling laku?",
            business = testBusiness,
            products = testProducts,
            transactions = testTransactions
        )
        assertTrue(response.contains("Kopi Susu"))
        assertTrue(response.contains("2 unit"))
        assertTrue(response.contains("[FAKTA DATA]"))
        assertTrue(response.contains("[ESTIMASI & ANALISIS]"))
        assertTrue(response.contains("[REKOMENDASI AKSI]"))
    }

    @Test
    fun testAiAssistantHandlesEmptyTransactionsHonestly() {
        val response = FallbackAiEngine.answerBusinessQuestion(
            question = "Produk mana yang paling laku?",
            business = testBusiness,
            products = testProducts,
            transactions = emptyList() // No sales yet
        )
        assertTrue(response.contains("belum memiliki data transaksi"))
        assertTrue(response.contains("Rp 0"))
        assertTrue(response.contains("Buka menu **Transaksi**"))
    }

    @Test
    fun testMarketingGeneratorProducesAllRequiredSections() {
        val result = FallbackAiEngine.generateMarketing(
            productName = "Kopi Susu Gula Aren",
            category = "Minuman",
            price = 22000L,
            targetAudience = "Pekerja kantoran dan mahasiswa",
            usp = "100% espresso blend biji kopi arabika lokal dan gula aren organik",
            platform = "Instagram"
        )
        assertTrue(result.contains("INSTAGRAM CAPTION"))
        assertTrue(result.contains("DESKRIPSI PRODUK MARKETPLACE"))
        assertTrue(result.contains("TEKS PROMOSI WHATSAPP BROADCAST"))
        assertTrue(result.contains("IDE KONTEN 7 HARI"))
        assertTrue(result.contains("5 VARIASI JUDUL PROMOSI"))
        assertTrue(result.contains("22.000"))
        assertTrue(result.contains("Kopi Susu Gula Aren"))
    }

    @Test
    fun testAiAssistantPromotionRecommendationForSlowMovingProducts() {
        val response = FallbackAiEngine.answerBusinessQuestion(
            question = "Buatkan promo untuk produk penjualan rendah",
            business = testBusiness,
            products = testProducts,
            transactions = testTransactions
        )
        assertTrue(response.contains("[FAKTA DATA]"))
        assertTrue(response.contains("[ESTIMASI & ANALISIS]"))
        assertTrue(response.contains("[REKOMENDASI AKSI & DRAF PROMOSI]"))
        assertTrue(response.contains("Teh Manis") || response.contains("Roti Bakar"))
    }
}
