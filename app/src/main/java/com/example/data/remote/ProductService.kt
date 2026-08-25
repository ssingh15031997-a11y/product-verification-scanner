package com.example.data.remote

import com.example.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

sealed class ProductApiResult {
    data class Success(
        val found: Boolean,
        val duplicate: Boolean,
        val count: Int,
        val products: List<Product>
    ) : ProductApiResult()

    data class Failure(
        val errorMessage: String,
        val throwable: Throwable? = null
    ) : ProductApiResult()
}

class ProductService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {
    companion object {
        const val DEFAULT_API_ENDPOINT =
            "https://script.google.com/macros/s/AKfycbwIh8nemekW3RTdVhxyocrIrd8jjKRdjQXIGM5iQs4guuUYu8g4nxKrokg8f_UA8molnA/exec"
    }

    suspend fun lookupByEan(
        ean: String,
        endpoint: String = DEFAULT_API_ENDPOINT
    ): ProductApiResult = withContext(Dispatchers.IO) {
        val trimmedEan = ean.trim()
        if (trimmedEan.isEmpty()) {
            return@withContext ProductApiResult.Failure("EAN barcode is empty")
        }

        val baseUrl = endpoint.trim().ifEmpty { DEFAULT_API_ENDPOINT }

        try {
            val encodedEan = URLEncoder.encode(trimmedEan, "UTF-8")
            val targetUrl = if (baseUrl.contains("?")) {
                "$baseUrl&ean=$encodedEan"
            } else {
                "$baseUrl?ean=$encodedEan"
            }

            val request = Request.Builder()
                .url(targetUrl)
                .get()
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ProductApiResult.Failure("API Error: HTTP ${response.code} ${response.message}")
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrBlank()) {
                    return@withContext ProductApiResult.Failure("Received empty response from API")
                }

                try {
                    val root = JSONObject(responseBody)
                    val success = root.optBoolean("success", true)
                    val found = root.optBoolean("found", false)
                    val duplicate = root.optBoolean("duplicate", false)
                    val count = root.optInt("count", 0)

                    val productsList = mutableListOf<Product>()
                    val productsArray = root.optJSONArray("products")
                    if (productsArray != null) {
                        for (i in 0 until productsArray.length()) {
                            val obj = productsArray.optJSONObject(i) ?: continue
                            val id = obj.opt("id")?.toString()?.trim() ?: (i + 1).toString()
                            val model = obj.optString("model", "").trim()
                            val color = obj.optString("color", "").trim()
                            val memory = obj.optString("memory", "").trim()
                            val productEan = obj.optString("ean", "").trim()
                            val sku = obj.optString("sku", "").trim()
                            val price = obj.opt("price")?.toString()?.trim() ?: ""
                            val sarValue = when {
                                obj.has("sarValue") -> obj.opt("sarValue")?.toString()?.trim() ?: ""
                                obj.has("sar_value") -> obj.opt("sar_value")?.toString()?.trim() ?: ""
                                obj.has("sar") -> obj.opt("sar")?.toString()?.trim() ?: ""
                                obj.has("SAR Value") -> obj.opt("SAR Value")?.toString()?.trim() ?: ""
                                else -> obj.optString("sarValue", "").trim()
                            }

                            productsList.add(
                                Product(
                                    id = id,
                                    model = model,
                                    color = color,
                                    memory = memory,
                                    ean = productEan.ifEmpty { trimmedEan },
                                    sku = sku,
                                    price = price,
                                    sarValue = sarValue
                                )
                            )
                        }
                    }

                    if (!success && !found && productsList.isEmpty()) {
                        val message = root.optString("message", "Product lookup returned unsuccessful")
                        return@withContext ProductApiResult.Failure(message)
                    }

                    ProductApiResult.Success(
                        found = found,
                        duplicate = duplicate,
                        count = if (count > 0) count else productsList.size,
                        products = productsList
                    )
                } catch (jsonEx: Exception) {
                    ProductApiResult.Failure("Failed to parse API response: ${jsonEx.localizedMessage}", jsonEx)
                }
            }
        } catch (e: Exception) {
            ProductApiResult.Failure(
                e.localizedMessage ?: "Failed to connect to Apps Script API",
                e
            )
        }
    }
}
