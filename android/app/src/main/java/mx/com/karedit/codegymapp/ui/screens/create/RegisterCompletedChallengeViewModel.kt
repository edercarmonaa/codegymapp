package mx.com.karedit.codegymapp.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.core.network.isOfflineUiMessage
import mx.com.karedit.codegymapp.data.repository.CreateChallengeRepository
import mx.com.karedit.codegymapp.domain.model.MobileLanguage
import mx.com.karedit.codegymapp.domain.model.MobilePlatform

class RegisterCompletedChallengeViewModel(
    private val repository: CreateChallengeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterCompletedChallengeUiState())
    val state: StateFlow<RegisterCompletedChallengeUiState> = _state

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
                            platforms = options.platforms,
                            languages = options.languages,
                            selectedPlatformId = it.selectedPlatformId ?: options.platforms.firstOrNull()?.id
                        )
                    }
                }
                .onFailure { error ->
                    if (!error.isOfflineUiMessage()) {
                        _state.update { it.copy(message = error.message ?: "No se pudieron cargar los catálogos.") }
                    }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun selectPlatform(id: Int) {
        _state.update { it.copy(selectedPlatformId = id) }
    }

    fun updateTitle(value: String) {
        _state.update { it.copy(title = value) }
    }

    fun updateChallengeUrl(value: String) {
        _state.update { it.copy(challengeUrl = value) }
    }

    fun updateDifficulty(value: String) {
        _state.update { it.copy(difficulty = value) }
    }

    fun updateTimeSpentMinutes(value: String) {
        _state.update { it.copy(timeSpentMinutes = value.filter(Char::isDigit)) }
    }

    fun updateNotes(value: String) {
        _state.update { it.copy(notes = value) }
    }

    fun updateGithubLinks(value: String) {
        _state.update { it.copy(githubLinks = value) }
    }

    fun toggleLanguage(id: Int) {
        _state.update { current ->
            val selected = current.selectedLanguageIds
            current.copy(
                selectedLanguageIds = if (id in selected) {
                    selected - id
                } else {
                    selected + id
                }
            )
        }
    }

    fun create(onCreated: (String) -> Unit) {
        val current = _state.value
        val validation = current.validationMessage()
        if (validation != null) {
            _state.update { it.copy(message = validation) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, message = null) }
            repository.createManual(
                platformId = current.selectedPlatformId ?: 0,
                title = current.title.trim(),
                challengeUrl = current.challengeUrl.trim(),
                difficulty = current.difficulty.trim(),
                timeSpentMinutes = current.timeSpentMinutes.toIntOrNull() ?: 0,
                notes = current.notes.trim(),
                languageIds = current.selectedLanguageIds,
                githubLinks = current.githubLinks.trim()
            )
                .onSuccess(onCreated)
                .onFailure { error ->
                    _state.update { it.copy(message = error.message ?: "No se pudo registrar el reto realizado.") }
                }
            _state.update { it.copy(isSaving = false) }
        }
    }
}

data class RegisterCompletedChallengeUiState(
    val platforms: List<MobilePlatform> = emptyList(),
    val languages: List<MobileLanguage> = emptyList(),
    val selectedPlatformId: Int? = null,
    val title: String = "",
    val challengeUrl: String = "",
    val difficulty: String = "",
    val timeSpentMinutes: String = "",
    val notes: String = "",
    val githubLinks: String = "",
    val selectedLanguageIds: List<Int> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null
) {
    fun validationMessage(): String? = when {
        selectedPlatformId == null -> "Selecciona una plataforma."
        title.isBlank() -> "Captura el nombre del reto."
        difficulty.isBlank() -> "Captura la dificultad."
        (timeSpentMinutes.toIntOrNull() ?: 0) <= 0 -> "Captura el tiempo invertido."
        selectedLanguageIds.isEmpty() -> "Selecciona al menos un lenguaje."
        else -> null
    }
}
