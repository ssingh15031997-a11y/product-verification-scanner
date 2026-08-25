package com.example.util

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // EAN-13, EAN-8, UPC-A, UPC-E, CODE-128, QR etc.
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @Volatile
    var isEnabled: Boolean = true

    private var lastScannedValue: String = ""
    private var lastScannedTimestamp: Long = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isEnabled) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (!isEnabled) return@addOnSuccessListener
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue ?: continue
                        val cleaned = EanValidator.cleanEan(rawValue)
                        if (cleaned.isNotEmpty()) {
                            val now = System.currentTimeMillis()
                            // Debounce duplicate reads within 1.5 seconds
                            if (cleaned != lastScannedValue || (now - lastScannedTimestamp) > 1500) {
                                lastScannedValue = cleaned
                                lastScannedTimestamp = now
                                onBarcodeDetected(cleaned)
                                break
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    // ignore frame processing errors
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
