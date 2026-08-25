package com.example.ui.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.IndustrialCyan500
import com.example.ui.theme.IndustrialNavy900
import com.example.ui.theme.SignalErrorRed
import com.example.util.BarcodeAnalyzer
import java.util.concurrent.Executors

@Composable
fun CameraScannerView(
    isTorchEnabled: Boolean,
    onBarcodeDetected: (String) -> Unit,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraPreviewContent(
            isTorchEnabled = isTorchEnabled,
            onBarcodeDetected = onBarcodeDetected,
            onToggleTorch = onToggleTorch,
            modifier = modifier
        )
    } else {
        CameraPermissionFallback(
            onRequestPermission = { launcher.launch(Manifest.permission.CAMERA) },
            modifier = modifier
        )
    }
}

@Composable
private fun CameraPreviewContent(
    isTorchEnabled: Boolean,
    onBarcodeDetected: (String) -> Unit,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val barcodeAnalyzer = remember {
        BarcodeAnalyzer { barcode ->
            onBarcodeDetected(barcode)
        }
    }

    LaunchedEffect(isTorchEnabled, camera) {
        try {
            camera?.cameraControl?.enableTorch(isTorchEnabled)
        } catch (_: Exception) {
            // Torch might not be supported on all devices/emulators
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            barcodeAnalyzer.isEnabled = false
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .testTag("camera_scanner_view")
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, barcodeAnalyzer)
                            }

                        val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        } else {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        }

                        cameraProvider.unbindAll()
                        val boundCamera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        camera = boundCamera
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Laser Viewfinder Overlay
        ScannerLaserOverlay(modifier = Modifier.fillMaxSize())

        // Top Controls Bar over camera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                color = IndustrialNavy900.copy(alpha = 0.75f),
                shape = RoundedCornerShape(20.dp)
            ) {
                IconButton(
                    onClick = onToggleTorch,
                    modifier = Modifier.testTag("torch_button")
                ) {
                    Icon(
                        imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flashlight Toggle",
                        tint = if (isTorchEnabled) IndustrialCyan500 else Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannerLaserOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val boxWidth = width * 0.82f
        val boxHeight = height * 0.55f
        val boxLeft = (width - boxWidth) / 2f
        val boxTop = (height - boxHeight) / 2f

        val cornerLength = 32.dp.toPx()
        val cornerStroke = 4.dp.toPx()
        val cornerColor = Color(0xFF00B4D8)

        // Darkened Scrim outside the viewfinder box
        drawRect(
            color = Color(0x77000000)
        )

        // Clear inside
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(12.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // Viewfinder Frame Outline
        drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(12.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 4 Corner Targeting Brackets
        // Top-Left
        drawLine(
            color = cornerColor,
            start = Offset(boxLeft, boxTop),
            end = Offset(boxLeft + cornerLength, boxTop),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = cornerColor,
            start = Offset(boxLeft, boxTop),
            end = Offset(boxLeft, boxTop + cornerLength),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )

        // Top-Right
        drawLine(
            color = cornerColor,
            start = Offset(boxLeft + boxWidth, boxTop),
            end = Offset(boxLeft + boxWidth - cornerLength, boxTop),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = cornerColor,
            start = Offset(boxLeft + boxWidth, boxTop),
            end = Offset(boxLeft + boxWidth, boxTop + cornerLength),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )

        // Bottom-Left
        drawLine(
            color = cornerColor,
            start = Offset(boxLeft, boxTop + boxHeight),
            end = Offset(boxLeft + cornerLength, boxTop + boxHeight),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = cornerColor,
            start = Offset(boxLeft, boxTop + boxHeight),
            end = Offset(boxLeft, boxTop + boxHeight - cornerLength),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )

        // Bottom-Right
        drawLine(
            color = cornerColor,
            start = Offset(boxLeft + boxWidth, boxTop + boxHeight),
            end = Offset(boxLeft + boxWidth - cornerLength, boxTop + boxHeight),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = cornerColor,
            start = Offset(boxLeft + boxWidth, boxTop + boxHeight),
            end = Offset(boxLeft + boxWidth, boxTop + boxHeight - cornerLength),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )

        // Laser Beam
        val laserY = boxTop + (boxHeight * laserProgress)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x00FF3366),
                    Color(0xEEFF0033),
                    Color(0x00FF3366)
                ),
                startY = laserY - 6.dp.toPx(),
                endY = laserY + 6.dp.toPx()
            ),
            topLeft = Offset(boxLeft + 4.dp.toPx(), laserY - 3.dp.toPx()),
            size = Size(boxWidth - 8.dp.toPx(), 6.dp.toPx())
        )
    }
}

@Composable
private fun CameraPermissionFallback(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = IndustrialNavy900),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("camera_permission_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.VideocamOff,
                contentDescription = "Camera Required",
                tint = SignalErrorRed,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Camera permission is required to scan EAN barcodes on products. You can also enter the EAN manually below.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("grant_camera_button")
            ) {
                Text(
                    text = "TRY AGAIN",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
