package mx.com.karedit.codegymapp.ui.screens.create

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterCompletedChallengeSheet(
    viewModel: RegisterCompletedChallengeViewModel,
    onCreated: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var platformExpanded by remember { mutableStateOf(false) }
    val selectedPlatform = state.platforms.firstOrNull { it.id == state.selectedPlatformId }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Registrar reto realizado", style = MaterialTheme.typography.headlineSmall)

            ExposedDropdownMenuBox(
                expanded = platformExpanded,
                onExpandedChange = { platformExpanded = !platformExpanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = !state.isLoading && !state.isSaving
                        )
                        .fillMaxWidth(),
                    value = selectedPlatform?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Plataforma") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformExpanded) },
                    enabled = !state.isLoading && !state.isSaving
                )
                ExposedDropdownMenu(
                    expanded = platformExpanded,
                    onDismissRequest = { platformExpanded = false }
                ) {
                    state.platforms.forEach { platform ->
                        DropdownMenuItem(
                            text = { Text(platform.name) },
                            onClick = {
                                viewModel.selectPlatform(platform.id)
                                platformExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Nombre del reto") },
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
                label = { Text("Tiempo invertido (min)") },
                singleLine = true,
                enabled = !state.isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.challengeUrl,
                onValueChange = viewModel::updateChallengeUrl,
                label = { Text("URL del reto") },
                singleLine = true,
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
                                checked = language.id in state.selectedLanguageIds,
                                onCheckedChange = { viewModel.toggleLanguage(language.id) },
                                enabled = !state.isSaving
                            )
                            Text(language.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.githubLinks,
                onValueChange = viewModel::updateGithubLinks,
                label = { Text("GitHub") },
                placeholder = { Text("Un enlace por línea") },
                minLines = 3,
                enabled = !state.isSaving
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("Notas") },
                minLines = 3,
                enabled = !state.isSaving
            )

            state.message?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
                onClick = { viewModel.create(onCreated) }
            ) {
                Text(if (state.isSaving) "Guardando..." else "Registrar realizado")
            }
        }
    }
}
