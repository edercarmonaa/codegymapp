package mx.com.karedit.codegymapp.ui.screens.challenges

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
import mx.com.karedit.codegymapp.data.repository.ChallengesRepository
import mx.com.karedit.codegymapp.domain.model.MobileChallenge

class ChallengesViewModel(private val challengesRepository: ChallengesRepository) : ViewModel() {
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val _state = MutableStateFlow(
        ChallengesUiState(month = LocalDate.now().format(monthFormatter))
    )
    val state: StateFlow<ChallengesUiState> = _state

    init {
        load()
    }

    fun selectStatus(status: ChallengeStatusFilter) {
        _state.update { it.copy(status = status) }
        load()
    }

    fun previousMonth() {
        moveMonth(months = -1)
    }

    fun nextMonth() {
        moveMonth(months = 1)
    }

    fun currentMonth() {
        _state.update { it.copy(month = LocalDate.now().format(monthFormatter)) }
        load()
    }

    private fun moveMonth(months: Long) {
        val currentMonth = YearMonth.parse(_state.value.month, monthFormatter)
        _state.update { it.copy(month = currentMonth.plusMonths(months).format(monthFormatter)) }
        load()
    }

    fun load() {
        viewModelScope.launch {
            val current = _state.value
            _state.update { it.copy(isLoading = true, snackbarMessage = null) }
            challengesRepository.challenges(month = current.month, status = current.status.value)
                .onSuccess { challenges -> _state.update { it.copy(challenges = challenges) } }
                .onFailure { error ->
                    _state.update { it.copy(snackbarMessage = error.message ?: "No se pudieron cargar los retos.") }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun completeChallenge(id: Int) {
        runChallengeAction { challengesRepository.completeChallenge(id) }
    }

    fun missChallenge(id: Int) {
        runChallengeAction { challengesRepository.missChallenge(id) }
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

data class ChallengesUiState(
    val month: String,
    val status: ChallengeStatusFilter = ChallengeStatusFilter.Pending,
    val challenges: List<MobileChallenge> = emptyList(),
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
) {
    val monthLabel: String
        get() {
            val monthName = YearMonth.parse(month).month.getDisplayName(
                java.time.format.TextStyle.FULL,
                Locale("es", "MX")
            )
            return "${monthName.replaceFirstChar { it.titlecase(Locale("es", "MX")) }} ${YearMonth.parse(month).year}"
        }
}

enum class ChallengeStatusFilter(val value: String, val label: String) {
    Pending("pending", "Pendientes"),
    Completed("completed", "Cumplidos"),
    Expired("expired", "Vencidos"),
    Missed("missed", "No realizados"),
    All("all", "Todos")
}
