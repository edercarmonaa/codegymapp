package mx.com.karedit.codegymapp.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.AuthRepository
import mx.com.karedit.codegymapp.data.repository.TodayRepository
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import mx.com.karedit.codegymapp.domain.model.User

class TodayViewModel(
    private val authRepository: AuthRepository,
    private val todayRepository: TodayRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TodayUiState())
    val state: StateFlow<TodayUiState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, snackbarMessage = null) }

            authRepository.me()
                .onSuccess { user -> _state.update { it.copy(user = user) } }
                .onFailure { error ->
                    _state.update { it.copy(snackbarMessage = error.message ?: "No se pudo cargar la sesión.") }
                }

            todayRepository.today()
                .onSuccess { data ->
                    _state.update {
                        it.copy(
                            todayChallenges = data.today,
                            expiredChallenges = data.expired
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(snackbarMessage = error.message ?: "No se pudo cargar Mi día.") }
                }

            _state.update { it.copy(isLoading = false) }
        }
    }

    fun completeChallenge(id: Int) {
        runChallengeAction { todayRepository.completeChallenge(id) }
    }

    fun missChallenge(id: Int) {
        runChallengeAction { todayRepository.missChallenge(id) }
    }

    fun cancelChallenge(id: Int) {
        runChallengeAction { todayRepository.cancelChallenge(id) }
    }

    fun rescheduleChallenge(id: Int, scheduledDate: String) {
        runChallengeAction { todayRepository.rescheduleChallenge(id, scheduledDate) }
    }

    private fun runChallengeAction(action: suspend () -> Result<String>) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true, snackbarMessage = null) }
            action()
                .onSuccess { message ->
                    _state.update { it.copy(snackbarMessage = message) }
                    refreshToday()
                }
                .onFailure { error ->
                    _state.update { it.copy(snackbarMessage = error.message ?: "No se pudo actualizar el reto.") }
                }
            _state.update { it.copy(isActionLoading = false) }
        }
    }

    private suspend fun refreshToday() {
        todayRepository.today()
            .onSuccess { data ->
                _state.update {
                    it.copy(
                        todayChallenges = data.today,
                        expiredChallenges = data.expired
                    )
                }
            }
            .onFailure { error ->
                _state.update { it.copy(snackbarMessage = error.message ?: "No se pudo cargar Mi día.") }
            }
    }

    fun snackbarShown() {
        _state.update { it.copy(snackbarMessage = null) }
    }
}

data class TodayUiState(
    val user: User? = null,
    val todayChallenges: List<MobileChallenge> = emptyList(),
    val expiredChallenges: List<MobileChallenge> = emptyList(),
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val snackbarMessage: String? = null
)
