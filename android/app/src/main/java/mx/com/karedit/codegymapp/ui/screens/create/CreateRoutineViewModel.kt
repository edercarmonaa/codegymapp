package mx.com.karedit.codegymapp.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.CreateRoutineRepository
import mx.com.karedit.codegymapp.domain.model.MobilePlatform

class CreateRoutineViewModel(
    private val repository: CreateRoutineRepository
) : ViewModel() {
    private val _state = MutableStateFlow(
        CreateRoutineUiState(
            startDate = LocalDate.now().toString(),
            monthDay = LocalDate.now().dayOfMonth.toString()
        )
    )
    val state: StateFlow<CreateRoutineUiState> = _state

    init {
        loadPlatforms()
    }

    fun loadPlatforms() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            repository.platforms()
                .onSuccess { platforms ->
                    _state.update {
                        it.copy(
                            platforms = platforms,
                            selectedPlatformId = it.selectedPlatformId ?: platforms.firstOrNull()?.id
                        )
                    }
                }
                .onFailure { error -> _state.update { it.copy(message = error.message ?: "No se pudieron cargar las plataformas.") } }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun selectPlatform(id: Int) {
        _state.update { it.copy(selectedPlatformId = id) }
    }

    fun selectFrequency(frequency: RoutineFrequency) {
        _state.update { it.copy(frequency = frequency, message = null) }
    }

    fun updateStartDate(value: String) {
        _state.update { it.copy(startDate = value) }
    }

    fun updateEndDate(value: String) {
        _state.update { it.copy(endDate = value) }
    }

    fun clearEndDate() {
        _state.update { it.copy(endDate = "") }
    }

    fun toggleWeekDay(day: Int) {
        _state.update {
            val days = if (day in it.weekDays) {
                it.weekDays - day
            } else {
                (it.weekDays + day).sorted()
            }
            it.copy(weekDays = days, message = null)
        }
    }

    fun updateMonthDay(value: String) {
        _state.update { it.copy(monthDay = value.filter(Char::isDigit).take(2), message = null) }
    }

    fun create(onCreated: (String) -> Unit) {
        val current = _state.value
        val platformId = current.selectedPlatformId ?: 0
        val monthDay = current.monthDay.toIntOrNull() ?: 0

        if (current.frequency == RoutineFrequency.Weekly && current.weekDays.isEmpty()) {
            _state.update { it.copy(message = "Selecciona al menos un día de la semana.") }
            return
        }

        if (current.frequency == RoutineFrequency.Monthly && monthDay !in 1..31) {
            _state.update { it.copy(message = "Captura un día del mes válido.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, message = null) }
            repository.create(
                platformId = platformId,
                frequencyType = current.frequency.value,
                weekDays = if (current.frequency == RoutineFrequency.Weekly) current.weekDays else emptyList(),
                monthDay = if (current.frequency == RoutineFrequency.Monthly) monthDay else 0,
                startDate = current.startDate,
                endDate = current.endDate
            )
                .onSuccess(onCreated)
                .onFailure { error -> _state.update { it.copy(message = error.message ?: "No se pudo crear la rutina.") } }
            _state.update { it.copy(isSaving = false) }
        }
    }
}

data class CreateRoutineUiState(
    val platforms: List<MobilePlatform> = emptyList(),
    val selectedPlatformId: Int? = null,
    val frequency: RoutineFrequency = RoutineFrequency.Daily,
    val weekDays: List<Int> = emptyList(),
    val monthDay: String = "1",
    val startDate: String,
    val endDate: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null
)

enum class RoutineFrequency(val value: String, val label: String) {
    Daily("daily", "Diaria"),
    Weekly("weekly", "Semanal"),
    Monthly("monthly", "Mensual")
}
