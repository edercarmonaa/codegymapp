package mx.com.karedit.codegymapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import mx.com.karedit.codegymapp.di.AppContainer
import mx.com.karedit.codegymapp.ui.navigation.CodeGymNavHost
import mx.com.karedit.codegymapp.ui.theme.CodeGymTheme

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(applicationContext)
        requestNotificationPermissionIfNeeded()
        appContainer.fcmTokenRegistrar.registerCurrentToken()

        setContent {
            CodeGymTheme {
                Surface {
                    CodeGymNavHost(appContainer = appContainer)
                }
            }
        }
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
}
