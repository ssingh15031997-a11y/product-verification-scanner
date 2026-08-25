package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.remote.ProductService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SheetConfig(
    val apiEndpoint: String = ProductService.DEFAULT_API_ENDPOINT,
    val sheetId: String = "",
    val sheetName: String = "Label Verification Data",
    val apiKey: String = "",
    val lastSyncTime: Long = 0L,
    val lastSyncStatus: String = "Ready",
    val totalProductsCached: Int = 0
)

class SheetConfigPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sheet_config_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<SheetConfig> = _configFlow.asStateFlow()

    private fun loadConfig(): SheetConfig {
        return SheetConfig(
            apiEndpoint = prefs.getString(KEY_API_ENDPOINT, ProductService.DEFAULT_API_ENDPOINT)
                ?: ProductService.DEFAULT_API_ENDPOINT,
            sheetId = prefs.getString(KEY_SHEET_ID, "") ?: "",
            sheetName = prefs.getString(KEY_SHEET_NAME, "Label Verification Data") ?: "Label Verification Data",
            apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            lastSyncTime = prefs.getLong(KEY_LAST_SYNC_TIME, 0L),
            lastSyncStatus = prefs.getString(KEY_LAST_SYNC_STATUS, "Ready") ?: "Ready",
            totalProductsCached = prefs.getInt(KEY_TOTAL_PRODUCTS, 0)
        )
    }

    fun updateEndpoint(apiEndpoint: String) {
        prefs.edit()
            .putString(KEY_API_ENDPOINT, apiEndpoint.trim())
            .apply()
        _configFlow.value = loadConfig()
    }

    fun updateConfig(apiEndpoint: String, sheetName: String = "Label Verification Data") {
        prefs.edit()
            .putString(KEY_API_ENDPOINT, apiEndpoint.trim())
            .putString(KEY_SHEET_NAME, sheetName.trim())
            .apply()
        _configFlow.value = loadConfig()
    }

    fun updateSyncRecord(timestamp: Long, status: String, count: Int) {
        prefs.edit()
            .putLong(KEY_LAST_SYNC_TIME, timestamp)
            .putString(KEY_LAST_SYNC_STATUS, status)
            .putInt(KEY_TOTAL_PRODUCTS, count)
            .apply()
        _configFlow.value = loadConfig()
    }

    companion object {
        private const val KEY_API_ENDPOINT = "pref_api_endpoint"
        private const val KEY_SHEET_ID = "pref_sheet_id"
        private const val KEY_SHEET_NAME = "pref_sheet_name"
        private const val KEY_API_KEY = "pref_api_key"
        private const val KEY_LAST_SYNC_TIME = "pref_last_sync_time"
        private const val KEY_LAST_SYNC_STATUS = "pref_last_sync_status"
        private const val KEY_TOTAL_PRODUCTS = "pref_total_products"
    }
}
