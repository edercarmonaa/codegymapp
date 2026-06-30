package mx.com.karedit.codegymapp.ui.screens.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import mx.com.karedit.codegymapp.ui.components.DatePickerField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeQuickActionsSheet(
    challenge: MobileChallenge,
    onReschedule: (String) -> Unit,
    onMiss: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    var showReschedule by remember(challenge.id) { mutableStateOf(false) }
    var rescheduleDate by remember(challenge.id) { mutableStateOf(challenge.scheduledDate) }
    var confirmMiss by remember(challenge.id) { mutableStateOf(false) }
    var confirmCancel by remember(challenge.id) { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(challenge.platformName, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Acciones rápidas",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (showReschedule) {
                DatePickerField(
                    value = rescheduleDate,
                    label = "Nueva fecha",
                    onDateSelected = { rescheduleDate = it }
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = rescheduleDate.isNotBlank() && rescheduleDate != challenge.scheduledDate,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onReschedule(rescheduleDate)
                    }
                ) {
                    Text("Guardar nueva fecha")
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showReschedule = false }
                ) {
                    Text("Volver")
                }
            } else {
                ActionButton("Reprogramar") { showReschedule = true }
                ActionButton("No cumplido") {
                    if (challenge.status == "expired") {
                        confirmMiss = true
                    } else {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMiss()
                    }
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { confirmCancel = true }
                ) {
                    Text("Cancelar")
                }
            }
        }
    }

    if (confirmMiss) {
        ConfirmDialog(
            title = "Marcar vencido como no cumplido",
            message = "Este reto vencido se cerrará como no cumplido. ¿Quieres continuar?",
            confirmText = "Sí, marcar",
            onConfirm = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onMiss()
            },
            onDismiss = { confirmMiss = false }
        )
    }

    if (confirmCancel) {
        ConfirmDialog(
            title = "Cancelar reto",
            message = "El reto se cerrará como cancelado. ¿Quieres continuar?",
            confirmText = "Sí, cancelar",
            onConfirm = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onCancel()
            },
            onDismiss = { confirmCancel = false }
        )
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onConfirm()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Volver")
            }
        }
    )
}
