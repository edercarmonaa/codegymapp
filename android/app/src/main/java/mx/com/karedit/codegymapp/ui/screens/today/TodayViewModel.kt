package mx.com.karedit.codegymapp.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.AuthRepository
import mx.com.karedit.codegymapp.domain.model.User

class TodayViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(TodayUiState())
    val state: StateFlow<TodayUiState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.me()
                .onSuccess { user -> _state.update { it.copy(user = user) } }
                .onFailure { error -> _state.update { it.copy(error = error.message ?: "No se pudo cargar Mi día.") } }
            _state.update { it.copy(isLoading = false) }
        }
    }
}

data class TodayUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
