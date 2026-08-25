package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE ean = :ean")
    fun observeProductsByEan(ean: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE ean = :ean")
    suspend fun findProductsByEan(ean: String): List<ProductEntity>

    @Query("SELECT * FROM products")
    fun observeAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(products: List<ProductEntity>) {
        clearAll()
        insertAll(products)
    }
}
