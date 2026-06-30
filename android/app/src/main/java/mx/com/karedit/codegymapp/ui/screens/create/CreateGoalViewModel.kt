package mx.com.karedit.codegymapp.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.GoalsRepository
import mx.com.karedit.codegymapp.data.repository.MobileGoalOption
import mx.com.karedit.codegymapp.domain.model.MobileGoal
import mx.com.karedit.codegymapp.domain.model.MobileLanguage
import mx.com.karedit.codegymapp.domain.model.MobilePlatform

class CreateGoalViewModel(private val repository: GoalsRepository) : ViewModel() {
    private val _state = MutableStateFlow(CreateGoalUiState())
    val state: StateFlow<CreateGoalUiState> = _state

    init {
        loadOptions()
    }

    fun loadOptions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            repository.options()
                .onSuccess { options ->
                    _state.update {
                        it.copy(
                            goalTypes = options.goalTypes,
                            periodTypes = options.periodTypes,
                            platforms = options.platforms,
                            languages = options.languages,
                            goalType = it.goalType.ifBlank { options.goalTypes.firstOrNull()?.value.orEmpty() },
                            periodType = it.periodType.ifBlank { options.periodTypes.firstOrNull()?.value.orEmpty() }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(message = error.message ?: "No se pudieron cargar las opciones de metas.") }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun startCreate() {
        _state.update {
            it.copy(
                editingGoalId = null,
                targetValue = "",
                platformId = 0,
                languageId = 0,
                autoRenew = false,
                message = null
            )
        }
    }

    fun startEdit(goal: MobileGoal) {
        _state.update {
            it.copy(
                editingGoalId = goal.id,
                goalType = goal.goalType,
                periodType = goal.periodType,
                targetValue = goal.targetValue.toString(),
                platformId = goal.platformId,
                languageId = goal.languageId,
                autoRenew = goal.autoRenew,
                message = null
            )
        }
    }

    fun selectGoalType(value: String) {
        _state.update { it.copy(goalType = value) }
    }

    fun selectPeriodType(value: String) {
        _state.update { it.copy(periodType = value) }
    }

    fun selectPlatform(id: Int) {
        _state.update { it.copy(platformId = id) }
    }

    fun selectLanguage(id: Int) {
        _state.update { it.copy(languageId = id) }
    }

    fun updateTargetValue(value: String) {
        _state.update { it.copy(targetValue = value.filter(Char::isDigit)) }
    }

    fun updateAutoRenew(value: Boolean) {
        _state.update { it.copy(autoRenew = value) }
    }

    fun save(onSaved: (String) -> Unit) {
        val current = _state.value
        val target = current.targetValue.toIntOrNull() ?: 0
        if (target <= 0) {
            _state.update { it.copy(message = "Captura un objetivo mayor a cero.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, message = null) }
            val result = if (current.editingGoalId != null) {
                repository.update(
                    id = current.editingGoalId,
                    goalType = current.goalType,
                    periodType = current.periodType,
                    targetValue = target,
                    platformId = current.platformId,
                    languageId = current.languageId,
                    autoRenew = current.autoRenew
                )
            } else {
                repository.create(
                    goalType = current.goalType,
                    periodType = current.periodType,
                    targetValue = target,
                    platformId = current.platformId,
                    languageId = current.languageId,
                    autoRenew = current.autoRenew
                )
            }

            result
                .onSuccess(onSaved)
                .onFailure { error ->
                    _state.update { it.copy(message = error.message ?: "No se pudo guardar la meta.") }
                }
            _state.update { it.copy(isSaving = false) }
        }
    }
}

data class CreateGoalUiState(
    val goalTypes: List<MobileGoalOption> = emptyList(),
    val periodTypes: List<MobileGoalOption> = emptyList(),
    val platforms: List<MobilePlatform> = emptyList(),
    val languages: List<MobileLanguage> = emptyList(),
    val editingGoalId: Int? = null,
    val goalType: String = "",
    val periodType: String = "",
    val targetValue: String = "",
    val platformId: Int = 0,
    val languageId: Int = 0,
    val autoRenew: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null
)
