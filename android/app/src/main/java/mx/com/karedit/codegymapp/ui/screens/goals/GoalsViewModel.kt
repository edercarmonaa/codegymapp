package mx.com.karedit.codegymapp.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.core.network.isOfflineUiMessage
import mx.com.karedit.codegymapp.data.repository.GoalsRepository
import mx.com.karedit.codegymapp.domain.model.MobileGoal

class GoalsViewModel(private val repository: GoalsRepository) : ViewModel() {
    private val _state = MutableStateFlow(GoalsUiState())
    val state: StateFlow<GoalsUiState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, snackbarMessage = null) }
            repository.activeGoals()
                .onSuccess { goals -> _state.update { it.copy(goals = goals) } }
                .onFailure { error ->
                    if (!error.isOfflineUiMessage()) {
                        _state.update { it.copy(snackbarMessage = error.message ?: "No se pudieron cargar las metas.") }
                    }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun snackbarShown() {
        _state.update { it.copy(snackbarMessage = null) }
    }
}

data class GoalsUiState(
    val goals: List<MobileGoal> = emptyList(),
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)
