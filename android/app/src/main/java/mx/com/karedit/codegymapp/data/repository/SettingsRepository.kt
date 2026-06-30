package mx.com.karedit.codegymapp.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences("codegym_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings

    fun updateThemePreference(themePreference: ThemePreference) {
        preferences.edit().putString(KEY_THEME, themePreference.value).apply()
        _settings.update { it.copy(themePreference = themePreference) }
    }

    fun updatePushEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_PUSH_ENABLED, enabled).apply()
        _settings.update { it.copy(pushEnabled = enabled) }
    }

    fun updateReminderTime(time: String) {
        preferences.edit().putString(KEY_REMINDER_TIME, time).apply()
        _settings.update { it.copy(reminderTime = time) }
    }

    fun markSyncedNow() {
        val timestamp = System.currentTimeMillis()
        preferences.edit().putLong(KEY_LAST_SYNC_AT, timestamp).apply()
        _settings.update { it.copy(lastSyncAt = timestamp) }
    }

    fun updateChallengeStatusFilter(status: String) {
        preferences.edit().putString(KEY_CHALLENGE_STATUS_FILTER, status).apply()
        _settings.update { it.copy(challengeStatusFilter = status) }
    }

    private fun loadSettings(): AppSettings =
        AppSettings(
            themePreference = ThemePreference.fromValue(preferences.getString(KEY_THEME, null)),
            pushEnabled = preferences.getBoolean(KEY_PUSH_ENABLED, true),
            reminderTime = preferences.getString(KEY_REMINDER_TIME, null) ?: DEFAULT_REMINDER_TIME,
            lastSyncAt = preferences.getLong(KEY_LAST_SYNC_AT, 0L),
            challengeStatusFilter = preferences.getString(KEY_CHALLENGE_STATUS_FILTER, null) ?: DEFAULT_CHALLENGE_STATUS_FILTER
        )

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_PUSH_ENABLED = "push_enabled"
        const val KEY_REMINDER_TIME = "reminder_time"
        const val KEY_LAST_SYNC_AT = "last_sync_at"
        const val KEY_CHALLENGE_STATUS_FILTER = "challenge_status_filter"
        const val DEFAULT_REMINDER_TIME = "08:00"
        const val DEFAULT_CHALLENGE_STATUS_FILTER = "pending"
    }
}

data class AppSettings(
    val themePreference: ThemePreference = ThemePreference.System,
    val pushEnabled: Boolean = true,
    val reminderTime: String = "08:00",
    val lastSyncAt: Long = 0L,
    val challengeStatusFilter: String = "pending"
)

enum class ThemePreference(val value: String, val label: String) {
    System("system", "Sistema"),
    Light("light", "Claro"),
    Dark("dark", "Oscuro");

    companion object {
        fun fromValue(value: String?): ThemePreference =
            entries.firstOrNull { it.value == value } ?: System
    }
}
