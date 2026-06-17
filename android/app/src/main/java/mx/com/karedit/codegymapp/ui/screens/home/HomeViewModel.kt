package mx.com.karedit.codegymapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.AuthRepository
import mx.com.karedit.codegymapp.data.repository.ChallengesRepository
import mx.com.karedit.codegymapp.data.repository.PlannedRepository
import mx.com.karedit.codegymapp.data.repository.TodayRepository
import mx.com.karedit.codegymapp.domain.model.User

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val todayRepository: TodayRepository,
    private val plannedRepository: PlannedRepository,
    private val challengesRepository: ChallengesRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            authRepository.me()
                .onSuccess { user -> _state.update { it.copy(user = user) } }

            todayRepository.today()
                .onSuccess { data ->
                    _state.update { it.copy(todayCount = data.today.size + data.expired.size) }
                }

            plannedRepository.planned()
                .onSuccess { planned -> _state.update { it.copy(plannedCount = planned.size) } }

            val month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            challengesRepository.challenges(month = month, status = "pending")
                .onSuccess { challenges -> _state.update { it.copy(challengesCount = challenges.size) } }

            _state.update { it.copy(isLoading = false) }
        }
    }
}

data class HomeUiState(
    val user: User? = null,
    val todayCount: Int = 0,
    val plannedCount: Int = 0,
    val challengesCount: Int = 0,
    val isLoading: Boolean = false
)
