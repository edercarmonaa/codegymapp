package mx.com.karedit.codegymapp.ui.screens.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCreateActionSheet(
    onScheduleChallenge: () -> Unit,
    onRegisterCompletedChallenge: () -> Unit,
    onCreateRoutine: () -> Unit,
    onCreateGoal: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Nueva acción", style = MaterialTheme.typography.headlineSmall)
            QuickActionButton("Programar reto", onScheduleChallenge)
            QuickActionButton("Registrar reto realizado", onRegisterCompletedChallenge)
            QuickActionButton("Crear rutina", onCreateRoutine)
            QuickActionButton("Crear meta", onCreateGoal)
        }
    }
}

@Composable
private fun QuickActionButton(label: String, onClick: () -> Unit) {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
