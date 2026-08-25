package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SheetConfig
import com.example.data.local.SheetConfigPreferences
import com.example.data.model.Product
import com.example.data.model.ScanLog
import com.example.data.remote.ProductService
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthUser
import com.example.data.repository.ProductRepository
import com.example.data.repository.SearchResult
import com.example.util.BeepManager
import com.example.util.EanValidator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenState {
    data object Login : ScreenState()
    data object Scanner : ScreenState()
    data class ProductDetails(val product: Product) : ScreenState()
    data class ProductNotFound(val ean: String) : ScreenState()
    data class DuplicateFound(val ean: String, val products: List<Product>) : ScreenState()
}

data class LoginUiState(
    val userIdInput: String = "",
    val passwordInput: String = "",
    val isPasswordMasked: Boolean = true,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

data class ScannerUiState(
    val isSearching: Boolean = false,
    val isSyncing: Boolean = false,
    val isTorchEnabled: Boolean = false,
    val manualEanInput: String = "",
    val manualInputError: String? = null,
    val syncStatusMessage: String? = null,
    val showHistoryDialog: Boolean = false,
    val showSettingsDialog: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val productService = ProductService()
    private val configPreferences = SheetConfigPreferences(application)
    private val authRepository = AuthRepository(application)
    private val productRepository = ProductRepository(database, productService, configPreferences, authRepository)
    private val beepManager = BeepManager(application)

    val currentUser: StateFlow<AuthUser> = authRepository.currentUser
    val sheetConfig: StateFlow<SheetConfig> = productRepository.configFlow
    val recentScanLogs: StateFlow<List<ScanLog>> = productRepository.recentScanLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _screenState = MutableStateFlow<ScreenState>(
        if (authRepository.currentUser.value.isLoggedIn) ScreenState.Scanner else ScreenState.Login
    )
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _scannerState = MutableStateFlow(ScannerUiState())
    val scannerState: StateFlow<ScannerUiState> = _scannerState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private var searchJob: kotlinx.coroutines.Job? = null
    private var currentSearchRequestId: Long = 0L

    init {
        viewModelScope.launch {
            productRepository.ensureInitialDataLoaded()
        }
    }

    // --- Authentication ---

    fun onUserIdChanged(value: String) {
        _loginState.value = _loginState.value.copy(
            userIdInput = value,
            errorMessage = null
        )
    }

    fun onPasswordChanged(value: String) {
        _loginState.value = _loginState.value.copy(
            passwordInput = value,
            errorMessage = null
        )
    }

    fun togglePasswordVisibility() {
        _loginState.value = _loginState.value.copy(
            isPasswordMasked = !_loginState.value.isPasswordMasked
        )
    }

    fun fillDemoCredentials() {
        _loginState.value = _loginState.value.copy(
            userIdInput = "sanjay2007",
            passwordInput = "Sanjay@2007",
            errorMessage = null
        )
    }

    fun login() {
        val userId = _loginState.value.userIdInput
        val password = _loginState.value.passwordInput

        if (userId.isBlank() || password.isBlank()) {
            _loginState.value = _loginState.value.copy(
                errorMessage = "Invalid User ID or Password"
            )
            return
        }

        val success = authRepository.login(userId, password)
        if (success) {
            _loginState.value = LoginUiState()
            _screenState.value = ScreenState.Scanner
        } else {
            _loginState.value = _loginState.value.copy(
                errorMessage = "Invalid User ID or Password"
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _loginState.value = LoginUiState()
        _screenState.value = ScreenState.Login
    }

    // --- Scanner & Verification ---

    fun onBarcodeScanned(rawEan: String) {
        performEanSearch(rawEan)
    }

    fun onManualInputChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        _scannerState.value = _scannerState.value.copy(
            manualEanInput = filtered,
            manualInputError = null
        )
    }

    fun onManualSearchSubmitted() {
        val input = _scannerState.value.manualEanInput
        when (val validation = EanValidator.validateManualInput(input)) {
            is EanValidator.ValidationResult.Error -> {
                _scannerState.value = _scannerState.value.copy(
                    manualInputError = validation.message
                )
                beepManager.playErrorBeep()
            }
            is EanValidator.ValidationResult.Valid -> {
                performEanSearch(validation.ean)
            }
        }
    }

    private fun performEanSearch(ean: String) {
        // Cancel any pending search to prevent race conditions
        searchJob?.cancel()
        val requestId = ++currentSearchRequestId

        // Clear previous product state and show searching state
        _screenState.value = ScreenState.Scanner
        _scannerState.value = _scannerState.value.copy(
            isSearching = true,
            manualInputError = null
        )

        searchJob = viewModelScope.launch {
            try {
                when (val result = productRepository.searchByEan(ean)) {
                    is SearchResult.Found -> {
                        if (requestId == currentSearchRequestId) {
                            beepManager.playSuccessBeep()
                            // Bind entire fresh product object from API response
                            _screenState.value = ScreenState.ProductDetails(result.product)
                        }
                    }
                    is SearchResult.NotFound -> {
                        if (requestId == currentSearchRequestId) {
                            beepManager.playWarningBeep()
                            _screenState.value = ScreenState.ProductNotFound(result.ean)
                        }
                    }
                    is SearchResult.Duplicate -> {
                        if (requestId == currentSearchRequestId) {
                            beepManager.playWarningBeep()
                            _screenState.value = ScreenState.DuplicateFound(result.ean, result.products)
                        }
                    }
                    is SearchResult.Error -> {
                        if (requestId == currentSearchRequestId) {
                            beepManager.playErrorBeep()
                            _snackbarEvent.emit(result.message)
                        }
                    }
                }
            } finally {
                if (requestId == currentSearchRequestId) {
                    _scannerState.value = _scannerState.value.copy(
                        isSearching = false,
                        manualEanInput = ""
                    )
                }
            }
        }
    }

    fun onScanNext() {
        searchJob?.cancel()
        _screenState.value = ScreenState.Scanner
        _scannerState.value = _scannerState.value.copy(
            isSearching = false,
            manualEanInput = "",
            manualInputError = null
        )
    }

    fun onBackToScanner() {
        _screenState.value = ScreenState.Scanner
    }

    fun onSelectDuplicateProduct(product: Product) {
        _screenState.value = ScreenState.ProductDetails(product)
    }

    fun toggleTorch() {
        _scannerState.value = _scannerState.value.copy(
            isTorchEnabled = !_scannerState.value.isTorchEnabled
        )
    }

    // --- Apps Script API Connection & Settings ---

    fun refreshData() {
        viewModelScope.launch {
            _scannerState.value = _scannerState.value.copy(isSyncing = true)
            val result = productRepository.testApiConnection()
            result.fold(
                onSuccess = { msg ->
                    beepManager.playSuccessBeep()
                    _scannerState.value = _scannerState.value.copy(
                        isSyncing = false,
                        syncStatusMessage = msg
                    )
                    _snackbarEvent.emit(msg)
                },
                onFailure = { error ->
                    beepManager.playErrorBeep()
                    val errorMsg = "API Connection Error: ${error.localizedMessage}"
                    _scannerState.value = _scannerState.value.copy(
                        isSyncing = false,
                        syncStatusMessage = errorMsg
                    )
                    _snackbarEvent.emit(errorMsg)
                }
            )
        }
    }

    fun updateApiEndpoint(apiEndpoint: String) {
        viewModelScope.launch {
            productRepository.updateApiEndpoint(apiEndpoint)
            hideSettingsDialog()
            refreshData()
        }
    }

    fun showHistoryDialog() {
        _scannerState.value = _scannerState.value.copy(showHistoryDialog = true)
    }

    fun hideHistoryDialog() {
        _scannerState.value = _scannerState.value.copy(showHistoryDialog = false)
    }

    fun showSettingsDialog() {
        _scannerState.value = _scannerState.value.copy(showSettingsDialog = true)
    }

    fun hideSettingsDialog() {
        _scannerState.value = _scannerState.value.copy(showSettingsDialog = false)
    }

    override fun onCleared() {
        super.onCleared()
        beepManager.release()
    }
}
