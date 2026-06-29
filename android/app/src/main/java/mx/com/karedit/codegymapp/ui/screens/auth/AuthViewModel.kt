package mx.com.karedit.codegymapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.core.session.SessionExpiredReason
import mx.com.karedit.codegymapp.data.repository.AuthRepository
import mx.com.karedit.codegymapp.data.repository.SettingsRepository

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    init {
        evaluateInitialSession()
    }

    fun evaluateInitialSession() {
        val hasAccessToken = authRepository.hasToken()
        val hasRefreshToken = authRepository.hasRefreshToken()
        val requiresBiometric = settingsRepository.settings.value.biometricEnabled && hasRefreshToken
        _state.update {
            it.copy(
                isAuthenticated = hasAccessToken && !requiresBiometric,
                biometricRequest = if (requiresBiometric) {
                    BiometricRequest(BiometricRequestReason.InitialLogin)
                } else {
                    null
                },
                loginMessage = null
            )
        }
    }

    fun onForegroundAfterBackground(elapsedMillis: Long) {
        if (elapsedMillis >= BACKGROUND_LOCK_TIMEOUT_MS) {
            requestBiometric(BiometricRequestReason.BackgroundTimeout)
        }
    }

    fun onSessionLocked() {
        requestBiometric(BiometricRequestReason.ManualLock)
    }

    fun onSessionExpired(reason: SessionExpiredReason) {
        if (settingsRepository.settings.value.biometricEnabled && authRepository.hasRefreshToken()) {
            requestBiometric(BiometricRequestReason.TokenExpired)
            return
        }

        authRepository.logoutAndClear()
        _state.update {
            it.copy(
                isAuthenticated = false,
                biometricRequest = null,
                loginMessage = when (reason) {
                    SessionExpiredReason.Inactivity -> "Sesión expirada"
                    SessionExpiredReason.Unauthorized -> "Sesión expirada. Inicia sesión de nuevo."
                    SessionExpiredReason.Manual -> null
                }
            )
        }
    }

    fun onManualLoginSuccess() {
        _state.update {
            it.copy(
                isAuthenticated = true,
                biometricRequest = null,
                loginMessage = null
            )
        }
    }

    fun onBiometricSucceeded(
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        _state.update { it.copy(isRefreshing = true, loginMessage = null) }
        viewModelScope.launch {
            authRepository.refreshSession()
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            isAuthenticated = true,
                            biometricRequest = null,
                            loginMessage = null,
                            isRefreshing = false
                        )
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    authRepository.logoutAndClear()
                    _state.update { state ->
                        state.copy(
                            isAuthenticated = false,
                            biometricRequest = null,
                            loginMessage = error.message ?: "No se pudo renovar la sesión. Inicia sesión con contraseña.",
                            isRefreshing = false
                        )
                    }
                    onFailure()
                }
        }
    }

    fun onBiometricCancelledOrFailed(message: String? = null) {
        authRepository.logoutAndClear()
        _state.update {
            it.copy(
                isAuthenticated = false,
                biometricRequest = null,
                loginMessage = message ?: "Verificación biométrica cancelada. Inicia sesión con contraseña."
            )
        }
    }

    fun disableBiometricAndLogout() {
        settingsRepository.updateBiometricEnabled(false)
        onBiometricCancelledOrFailed("Huella desactivada. Inicia sesión con usuario y contraseña.")
    }

    private fun requestBiometric(reason: BiometricRequestReason) {
        if (!authRepository.hasRefreshToken()) {
            onSessionExpired(SessionExpiredReason.Manual)
            return
        }

        if (!settingsRepository.settings.value.biometricEnabled) {
            if (authRepository.hasToken()) {
                _state.update { it.copy(isAuthenticated = true, biometricRequest = null) }
            } else {
                onSessionExpired(SessionExpiredReason.Manual)
            }
            return
        }

        _state.update {
            it.copy(
                isAuthenticated = false,
                biometricRequest = BiometricRequest(reason),
                loginMessage = null
            )
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(authRepository, settingsRepository) as T
    }

    private companion object {
        const val BACKGROUND_LOCK_TIMEOUT_MS = 3 * 60 * 1000L
    }
}

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val biometricRequest: BiometricRequest? = null,
    val loginMessage: String? = null,
    val isRefreshing: Boolean = false
)

data class BiometricRequest(
    val reason: BiometricRequestReason,
    val id: Long = System.nanoTime()
)

enum class BiometricRequestReason {
    InitialLogin,
    BackgroundTimeout,
    ManualLock,
    TokenExpired
}
