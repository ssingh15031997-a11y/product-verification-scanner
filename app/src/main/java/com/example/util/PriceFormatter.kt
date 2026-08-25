package com.example.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object PriceFormatter {

    /**
     * Formats raw price string (e.g. "29999", "29,999", "₹29,999") into standard "₹29,999".
     */
    fun format(priceRaw: String?): String {
        if (priceRaw.isNullOrBlank()) return "₹0"
        val trimmed = priceRaw.trim()
        if (trimmed.startsWith("₹")) return trimmed

        // Extract numeric part
        val cleanNumber = trimmed.replace(",", "").replace(" ", "").replace("Rs.", "", ignoreCase = true).trim()
        val numericValue = cleanNumber.toDoubleOrNull() ?: return "₹$trimmed"

        return try {
            val locale = Locale.Builder().setLanguage("en").setRegion("IN").build()
            val formatter = NumberFormat.getCurrencyInstance(locale) as DecimalFormat
            val dfs = formatter.decimalFormatSymbols
            dfs.currencySymbol = "₹"
            formatter.decimalFormatSymbols = dfs
            formatter.maximumFractionDigits = if (numericValue % 1.0 == 0.0) 0 else 2
            formatter.format(numericValue)
        } catch (_: Exception) {
            "₹$cleanNumber"
        }
    }
}
