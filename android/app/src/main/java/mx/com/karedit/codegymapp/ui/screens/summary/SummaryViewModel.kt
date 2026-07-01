package mx.com.karedit.codegymapp.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.core.network.isOfflineUiMessage
import mx.com.karedit.codegymapp.data.repository.SummaryRepository
import mx.com.karedit.codegymapp.domain.model.MobileSummary

class SummaryViewModel(private val summaryRepository: SummaryRepository) : ViewModel() {
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val _state = MutableStateFlow(SummaryUiState(month = LocalDate.now().format(monthFormatter)))
    val state: StateFlow<SummaryUiState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val month = _state.value.month
            _state.update { it.copy(isLoading = true, snackbarMessage = null) }
            summaryRepository.summary(month)
                .onSuccess { summary -> _state.update { it.copy(summary = summary) } }
                .onFailure { error ->
                    if (!error.isOfflineUiMessage()) {
                        _state.update { it.copy(snackbarMessage = error.message ?: "No se pudo cargar el resumen.") }
                    }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun previousMonth() {
        moveMonth(-1)
    }

    fun nextMonth() {
        moveMonth(1)
    }

    fun currentMonth() {
        _state.update { it.copy(month = LocalDate.now().format(monthFormatter)) }
        load()
    }

    private fun moveMonth(months: Long) {
        val next = YearMonth.parse(_state.value.month, monthFormatter).plusMonths(months)
        _state.update { it.copy(month = next.format(monthFormatter)) }
        load()
    }

    fun snackbarShown() {
        _state.update { it.copy(snackbarMessage = null) }
    }
}

data class SummaryUiState(
    val month: String,
    val summary: MobileSummary? = null,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
) {
    val monthLabel: String
        get() {
            val yearMonth = YearMonth.parse(month)
            val monthName = yearMonth.month.getDisplayName(
                java.time.format.TextStyle.FULL,
                Locale("es", "MX")
            )
            return "${monthName.replaceFirstChar { it.titlecase(Locale("es", "MX")) }} ${yearMonth.year}"
        }
}
