package mx.com.karedit.codegymapp.ui.screens.planned

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.core.network.isOfflineUiMessage
import mx.com.karedit.codegymapp.data.repository.PlannedRepository
import mx.com.karedit.codegymapp.domain.model.MobileChallenge

class PlannedViewModel(private val plannedRepository: PlannedRepository) : ViewModel() {
    private val _state = MutableStateFlow(PlannedUiState())
    val state: StateFlow<PlannedUiState> = _state

    init {
        load()
    }

    fun selectFilter(filter: PlannedFilter) {
        _state.update { it.copy(filter = filter) }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, snackbarMessage = null) }
            plannedRepository.planned()
                .onSuccess { challenges -> _state.update { it.copy(challenges = challenges) } }
                .onFailure { error ->
                    if (!error.isOfflineUiMessage()) {
                        _state.update { it.copy(snackbarMessage = error.message ?: "No se pudo cargar Planeado.") }
                    }
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

    fun cancelChallenge(id: Int) {
        runChallengeAction { plannedRepository.cancelChallenge(id) }
    }

    fun rescheduleChallenge(id: Int, scheduledDate: String) {
        runChallengeAction { plannedRepository.rescheduleChallenge(id, scheduledDate) }
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
    val filter: PlannedFilter = PlannedFilter.All,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
) {
    val filteredChallenges: List<MobileChallenge>
        get() = challenges.filter { challenge -> filter.matches(challenge) }
}

enum class PlannedFilter(val label: String) {
    Expired("Vencidas"),
    Today("Hoy"),
    Tomorrow("Mañana"),
    ThisWeek("Esta semana"),
    Later("Después"),
    All("Todo");

    fun matches(challenge: MobileChallenge): Boolean {
        val date = challenge.scheduledLocalDate() ?: return this == All
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        return when (this) {
            Expired -> challenge.status == "expired" || date < today
            Today -> challenge.status == "pending" && date == today
            Tomorrow -> challenge.status == "pending" && date == tomorrow
            ThisWeek -> challenge.status == "pending" && date > tomorrow && date <= endOfWeek
            Later -> challenge.status == "pending" && date > endOfWeek
            All -> true
        }
    }
}

private fun MobileChallenge.scheduledLocalDate(): LocalDate? =
    try {
        LocalDate.parse(scheduledDate)
    } catch (_: DateTimeParseException) {
        null
    }
