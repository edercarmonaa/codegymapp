package mx.com.karedit.codegymapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import mx.com.karedit.codegymapp.di.AppContainer
import mx.com.karedit.codegymapp.ui.navigation.CodeGymNavHost
import mx.com.karedit.codegymapp.ui.theme.CodeGymTheme

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(applicationContext)

        setContent {
            CodeGymTheme {
                Surface {
                    CodeGymNavHost(appContainer = appContainer)
                }
            }
        }
    }
}
