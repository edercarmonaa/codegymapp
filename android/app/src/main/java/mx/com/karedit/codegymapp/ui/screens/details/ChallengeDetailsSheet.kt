package mx.com.karedit.codegymapp.ui.screens.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.domain.model.MobileChallenge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailsSheet(
    challenge: MobileChallenge,
    viewModel: ChallengeDetailsViewModel,
    isActionLoading: Boolean,
    onSaved: (String) -> Unit,
    onComplete: () -> Unit,
    onMiss: () -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    val canClose = challenge.status == "pending" || challenge.status == "expired"
    var platformMenuExpanded by remember { mutableStateOf(false) }
    val selectedPlatform = state.platforms.firstOrNull { it.id == state.platformId }

    LaunchedEffect(challenge.id) {
        viewModel.load(challenge)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(state.platformName, style = MaterialTheme.typography.headlineSmall)
            Text(
                "${state.scheduledDate} · ${challenge.statusLabel()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            ExposedDropdownMenuBox(
                expanded = platformMenuExpanded,
                onExpandedChange = { platformMenuExpanded = !platformMenuExpanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = !state.isSaving
                        )
                        .fillMaxWidth(),
                    value = selectedPlatform?.name ?: state.platformName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Plataforma") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformMenuExpanded) },
                    enabled = !state.isSaving
                )
                ExposedDropdownMenu(
                    expanded = platformMenuExpanded,
                    onDismissRequest = { platformMenuExpanded = false }
                ) {
                    state.platforms.forEach { platform ->
                        DropdownMenuItem(
                            text = { Text(platform.name) },
                            onClick = {
                                viewModel.selectPlatform(platform.id)
                                platformMenuExpanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Título") },
                singleLine = true,
                enabled = !state.isSaving
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.challengeUrl,
                onValueChange = viewModel::updateChallengeUrl,
                label = { Text("URL del reto") },
                singleLine = true,
                enabled = !state.isSaving
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.difficulty,
                onValueChange = viewModel::updateDifficulty,
                label = { Text("Dificultad") },
                singleLine = true,
                enabled = !state.isSaving
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.timeSpentMinutes,
                onValueChange = viewModel::updateTimeSpentMinutes,
                label = { Text("Tiempo practicado (min)") },
                singleLine = true,
                enabled = !state.isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("Notas") },
                minLines = 3,
                enabled = !state.isSaving
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.githubLinks,
                onValueChange = viewModel::updateGithubLinks,
                label = { Text("GitHub") },
                placeholder = { Text("Un enlace por línea") },
                minLines = 3,
                enabled = !state.isSaving
            )

            if (state.languages.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Lenguajes", style = MaterialTheme.typography.titleMedium)
                    state.languages.forEach { language ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = state.selectedLanguageIds.contains(language.id),
                                onCheckedChange = { viewModel.toggleLanguage(language.id) },
                                enabled = !state.isSaving
                            )
                            Text(language.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            if (state.challengeUrl.isNotBlank()) {
                TextButton(onClick = { uriHandler.openUri(state.challengeUrl) }) {
                    Text("Abrir reto")
                }
            }

            state.message?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
                onClick = { viewModel.save(onSaved) }
            ) {
                Text(if (state.isSaving) "Guardando..." else "Guardar cambios")
            }

            if (canClose) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = !isActionLoading && !state.isSaving,
                        onClick = onComplete
                    ) {
                        Text("Completar")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = !isActionLoading && !state.isSaving,
                        onClick = onMiss
                    ) {
                        Text("No realizado")
                    }
                }
            }
        }
    }
}

private fun MobileChallenge.statusLabel(): String =
    when (status) {
        "pending" -> "Pendiente"
        "expired" -> "Vencido"
        "completed" -> "Completado"
        "missed" -> "No realizado"
        "cancelled" -> "Cancelado"
        else -> status
    }
