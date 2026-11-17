package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Embedded
import androidx.room.Relation

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String = "Owner",
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "businesses",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class BusinessEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val name: String,
    val category: String, // e.g. "Makanan & Minuman", "Retail / Toko Kelontong", "Fashion", "Jasa"
    val currency: String = "IDR",
    val address: String = "",
    val phone: String = "",
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("businessId")]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val name: String,
    val sku: String = "",
    val category: String = "Umum",
    val price: Long, // in IDR Rupiah
    val costPrice: Long = 0L, // modal/HPP
    val stock: Int = 0,
    val minStock: Int = 5,
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("businessId"), Index("timestamp")]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customerName: String = "Pelanggan Umum",
    val totalAmount: Long, // in IDR
    val paymentMethod: String = "Tunai", // "Tunai", "QRIS", "Transfer Bank"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId"), Index("productId")]
)
data class TransactionItemEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Long,
    val subtotal: Long
)

data class TransactionWithItems(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val items: List<TransactionItemEntity>
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("businessId"), Index("timestamp")]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val sender: String, // "user" or "assistant"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val hasBusinessContext: Boolean = true
)

@Entity(
    tableName = "ai_generations",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("businessId")]
)
data class AiGenerationEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val featureType: String, // "MARKETING_CAPTION", "MARKETING_WA", "MARKETING_MARKETPLACE", "MARKETING_CALENDAR", "SALES_ANALYSIS"
    val prompt: String,
    val resultContent: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "business_reports",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("businessId")]
)
data class BusinessReportEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val title: String,
    val period: String, // "7_DAYS", "30_DAYS", "ALL"
    val summary: String,
    val topProductsJson: String,
    val findingsJson: String,
    val recommendationsJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
