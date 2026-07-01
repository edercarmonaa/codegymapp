package mx.com.karedit.codegymapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import mx.com.karedit.codegymapp.di.AppContainer
import mx.com.karedit.codegymapp.data.repository.ThemePreference
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymNavHost
import mx.com.karedit.codegymapp.ui.theme.CodeGymTheme

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer
    private var pendingNotificationRoute by mutableStateOf<String?>(null)
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(applicationContext)
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                appContainer.syncNow()
            }
        })
        pendingNotificationRoute = routeFromNotification(intent)
        requestNotificationPermissionIfNeeded()
        appContainer.fcmTokenRegistrar.registerCurrentToken()

        setContent {
            val settings by appContainer.settingsRepository.settings.collectAsState()
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (settings.themePreference) {
                ThemePreference.System -> systemDarkTheme
                ThemePreference.Light -> false
                ThemePreference.Dark -> true
            }

            CodeGymTheme(darkTheme = darkTheme) {
                val isOnline by appContainer.networkMonitor.isOnline.collectAsState()
                LaunchedEffect(appContainer.sessionManager) {
                    while (true) {
                        delay(30_000)
                        appContainer.sessionManager.expireIfInactive()
                    }
                }

                Surface(
                    modifier = Modifier.pointerInput(appContainer.sessionManager) {
                        awaitEachGesture {
                            awaitPointerEvent()
                            appContainer.sessionManager.recordInteraction()
                        }
                    }
                ) {
                    Column {
                        if (!isOnline) {
                            OfflineBanner()
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CodeGymNavHost(
                                appContainer = appContainer,
                                pendingNotificationRoute = pendingNotificationRoute,
                                onPendingNotificationRouteHandled = { pendingNotificationRoute = null }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationRoute = routeFromNotification(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        val isGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            notificationPermissionLauncher.launch(permission)
        }
    }

    private fun routeFromNotification(intent: Intent?): String? {
        return when (intent?.getStringExtra(EXTRA_NOTIFICATION_SCREEN) ?: intent?.getStringExtra("screen")) {
            "challenges_expired" -> AppRoutes.ChallengesExpired
            "today" -> AppRoutes.Today
            "notifications" -> AppRoutes.Notifications
            else -> when (intent?.getStringExtra(EXTRA_NOTIFICATION_TYPE) ?: intent?.getStringExtra("type")) {
                "expired_review_reminder" -> AppRoutes.ChallengesExpired
                "today_reminder" -> AppRoutes.Today
                "test" -> AppRoutes.Notifications
                else -> null
            }
        }
    }

    private companion object {
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_NOTIFICATION_SCREEN = "notification_screen"
    }
}

@androidx.compose.runtime.Composable
private fun OfflineBanner() {
    Text(
        text = "Sin conexión a internet",
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.onErrorContainer,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.bodyMedium
    )
}
