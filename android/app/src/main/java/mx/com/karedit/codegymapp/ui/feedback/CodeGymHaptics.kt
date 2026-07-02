package mx.com.karedit.codegymapp.ui.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch

@Composable
fun rememberCodeGymHapticSnackbar(snackbarHostState: SnackbarHostState): (String) -> Unit {
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    return remember(snackbarHostState, hapticFeedback, scope) {
        { message ->
            scope.launch {
                snackbarHostState.showCodeGymSnackbar(message, hapticFeedback)
            }
        }
    }
}

suspend fun SnackbarHostState.showCodeGymSnackbar(
    message: String,
    hapticFeedback: HapticFeedback,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    hapticFeedback.performCodeGymFeedback(message)
    showSnackbar(message = message, duration = duration)
}

fun HapticFeedback.performCodeGymFeedback(message: String) {
    when (message.toCodeGymHapticEvent()) {
        CodeGymHapticEvent.Save,
        CodeGymHapticEvent.Complete,
        CodeGymHapticEvent.OfflineSaved,
        CodeGymHapticEvent.SyncCompleted -> performHapticFeedback(HapticFeedbackType.TextHandleMove)
        CodeGymHapticEvent.Cancel,
        CodeGymHapticEvent.CriticalError -> performHapticFeedback(HapticFeedbackType.LongPress)
        null -> Unit
    }
}

private enum class CodeGymHapticEvent {
    Save,
    Complete,
    Cancel,
    CriticalError,
    OfflineSaved,
    SyncCompleted
}

private fun String.toCodeGymHapticEvent(): CodeGymHapticEvent? {
    val value = lowercase()
    return when {
        value.contains("no se pudo") ||
            value.contains("error") ||
            value.contains("inválid") ||
            value.contains("fall") ||
            value.contains("sesión expirada") -> CodeGymHapticEvent.CriticalError
        value.contains("guardad") && value.contains("sincronizar") -> CodeGymHapticEvent.OfflineSaved
        value.contains("sincronizad") || value.contains("sincronización completada") -> CodeGymHapticEvent.SyncCompleted
        value.contains("complet") || value.contains("cumplid") -> CodeGymHapticEvent.Complete
        value.contains("cancel") ||
            value.contains("eliminad") ||
            value.contains("desactiv") ||
            value.contains("no cumplid") -> CodeGymHapticEvent.Cancel
        value.contains("guardad") ||
            value.contains("cread") ||
            value.contains("registrad") ||
            value.contains("actualizad") -> CodeGymHapticEvent.Save
        else -> null
    }
}
