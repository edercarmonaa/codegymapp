package mx.com.karedit.codegymapp.core.notifications

import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.BuildConfig
import mx.com.karedit.codegymapp.data.repository.AuthRepository
import mx.com.karedit.codegymapp.data.repository.DeviceTokenRepository
import mx.com.karedit.codegymapp.data.repository.SettingsRepository

class FcmTokenRegistrar(
    private val authRepository: AuthRepository,
    private val deviceTokenRepository: DeviceTokenRepository,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope
) {
    fun registerCurrentToken() {
        if (!authRepository.hasToken()) {
            return
        }

        try {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token -> registerToken(token) }
        } catch (_: IllegalStateException) {
            // Firebase is not configured yet. Registration will start once google-services.json is added.
        }
    }

    fun registerToken(token: String) {
        if (token.isBlank() || !authRepository.hasToken()) {
            return
        }

        scope.launch {
            val settings = settingsRepository.settings.value
            deviceTokenRepository.store(
                token = token,
                deviceName = deviceName(),
                appVersion = BuildConfig.VERSION_NAME,
                pushEnabled = settings.pushEnabled,
                reminderTime = settings.reminderTime
            )
        }
    }

    private fun deviceName(): String =
        listOf(Build.MANUFACTURER, Build.MODEL)
            .joinToString(" ")
            .trim()
            .ifBlank { "Android" }
}
