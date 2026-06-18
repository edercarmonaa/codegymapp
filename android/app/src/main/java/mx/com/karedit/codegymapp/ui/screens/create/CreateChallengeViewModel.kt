package mx.com.karedit.codegymapp.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.CreateChallengeRepository
import mx.com.karedit.codegymapp.domain.model.MobilePlatform

class CreateChallengeViewModel(
    private val repository: CreateChallengeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(
        CreateChallengeUiState(scheduledDate = LocalDate.now().toString())
    )
    val state: StateFlow<CreateChallengeUiState> = _state

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

    fun updateScheduledDate(value: String) {
        _state.update { it.copy(scheduledDate = value) }
    }

    fun create(onCreated: (String) -> Unit) {
        val current = _state.value
        val platformId = current.selectedPlatformId ?: 0
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, message = null) }
            repository.create(platformId = platformId, scheduledDate = current.scheduledDate)
                .onSuccess(onCreated)
                .onFailure { error -> _state.update { it.copy(message = error.message ?: "No se pudo crear el reto.") } }
            _state.update { it.copy(isSaving = false) }
        }
    }
}

data class CreateChallengeUiState(
    val platforms: List<MobilePlatform> = emptyList(),
    val selectedPlatformId: Int? = null,
    val scheduledDate: String,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null
)
