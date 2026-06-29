package mx.com.karedit.codegymapp.ui.screens.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun BiometricUnlockScreen(
    onUnlocked: () -> Unit,
    onBiometricFatalError: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var message by remember { mutableStateOf<String?>(null) }
    var showExplicitBiometricButton by remember { mutableStateOf(false) }

    fun showPrompt() {
        showExplicitBiometricButton = false
        if (activity == null) {
            val errorMessage = "No se pudo iniciar la verificación biométrica."
            message = errorMessage
            onBiometricFatalError(errorMessage)
            return
        }

        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(authenticators)

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            val errorMessage = "Huella no disponible. Inicia sesión con usuario y contraseña."
            message = errorMessage
            onBiometricFatalError(errorMessage)
            return
        }

        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    message = null
                    onUnlocked()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode.isRecoverableBiometricCancellation()) {
                        message = "Verificación cancelada. Puedes intentar entrar con biometría de nuevo."
                        showExplicitBiometricButton = true
                        return
                    }

                    val errorMessage = errString
                        .toString()
                        .ifBlank { "No se pudo verificar la huella. Inicia sesión con contraseña." }
                    message = errorMessage
                    onBiometricFatalError(errorMessage)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    message = "No se pudo verificar la huella. Intenta de nuevo."
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear CodeGym")
            .setSubtitle("Confirma tu huella para continuar")
            .setAllowedAuthenticators(authenticators)
            .setNegativeButtonText("Cancelar")
            .build()

        prompt.authenticate(promptInfo)
    }

    LaunchedEffect(Unit) {
        showPrompt()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "CodeGym",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Inicio de sesión con huella",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        message?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
        }

        BiometricAccessButton(
            showProminent = showExplicitBiometricButton,
            onClick = ::showPrompt
        )
    }
}

@Composable
private fun BiometricAccessButton(
    showProminent: Boolean,
    onClick: () -> Unit
) {
    if (showProminent) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Acceso protegido", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Tu sesión sigue protegida. Usa tu huella para continuar sin escribir credenciales.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    onClick = onClick
                ) {
                    Text("Entrar con biometría", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    } else {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick
        ) {
            Text("Entrar con biometría")
        }
    }
}

private fun Int.isRecoverableBiometricCancellation(): Boolean =
    this == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
        this == BiometricPrompt.ERROR_USER_CANCELED ||
        this == BiometricPrompt.ERROR_CANCELED ||
        this == BiometricPrompt.ERROR_TIMEOUT
