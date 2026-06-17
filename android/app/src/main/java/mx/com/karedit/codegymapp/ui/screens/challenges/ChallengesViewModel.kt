package mx.com.karedit.codegymapp.ui.screens.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.ChallengesRepository
import mx.com.karedit.codegymapp.domain.model.MobileChallenge

class ChallengesViewModel(private val challengesRepository: ChallengesRepository) : ViewModel() {
    private val _state = MutableStateFlow(
        ChallengesUiState(month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")))
    )
    val state: StateFlow<ChallengesUiState> = _state

    init {
        load()
    }

    fun selectStatus(status: ChallengeStatusFilter) {
        _state.update { it.copy(status = status) }
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
)

enum class ChallengeStatusFilter(val value: String, val label: String) {
    Pending("pending", "Pendientes"),
    Completed("completed", "Cumplidos"),
    Expired("expired", "Vencidos"),
    Missed("missed", "No realizados"),
    All("all", "Todos")
}
