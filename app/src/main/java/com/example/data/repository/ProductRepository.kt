package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ProductEntity
import com.example.data.local.ScanLogEntity
import com.example.data.local.SheetConfig
import com.example.data.local.SheetConfigPreferences
import com.example.data.model.Product
import com.example.data.model.ScanLog
import com.example.data.model.ScanResultType
import com.example.data.remote.ProductApiResult
import com.example.data.remote.ProductService
import com.example.util.EanValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

sealed class SearchResult {
    data class Found(val product: Product) : SearchResult()
    data class Duplicate(val ean: String, val products: List<Product>) : SearchResult()
    data class NotFound(val ean: String) : SearchResult()
    data class Error(val message: String) : SearchResult()
}

class ProductRepository(
    private val database: AppDatabase,
    private val productService: ProductService,
    private val configPreferences: SheetConfigPreferences,
    private val authRepository: AuthRepository
) {

    val configFlow: StateFlow<SheetConfig> = configPreferences.configFlow

    val recentScanLogs: Flow<List<ScanLog>> = database.scanLogDao()
        .observeRecentLogs(50)
        .map { list -> list.map { it.toScanLog() } }

    /**
     * Initializes database or sync state.
     */
    suspend fun ensureInitialDataLoaded() = withContext(Dispatchers.IO) {
        val cachedCount = database.productDao().getProductCount()
        configPreferences.updateSyncRecord(
            timestamp = System.currentTimeMillis(),
            status = "Apps Script API Connected",
            count = cachedCount
        )
    }

    /**
     * Look up product by EAN using Google Apps Script Web App API.
     */
    suspend fun searchByEan(rawEan: String): SearchResult = withContext(Dispatchers.IO) {
        val cleanEan = EanValidator.cleanEan(rawEan)
        if (cleanEan.isEmpty()) {
            return@withContext SearchResult.Error("EAN barcode cannot be empty")
        }

        val endpoint = configPreferences.configFlow.value.apiEndpoint
        val currentUser = authRepository.currentUser.value.userId.ifEmpty { "operator" }

        // Call Google Apps Script Web App API
        val apiResult: ProductApiResult = productService.lookupByEan(cleanEan, endpoint)
        when (apiResult) {
            is ProductApiResult.Success -> {
                when {
                    apiResult.found && !apiResult.duplicate && apiResult.products.isNotEmpty() -> {
                        val product = apiResult.products.first()
                        recordScan(
                            userId = currentUser,
                            scannedEan = cleanEan,
                            resultType = ScanResultType.FOUND,
                            product = product
                        )
                        // Save/cache product entity
                        try {
                            database.productDao().insert(ProductEntity.fromProduct(product))
                        } catch (e: Exception) {
                            // non-blocking
                        }
                        SearchResult.Found(product)
                    }
                    apiResult.found && apiResult.duplicate -> {
                        val products = apiResult.products
                        recordScan(
                            userId = currentUser,
                            scannedEan = cleanEan,
                            resultType = ScanResultType.DUPLICATE,
                            product = products.firstOrNull()
                        )
                        // Save/cache duplicate product entities
                        try {
                            database.productDao().insertAll(products.map { ProductEntity.fromProduct(it) })
                        } catch (e: Exception) {
                            // non-blocking
                        }
                        SearchResult.Duplicate(cleanEan, products)
                    }
                    else -> {
                        // Not found
                        recordScan(
                            userId = currentUser,
                            scannedEan = cleanEan,
                            resultType = ScanResultType.NOT_FOUND,
                            product = null
                        )
                        SearchResult.NotFound(cleanEan)
                    }
                }
            }
            is ProductApiResult.Failure -> {
                // API Request failed (Network error, timeout, non-200 response, or invalid JSON)
                // Check if local cache has this product as an offline fallback
                val cachedMatches: List<Product> = try {
                    database.productDao().findProductsByEan(cleanEan).map { it.toProduct() }
                } catch (e: Exception) {
                    emptyList()
                }

                if (cachedMatches.size == 1) {
                    val cached = cachedMatches.first()
                    recordScan(
                        userId = currentUser,
                        scannedEan = cleanEan,
                        resultType = ScanResultType.FOUND,
                        product = cached
                    )
                    SearchResult.Found(cached)
                } else if (cachedMatches.size > 1) {
                    recordScan(
                        userId = currentUser,
                        scannedEan = cleanEan,
                        resultType = ScanResultType.DUPLICATE,
                        product = cachedMatches.first()
                    )
                    SearchResult.Duplicate(cleanEan, cachedMatches)
                } else {
                    SearchResult.Error(apiResult.errorMessage)
                }
            }
        }
    }

    /**
     * Tests API connection with sample EAN or refreshes API status.
     */
    suspend fun testApiConnection(): Result<String> = withContext(Dispatchers.IO) {
        val endpoint = configPreferences.configFlow.value.apiEndpoint
        // Test query with standard known EAN
        val testEan = "8906202671265"
        when (val result = productService.lookupByEan(testEan, endpoint)) {
            is ProductApiResult.Success -> {
                val now = System.currentTimeMillis()
                val totalCount = database.productDao().getProductCount()
                configPreferences.updateSyncRecord(
                    timestamp = now,
                    status = "Apps Script API Connected",
                    count = totalCount
                )
                Result.success("API Connected: Google Apps Script Web App responding")
            }
            is ProductApiResult.Failure -> {
                configPreferences.updateSyncRecord(
                    timestamp = System.currentTimeMillis(),
                    status = "API Connection Error: ${result.errorMessage}",
                    count = database.productDao().getProductCount()
                )
                Result.failure(Exception(result.errorMessage))
            }
        }
    }

    suspend fun updateApiEndpoint(newEndpoint: String) {
        configPreferences.updateEndpoint(newEndpoint)
    }

    private suspend fun recordScan(
        userId: String,
        scannedEan: String,
        resultType: ScanResultType,
        product: Product?
    ) {
        try {
            val logEntity = ScanLogEntity(
                userId = userId,
                scannedEan = scannedEan,
                resultType = resultType.name,
                model = product?.model,
                color = product?.color,
                memory = product?.memory,
                sku = product?.sku
            )
            database.scanLogDao().insertLog(logEntity)
        } catch (e: Exception) {
            // non-blocking
        }
    }
}
