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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import mx.com.karedit.codegymapp.di.AppContainer
import mx.com.karedit.codegymapp.data.repository.ThemePreference
import mx.com.karedit.codegymapp.data.security.SecureLocalDataRecovery
import mx.com.karedit.codegymapp.data.security.SecureStorageUnavailableException
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymNavHost
import mx.com.karedit.codegymapp.ui.theme.CodeGymTheme

class MainActivity : ComponentActivity() {
    private var pendingNotificationRoute by mutableStateOf<String?>(null)
    private var processLifecycleObserver: DefaultLifecycleObserver? = null
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = try {
            AppContainer(applicationContext)
        } catch (_: SecureStorageUnavailableException) {
            showSecureStorageRecovery()
            return
        }
        processLifecycleObserver = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                appContainer.syncNow()
            }
        }.also(ProcessLifecycleOwner.get().lifecycle::addObserver)
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

    private fun showSecureStorageRecovery() {
        setContent {
            CodeGymTheme(darkTheme = isSystemInDarkTheme()) {
                SecureStorageRecoveryScreen(
                    onRetry = { recreate() },
                    onReset = {
                        SecureLocalDataRecovery.reset(applicationContext).isSuccess.also { success ->
                            if (success) recreate()
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationRoute = routeFromNotification(intent)
    }

    override fun onDestroy() {
        processLifecycleObserver?.let(ProcessLifecycleOwner.get().lifecycle::removeObserver)
        processLifecycleObserver = null
        super.onDestroy()
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
private fun SecureStorageRecoveryScreen(
    onRetry: () -> Unit,
    onReset: () -> Boolean
) {
    var showConfirmation by remember { mutableStateOf(false) }
    var resetFailed by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No se pueden abrir los datos locales cifrados",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "La clave segura de Android cambió o dejó de estar disponible. Tus datos del servidor no están afectados.",
                modifier = Modifier.padding(top = 12.dp, bottom = 20.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            if (resetFailed) {
                Text(
                    text = "No fue posible restablecer los datos locales. Reinicia el teléfono e inténtalo otra vez.",
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
            TextButton(onClick = { showConfirmation = true }) {
                Text("Restablecer datos locales")
            }
        }
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("¿Restablecer datos locales?") },
            text = {
                Text(
                    "Se eliminarán la caché y los cambios offline pendientes de este teléfono. " +
                        "Los datos ya sincronizados en el servidor permanecerán intactos."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmation = false
                        resetFailed = !onReset()
                    }
                ) {
                    Text("Restablecer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
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
