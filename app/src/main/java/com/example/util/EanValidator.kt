package com.example.util

object EanValidator {

    /**
     * Clean and normalize barcode/EAN string:
     * - Trims leading/trailing whitespace
     * - Removes accidental internal spaces
     * - Removes non-digit artifacts if necessary
     */
    fun cleanEan(rawInput: String?): String {
        if (rawInput.isNullOrBlank()) return ""
        return rawInput.trim().replace("\\s+".toRegex(), "")
    }

    /**
     * Checks if string consists solely of digits.
     */
    fun isDigitsOnly(input: String): Boolean {
        return input.isNotEmpty() && input.all { it.isDigit() }
    }

    /**
     * Validates if manual entry is a valid EAN-13 (or general product barcode of 8-14 digits).
     */
    fun validateManualInput(input: String): ValidationResult {
        val cleaned = cleanEan(input)
        if (cleaned.isEmpty()) {
            return ValidationResult.Error("Please enter an EAN barcode.")
        }
        if (!cleaned.all { it.isDigit() }) {
            return ValidationResult.Error("Please enter a valid EAN containing only numbers.")
        }
        if (cleaned.length < 8 || cleaned.length > 14) {
            return ValidationResult.Error("Please enter a valid EAN (standard EAN-13 has 13 digits).")
        }
        return ValidationResult.Valid(cleaned)
    }

    sealed class ValidationResult {
        data class Valid(val ean: String) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
