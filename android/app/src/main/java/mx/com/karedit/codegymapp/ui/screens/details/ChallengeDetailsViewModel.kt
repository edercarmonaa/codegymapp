package mx.com.karedit.codegymapp.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.ChallengeDetailsRepository
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import mx.com.karedit.codegymapp.domain.model.MobileLanguage
import mx.com.karedit.codegymapp.domain.model.MobilePlatform

class ChallengeDetailsViewModel(
    private val repository: ChallengeDetailsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ChallengeDetailsUiState())
    val state: StateFlow<ChallengeDetailsUiState> = _state

    fun load(challenge: MobileChallenge) {
        _state.value = ChallengeDetailsUiState(
            id = challenge.id,
            platformId = challenge.platformId,
            platformName = challenge.platformName,
            scheduledDate = challenge.scheduledDate,
            status = challenge.status,
            title = challenge.title,
            challengeUrl = challenge.challengeUrl.orEmpty(),
            difficulty = challenge.difficulty,
            timeSpentMinutes = if (challenge.timeSpentMinutes > 0) challenge.timeSpentMinutes.toString() else "",
            notes = challenge.notes,
            selectedLanguageIds = challenge.languageIds.toSet(),
            githubLinks = challenge.githubLinks.joinToString("\n")
        )
        loadOptions()
    }

    private fun loadOptions() {
        viewModelScope.launch {
            repository.options()
                .onSuccess { options ->
                    _state.update {
                        it.copy(
                            platforms = options.platforms,
                            languages = options.languages,
                            platformId = it.platformId.takeIf { platformId -> platformId > 0 }
                                ?: options.platforms.firstOrNull()?.id
                                ?: 0
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(message = error.message ?: "No se pudieron cargar los catálogos.") }
                }
        }
    }

    fun selectPlatform(id: Int) {
        val platformName = _state.value.platforms.firstOrNull { it.id == id }?.name ?: _state.value.platformName
        _state.update { it.copy(platformId = id, platformName = platformName) }
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
        _state.update { it.copy(timeSpentMinutes = value.filter(Char::isDigit).take(4)) }
    }

    fun updateNotes(value: String) {
        _state.update { it.copy(notes = value) }
    }

    fun toggleLanguage(id: Int) {
        _state.update {
            val updated = if (it.selectedLanguageIds.contains(id)) {
                it.selectedLanguageIds - id
            } else {
                it.selectedLanguageIds + id
            }
            it.copy(selectedLanguageIds = updated)
        }
    }

    fun updateGithubLinks(value: String) {
        _state.update { it.copy(githubLinks = value) }
    }

    fun save(onSaved: (String) -> Unit) {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, message = null) }
            repository.save(
                id = current.id,
                platformId = current.platformId,
                title = current.title.trim(),
                challengeUrl = current.challengeUrl.trim(),
                difficulty = current.difficulty.trim(),
                timeSpentMinutes = current.timeSpentMinutes.toIntOrNull(),
                notes = current.notes.trim(),
                languageIds = current.selectedLanguageIds.toList(),
                githubLinks = current.githubLinks.trim()
            )
                .onSuccess(onSaved)
                .onFailure { error -> _state.update { it.copy(message = error.message ?: "No se pudo actualizar el reto.") } }
            _state.update { it.copy(isSaving = false) }
        }
    }
}

data class ChallengeDetailsUiState(
    val id: Int = 0,
    val platformId: Int = 0,
    val platformName: String = "",
    val scheduledDate: String = "",
    val status: String = "",
    val title: String = "",
    val challengeUrl: String = "",
    val difficulty: String = "",
    val timeSpentMinutes: String = "",
    val notes: String = "",
    val selectedLanguageIds: Set<Int> = emptySet(),
    val githubLinks: String = "",
    val platforms: List<MobilePlatform> = emptyList(),
    val languages: List<MobileLanguage> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null
)
