package mx.com.karedit.codegymapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1976D2),
    secondary = Color(0xFF079FBC),
    background = Color(0xFFF7F9FC),
    surface = Color(0xFFFFFFFF),
    error = Color(0xFFD32F2F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8492F4),
    secondary = Color(0xFF6FA8B5),
    background = Color(0xFF080808),
    surface = Color(0xFF101010),
    surfaceVariant = Color(0xFF202020),
    onSurface = Color(0xFFEDEDED),
    onSurfaceVariant = Color(0xFFA2A2A8),
    outline = Color(0xFF8C8C92),
    error = Color(0xFFFF6B74)
)

@Composable
fun CodeGymTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
