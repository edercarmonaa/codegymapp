package mx.com.karedit.codegymapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.data.repository.AuthRepository
import mx.com.karedit.codegymapp.data.repository.SettingsRepository
import mx.com.karedit.codegymapp.data.repository.ThemePreference

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    val settings = settingsRepository.settings

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun updateTheme(themePreference: ThemePreference) {
        settingsRepository.updateThemePreference(themePreference)
        viewModelScope.launch {
            settingsRepository.syncSettings()
                .onSuccess { message -> _message.value = message }
                .onFailure { error -> _message.value = error.message ?: "Tema actualizado localmente; no se pudo sincronizar." }
        }
    }

    fun updatePush(enabled: Boolean) {
        settingsRepository.updatePushEnabled(enabled)
        viewModelScope.launch {
            settingsRepository.syncSettings()
                .onSuccess { message -> _message.value = message }
                .onFailure { error ->
                    _message.value = error.message ?: if (enabled) {
                        "Push activado localmente; no se pudo sincronizar."
                    } else {
                        "Push desactivado localmente; no se pudo sincronizar."
                    }
                }
        }
    }

    fun updateReminderTime(time: String) {
        settingsRepository.updateReminderTime(time)
        viewModelScope.launch {
            settingsRepository.syncSettings()
                .onSuccess { message -> _message.value = message }
                .onFailure { error -> _message.value = error.message ?: "Hora actualizada localmente; no se pudo sincronizar." }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            settingsRepository.syncSettings()
                .onSuccess { message -> _message.value = message }
                .onFailure { error -> _message.value = error.message ?: "No se pudo sincronizar." }
        }
    }

    fun logout() {
        authRepository.logoutAndClear()
    }

    fun messageShown() {
        _message.update { null }
    }
}
