package mx.com.karedit.codegymapp.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.AuthRepository
import mx.com.karedit.codegymapp.data.repository.SettingsRepository
import mx.com.karedit.codegymapp.domain.model.User

class AccountViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            authRepository.me()
                .onSuccess { user -> _state.update { it.copy(user = user) } }
                .onFailure { error -> _state.update { it.copy(message = error.message ?: "No se pudo cargar la cuenta.") } }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun logout() {
        if (settingsRepository.settings.value.biometricEnabled) {
            authRepository.lockSession()
        } else {
            authRepository.logoutAndClear()
        }
    }
}

data class AccountUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)
