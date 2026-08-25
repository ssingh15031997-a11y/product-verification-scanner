package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ScanLog
import com.example.data.model.ScanResultType

@Entity(tableName = "scan_logs")
data class ScanLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val scannedEan: String,
    val resultType: String,
    val model: String? = null,
    val color: String? = null,
    val memory: String? = null,
    val sku: String? = null
) {
    fun toScanLog(): ScanLog = ScanLog(
        id = id,
        timestamp = timestamp,
        userId = userId,
        scannedEan = scannedEan,
        resultType = runCatching { ScanResultType.valueOf(resultType) }.getOrDefault(ScanResultType.FOUND),
        model = model,
        color = color,
        memory = memory,
        sku = sku
    )

    companion object {
        fun fromScanLog(log: ScanLog): ScanLogEntity = ScanLogEntity(
            timestamp = log.timestamp,
            userId = log.userId,
            scannedEan = log.scannedEan,
            resultType = log.resultType.name,
            model = log.model,
            color = log.color,
            memory = log.memory,
            sku = log.sku
        )
    }
}
