package mx.com.karedit.codegymapp.ui.screens.planned

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.PlannedRepository
import mx.com.karedit.codegymapp.domain.model.MobileChallenge

class PlannedViewModel(private val plannedRepository: PlannedRepository) : ViewModel() {
    private val _state = MutableStateFlow(PlannedUiState())
    val state: StateFlow<PlannedUiState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, snackbarMessage = null) }
            plannedRepository.planned()
                .onSuccess { challenges -> _state.update { it.copy(challenges = challenges) } }
                .onFailure { error ->
                    _state.update { it.copy(snackbarMessage = error.message ?: "No se pudo cargar Planeado.") }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun completeChallenge(id: Int) {
        runChallengeAction { plannedRepository.completeChallenge(id) }
    }

    fun missChallenge(id: Int) {
        runChallengeAction { plannedRepository.missChallenge(id) }
    }

    private fun runChallengeAction(action: suspend () -> Result<String>) {
        viewModelScope.launch {
            _state.update { it.copy(snackbarMessage = null) }
            action()
                .onSuccess { message ->
                    _state.update { it.copy(snackbarMessage = message) }
                    load()
                }
                .onFailure { error ->
                    _state.update { it.copy(snackbarMessage = error.message ?: "No se pudo actualizar el reto.") }
                }
        }
    }

    fun snackbarShown() {
        _state.update { it.copy(snackbarMessage = null) }
    }
}

data class PlannedUiState(
    val challenges: List<MobileChallenge> = emptyList(),
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)
