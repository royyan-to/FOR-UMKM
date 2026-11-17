package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Profile ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfile(id: String): ProfileEntity?

    @Query("SELECT * FROM profiles ORDER BY createdAt ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    // --- Business ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
    suspend fun getBusiness(id: String): BusinessEntity?

    @Query("SELECT * FROM businesses WHERE profileId = :profileId ORDER BY createdAt ASC")
    fun getBusinessesByProfile(profileId: String): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses ORDER BY createdAt ASC")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>

    @Query("DELETE FROM businesses WHERE id = :businessId")
    suspend fun deleteBusiness(businessId: String)

    // --- Products ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProduct(productId: String)

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE businessId = :businessId ORDER BY name ASC")
    fun getProductsByBusiness(businessId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE businessId = :businessId ORDER BY name ASC")
    suspend fun getProductsListByBusiness(businessId: String): List<ProductEntity>

    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    suspend fun updateProductStock(productId: String, newStock: Int)

    // --- Transactions & Items ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @androidx.room.Transaction
    suspend fun recordSaleTransaction(
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>
    ) {
        insertTransaction(transaction)
        insertTransactionItems(items)
        // Deduct inventory for each item
        for (item in items) {
            val product = getProductById(item.productId)
            if (product != null) {
                val updatedStock = (product.stock - item.quantity).coerceAtLeast(0)
                updateProductStock(product.id, updatedStock)
            }
        }
    }

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getTransactionsWithItems(businessId: String): Flow<List<TransactionWithItems>>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE businessId = :businessId ORDER BY timestamp DESC")
    suspend fun getTransactionsWithItemsList(businessId: String): List<TransactionWithItems>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE businessId = :businessId AND timestamp BETWEEN :startTimestamp AND :endTimestamp ORDER BY timestamp DESC")
    fun getTransactionsInDateRange(
        businessId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<List<TransactionWithItems>>

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: String)

    // --- Chat Messages ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE businessId = :businessId ORDER BY timestamp ASC")
    fun getChatMessages(businessId: String): Flow<List<ChatMessageEntity>>

    @Query("DELETE FROM chat_messages WHERE businessId = :businessId")
    suspend fun clearChatMessages(businessId: String)

    // --- AI Generations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiGeneration(generation: AiGenerationEntity)

    @Query("SELECT * FROM ai_generations WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getAiGenerations(businessId: String): Flow<List<AiGenerationEntity>>

    // --- Business Reports ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinessReport(report: BusinessReportEntity)

    @Query("SELECT * FROM business_reports WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getBusinessReports(businessId: String): Flow<List<BusinessReportEntity>>

    @Query("DELETE FROM business_reports WHERE id = :reportId")
    suspend fun deleteBusinessReport(reportId: String)

    // --- Clear All Data for Business ---
    @Query("DELETE FROM products WHERE businessId = :businessId")
    suspend fun clearProductsForBusiness(businessId: String)

    @Query("DELETE FROM transactions WHERE businessId = :businessId")
    suspend fun clearTransactionsForBusiness(businessId: String)
}
