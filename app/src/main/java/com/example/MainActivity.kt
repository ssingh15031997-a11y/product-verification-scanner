package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.ScreenState
import com.example.ui.details.DuplicateEanScreen
import com.example.ui.details.ProductDetailsScreen
import com.example.ui.details.ProductNotFoundScreen
import com.example.ui.history.ScanHistoryDialog
import com.example.ui.login.LoginScreen
import com.example.ui.scanner.ScannerScreen
import com.example.ui.settings.SheetConfigDialog
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ProductVerificationApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ProductVerificationApp(viewModel: MainViewModel) {
    val screenState by viewModel.screenState.collectAsState()
    val loginState by viewModel.loginState.collectAsState()
    val scannerState by viewModel.scannerState.collectAsState()
    val authUser by viewModel.currentUser.collectAsState()
    val sheetConfig by viewModel.sheetConfig.collectAsState()
    val recentLogs by viewModel.recentScanLogs.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AnimatedContent(
            targetState = screenState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition",
            modifier = Modifier.padding(innerPadding)
        ) { targetState ->
            when (targetState) {
                is ScreenState.Login -> {
                    LoginScreen(
                        state = loginState,
                        onUserIdChanged = viewModel::onUserIdChanged,
                        onPasswordChanged = viewModel::onPasswordChanged,
                        onTogglePasswordMask = viewModel::togglePasswordVisibility,
                        onLoginSubmitted = viewModel::login,
                        onFillDemoCredentials = viewModel::fillDemoCredentials
                    )
                }
                is ScreenState.Scanner -> {
                    ScannerScreen(
                        userId = authUser.userId,
                        state = scannerState,
                        sheetConfig = sheetConfig,
                        onBarcodeScanned = viewModel::onBarcodeScanned,
                        onManualInputChanged = viewModel::onManualInputChanged,
                        onManualSearchSubmitted = viewModel::onManualSearchSubmitted,
                        onToggleTorch = viewModel::toggleTorch,
                        onRefreshData = viewModel::refreshData,
                        onShowHistory = viewModel::showHistoryDialog,
                        onShowSettings = viewModel::showSettingsDialog,
                        onLogout = viewModel::logout
                    )
                }
                is ScreenState.ProductDetails -> {
                    ProductDetailsScreen(
                        product = targetState.product,
                        onScanNext = viewModel::onScanNext,
                        onRefreshMaster = viewModel::refreshData
                    )
                }
                is ScreenState.ProductNotFound -> {
                    ProductNotFoundScreen(
                        ean = targetState.ean,
                        onScanAgain = viewModel::onScanNext,
                        onEnterManually = viewModel::onBackToScanner
                    )
                }
                is ScreenState.DuplicateFound -> {
                    DuplicateEanScreen(
                        ean = targetState.ean,
                        products = targetState.products,
                        onSelectProduct = viewModel::onSelectDuplicateProduct,
                        onScanNext = viewModel::onScanNext,
                        onBackToScanner = viewModel::onBackToScanner
                    )
                }
            }
        }

        // Scan History Dialog
        if (scannerState.showHistoryDialog) {
            ScanHistoryDialog(
                logs = recentLogs,
                onDismiss = viewModel::hideHistoryDialog
            )
        }

        // Apps Script API Connection Dialog
        if (scannerState.showSettingsDialog) {
            SheetConfigDialog(
                currentConfig = sheetConfig,
                onSaveConfig = viewModel::updateApiEndpoint,
                onDismiss = viewModel::hideSettingsDialog
            )
        }
    }
}
