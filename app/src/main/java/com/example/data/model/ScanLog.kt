package com.example.data.model

enum class ScanResultType {
    FOUND,
    NOT_FOUND,
    DUPLICATE
}

data class ScanLog(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val scannedEan: String,
    val resultType: ScanResultType,
    val model: String? = null,
    val color: String? = null,
    val memory: String? = null,
    val sku: String? = null
)
