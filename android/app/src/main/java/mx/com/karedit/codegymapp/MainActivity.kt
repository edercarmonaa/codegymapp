package mx.com.karedit.codegymapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import mx.com.karedit.codegymapp.di.AppContainer
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
        pendingNotificationRoute = routeFromNotification(intent)
        requestNotificationPermissionIfNeeded()
        appContainer.fcmTokenRegistrar.registerCurrentToken()

        setContent {
            CodeGymTheme {
                Surface {
                    CodeGymNavHost(
                        appContainer = appContainer,
                        pendingNotificationRoute = pendingNotificationRoute,
                        onPendingNotificationRouteHandled = { pendingNotificationRoute = null }
                    )
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
        return when (intent?.getStringExtra(EXTRA_NOTIFICATION_TYPE) ?: intent?.getStringExtra("type")) {
            "expired_review_reminder" -> AppRoutes.Challenges
            "today_reminder" -> AppRoutes.Today
            else -> null
        }
    }

    private companion object {
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
    }
}
