package mx.com.karedit.codegymapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
        _message.value = "Tema actualizado."
    }

    fun updatePush(enabled: Boolean) {
        settingsRepository.updatePushEnabled(enabled)
        _message.value = if (enabled) "Push activado." else "Push desactivado localmente."
    }

    fun updateReminderTime(time: String) {
        settingsRepository.updateReminderTime(time)
        _message.value = "Hora de recordatorio actualizada."
    }

    fun syncNow() {
        settingsRepository.markSyncedNow()
        _message.value = "Sincronización registrada."
    }

    fun logout() {
        authRepository.logoutAndClear()
    }

    fun messageShown() {
        _message.update { null }
    }
}
