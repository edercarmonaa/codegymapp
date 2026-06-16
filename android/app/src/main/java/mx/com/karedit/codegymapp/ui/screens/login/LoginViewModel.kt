package mx.com.karedit.codegymapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.AuthRepository

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun updateUsername(value: String) {
        _state.update { it.copy(username = value, error = null) }
    }

    fun updatePassword(value: String) {
        _state.update { it.copy(password = value, error = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val current = _state.value
        if (current.username.isBlank() || current.password.isBlank()) {
            _state.update { it.copy(error = "Captura usuario y contraseña.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.login(current.username.trim(), current.password)
                .onSuccess { onSuccess() }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message ?: "No se pudo iniciar sesión.") }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
