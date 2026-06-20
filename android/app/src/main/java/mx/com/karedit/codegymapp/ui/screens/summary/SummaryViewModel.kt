package mx.com.karedit.codegymapp.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.SummaryRepository
import mx.com.karedit.codegymapp.domain.model.MobileSummary

class SummaryViewModel(private val summaryRepository: SummaryRepository) : ViewModel() {
    private val _state = MutableStateFlow(SummaryUiState())
    val state: StateFlow<SummaryUiState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, snackbarMessage = null) }
            summaryRepository.summary()
                .onSuccess { summary -> _state.update { it.copy(summary = summary) } }
                .onFailure { error ->
                    _state.update { it.copy(snackbarMessage = error.message ?: "No se pudo cargar el resumen.") }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun snackbarShown() {
        _state.update { it.copy(snackbarMessage = null) }
    }
}

data class SummaryUiState(
    val summary: MobileSummary? = null,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)
