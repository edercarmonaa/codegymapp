package mx.com.karedit.codegymapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
        val hasToken = authRepository.hasToken()
        val requiresBiometric = hasToken && settingsRepository.settings.value.biometricEnabled
        _state.update {
            it.copy(
                isAuthenticated = hasToken && !requiresBiometric,
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

    fun onBiometricSucceeded() {
        _state.update {
            it.copy(
                isAuthenticated = true,
                biometricRequest = null,
                loginMessage = null
            )
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
        if (!authRepository.hasToken()) {
            onSessionExpired(SessionExpiredReason.Manual)
            return
        }

        if (!settingsRepository.settings.value.biometricEnabled) {
            _state.update { it.copy(isAuthenticated = true, biometricRequest = null) }
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
    val loginMessage: String? = null
)

data class BiometricRequest(
    val reason: BiometricRequestReason,
    val id: Long = System.nanoTime()
)

enum class BiometricRequestReason {
    InitialLogin,
    BackgroundTimeout,
    ManualLock
}
