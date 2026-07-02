package mx.com.karedit.codegymapp.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import mx.com.karedit.codegymapp.data.repository.AppSettings
import mx.com.karedit.codegymapp.data.repository.ThemePreference
import mx.com.karedit.codegymapp.ui.feedback.rememberCodeGymHapticSnackbar
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymSectionScaffold

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigate: (String) -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val showHapticSnackbar = rememberCodeGymHapticSnackbar(snackbarHostState)
    val scrollState = rememberScrollState()

    LaunchedEffect(message) {
        val currentMessage = message ?: return@LaunchedEffect
        showHapticSnackbar(currentMessage)
        viewModel.messageShown()
    }

    CodeGymSectionScaffold(
        onBackHome = { onNavigate(AppRoutes.Account) },
        snackbarHostState = snackbarHostState,
        collapsedTitle = "Configuración",
        isCollapsed = scrollState.value > 96,
        showFab = false
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Configuración", style = MaterialTheme.typography.displayMedium)
            Text(
                text = "Preferencias de app, seguridad, notificaciones y sesión.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ThemeSettingsCard(
                selected = settings.themePreference,
                onSelected = viewModel::updateTheme
            )
            ToggleSettingsCard(
                title = "Push",
                description = "Control local para recibir recordatorios push en el móvil.",
                checked = settings.pushEnabled,
                onCheckedChange = viewModel::updatePush
            )
            ReminderSettingsCard(
                settings = settings,
                onTimeChange = viewModel::updateReminderTime
            )
            SyncSettingsCard(
                lastSyncAt = settings.lastSyncAt,
                onSyncNow = viewModel::syncNow
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = viewModel::logout
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun ThemeSettingsCard(
    selected: ThemePreference,
    onSelected: (ThemePreference) -> Unit
) {
    SettingsCard(title = "Tema") {
        ThemePreference.entries.forEach { preference ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(preference) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == preference,
                    onClick = { onSelected(preference) }
                )
                Text(preference.label, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ToggleSettingsCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsCard(title = title) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun ReminderSettingsCard(
    settings: AppSettings,
    onTimeChange: (String) -> Unit
) {
    var reminderTime by remember(settings.reminderTime) { mutableStateOf(settings.reminderTime) }

    SettingsCard(title = "Hora de recordatorio push") {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = reminderTime,
            onValueChange = { value ->
                reminderTime = value.take(5)
                if (reminderTime.matches(Regex("""^([01]\d|2[0-3]):[0-5]\d$"""))) {
                    onTimeChange(reminderTime)
                }
            },
            label = { Text("Formato 24h") },
            placeholder = { Text("08:00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Text(
            text = "Se guardará al capturar una hora válida, por ejemplo 07:30.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SyncSettingsCard(
    lastSyncAt: Long,
    onSyncNow: () -> Unit
) {
    SettingsCard(title = "Sincronización con API") {
        Text(
            text = if (lastSyncAt > 0L) {
                "Última sincronización: ${lastSyncAt.asReadableDate()}"
            } else {
                "Aún no hay sincronización manual registrada."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSyncNow
        ) {
            Text("Sincronizar ahora")
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

private fun Long.asReadableDate(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(this))
