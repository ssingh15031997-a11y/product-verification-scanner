package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Product

@Entity(
    tableName = "products",
    indices = [Index(value = ["ean"])]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val sheetId: String,
    val model: String,
    val color: String,
    val memory: String,
    val ean: String,
    val sku: String,
    val price: String,
    val sarValue: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toProduct(): Product = Product(
        id = sheetId,
        model = model,
        color = color,
        memory = memory,
        ean = ean,
        sku = sku,
        price = price,
        sarValue = sarValue
    )

    companion object {
        fun fromProduct(product: Product): ProductEntity = ProductEntity(
            sheetId = product.id,
            model = product.model,
            color = product.color,
            memory = product.memory,
            ean = product.ean.trim().replace(" ", ""),
            sku = product.sku,
            price = product.price,
            sarValue = product.sarValue
        )
    }
}
