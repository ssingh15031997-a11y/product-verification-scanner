package com.example.ui.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SheetConfig
import com.example.ui.ScannerUiState
import com.example.ui.components.AppTopBar
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.UtilityBg
import com.example.ui.theme.UtilityGreen
import com.example.ui.theme.UtilityNavy
import com.example.ui.theme.UtilityRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScannerScreen(
    userId: String,
    state: ScannerUiState,
    sheetConfig: SheetConfig,
    onBarcodeScanned: (String) -> Unit,
    onManualInputChanged: (String) -> Unit,
    onManualSearchSubmitted: () -> Unit,
    onToggleTorch: () -> Unit,
    onRefreshData: () -> Unit,
    onShowHistory: () -> Unit,
    onShowSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardWedgeBuffer = remember { StringBuilder() }

    Scaffold(
        topBar = {
            AppTopBar(
                userId = userId,
                isSyncing = state.isSyncing,
                onRefreshClicked = onRefreshData,
                onHistoryClicked = onShowHistory,
                onSettingsClicked = onShowSettings,
                onLogoutClicked = onLogout
            )
        },
        modifier = modifier
            .fillMaxSize()
            // Keyboard Wedge Listener for USB/Bluetooth hardware scanners
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    if (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) {
                        val scannedText = keyboardWedgeBuffer.toString().trim()
                        if (scannedText.isNotEmpty()) {
                            keyboardWedgeBuffer.clear()
                            onBarcodeScanned(scannedText)
                            return@onPreviewKeyEvent true
                        }
                    } else {
                        val char = keyEvent.utf16CodePoint.toChar()
                        if (char.isDigit()) {
                            keyboardWedgeBuffer.append(char)
                        }
                    }
                }
                false
            }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(UtilityBg)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sync / Master Data Status Bar
                DataStatusBar(
                    sheetConfig = sheetConfig,
                    isSyncing = state.isSyncing,
                    modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Scanner Container Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .testTag("scanner_main_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title Header
                        Text(
                            text = "SCAN EAN BARCODE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = UtilityNavy
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Camera Viewfinder Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.25f)
                                .widthIn(max = 480.dp)
                        ) {
                            CameraScannerView(
                                isTorchEnabled = state.isTorchEnabled,
                                onBarcodeDetected = onBarcodeScanned,
                                onToggleTorch = onToggleTorch,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Loading overlay when searching barcode
                            if (state.isSearching) {
                                Surface(
                                    color = UtilityNavy.copy(alpha = 0.88f),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "LOOKING UP EAN...",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Connecting to Apps Script API...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate400
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Point camera at the product packaging barcode",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Slate400,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // "OR" Divider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Slate200)
                            Text(
                                text = "  OR  ",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = Slate400
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Slate200)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Manual EAN Input Section
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ENTER EAN MANUALLY",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Slate400
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = state.manualEanInput,
                                onValueChange = onManualInputChanged,
                                placeholder = { Text("e.g. 8906202671265", color = Slate400) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Keyboard,
                                        contentDescription = "Manual Keyboard",
                                        tint = UtilityNavy
                                    )
                                },
                                trailingIcon = {
                                    if (state.manualEanInput.isNotEmpty()) {
                                        IconButton(onClick = { onManualInputChanged("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear Input",
                                                tint = Slate400
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        focusManager.clearFocus()
                                        onManualSearchSubmitted()
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = UtilityNavy,
                                    unfocusedBorderColor = Slate200,
                                    focusedContainerColor = Slate50,
                                    unfocusedContainerColor = Slate50
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("manual_ean_input")
                            )

                            // Manual Input Error
                            AnimatedVisibility(
                                visible = state.manualInputError != null,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = state.manualInputError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = UtilityRed,
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    onManualSearchSubmitted()
                                },
                                enabled = !state.isSearching && state.manualEanInput.isNotBlank(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UtilityNavy,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("manual_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SEARCH PRODUCT",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Demo Sample EANs for fast operator evaluation
                DemoEanChips(
                    onEanSelected = { ean ->
                        onBarcodeScanned(ean)
                    },
                    modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)
                )
            }
        }
    }
}

@Composable
private fun DataStatusBar(
    sheetConfig: SheetConfig,
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = UtilityGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Apps Script API: Connected",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = UtilityNavy
                    )
                    if (sheetConfig.lastSyncTime > 0) {
                        val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        Text(
                            text = "Status: ${sheetConfig.lastSyncStatus} (${timeFormat.format(Date(sheetConfig.lastSyncTime))})",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Slate400
                        )
                    }
                }
            }

            if (isSyncing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = UtilityNavy,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Checking...",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = UtilityNavy
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoEanChips(
    onEanSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = UtilityNavy,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Quick Test Sample Barcodes:",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Black
                SampleChip(
                    label = "8906202671265 (Black)",
                    onClick = { onEanSelected("8906202671265") },
                    modifier = Modifier.weight(1f)
                )
                // Blue
                SampleChip(
                    label = "8906202671272 (Blue)",
                    onClick = { onEanSelected("8906202671272") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Green
                SampleChip(
                    label = "8906202671289 (Green)",
                    onClick = { onEanSelected("8906202671289") },
                    modifier = Modifier.weight(1f)
                )
                // Duplicate sample
                SampleChip(
                    label = "8906202671296 (Duplicate)",
                    onClick = { onEanSelected("8906202671296") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SampleChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Slate100,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Slate800,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
        )
    }
}
