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
import mx.com.karedit.codegymapp.data.repository.MobileGoalOption
import mx.com.karedit.codegymapp.domain.model.MobileLanguage
import mx.com.karedit.codegymapp.domain.model.MobilePlatform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalSheet(
    viewModel: CreateGoalViewModel,
    onCreated: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isEditing = state.editingGoalId != null

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(if (isEditing) "Editar meta" else "Crear meta", style = MaterialTheme.typography.headlineSmall)

            GoalOptionDropdown(
                label = "Tipo de meta",
                options = state.goalTypes,
                selectedValue = state.goalType,
                enabled = !state.isSaving,
                onSelected = viewModel::selectGoalType
            )
            GoalOptionDropdown(
                label = "Periodo",
                options = state.periodTypes,
                selectedValue = state.periodType,
                enabled = !state.isSaving,
                onSelected = viewModel::selectPeriodType
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.targetValue,
                onValueChange = viewModel::updateTargetValue,
                label = { Text("Objetivo") },
                singleLine = true,
                enabled = !state.isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            PlatformDropdown(
                selectedId = state.platformId,
                platforms = state.platforms,
                enabled = !state.isSaving,
                onSelected = viewModel::selectPlatform
            )
            LanguageDropdown(
                selectedId = state.languageId,
                languages = state.languages,
                enabled = !state.isSaving,
                onSelected = viewModel::selectLanguage
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.autoRenew,
                    onCheckedChange = viewModel::updateAutoRenew,
                    enabled = !state.isSaving
                )
                Text("Renovar automáticamente", style = MaterialTheme.typography.bodyLarge)
            }

            state.message?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving && state.goalType.isNotBlank() && state.periodType.isNotBlank(),
                onClick = { viewModel.save(onCreated) }
            ) {
                Text(
                    when {
                        state.isSaving -> "Guardando..."
                        isEditing -> "Guardar meta"
                        else -> "Crear meta"
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalOptionDropdown(
    label: String,
    options: List<MobileGoalOption>,
    selectedValue: String,
    enabled: Boolean,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = options.firstOrNull { it.value == selectedValue }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = enabled)
                .fillMaxWidth(),
            value = selected?.label ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = enabled
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option.value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlatformDropdown(
    selectedId: Int,
    platforms: List<MobilePlatform>,
    enabled: Boolean,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = platforms.firstOrNull { it.id == selectedId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = enabled)
                .fillMaxWidth(),
            value = selected?.name ?: "Todas las plataformas",
            onValueChange = {},
            readOnly = true,
            label = { Text("Plataforma") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = enabled
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Todas las plataformas") },
                onClick = {
                    onSelected(0)
                    expanded = false
                }
            )
            platforms.forEach { platform ->
                DropdownMenuItem(
                    text = { Text(platform.name) },
                    onClick = {
                        onSelected(platform.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    selectedId: Int,
    languages: List<MobileLanguage>,
    enabled: Boolean,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = languages.firstOrNull { it.id == selectedId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = enabled)
                .fillMaxWidth(),
            value = selected?.name ?: "Todos los lenguajes",
            onValueChange = {},
            readOnly = true,
            label = { Text("Lenguaje") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = enabled
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Todos los lenguajes") },
                onClick = {
                    onSelected(0)
                    expanded = false
                }
            )
            languages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.name) },
                    onClick = {
                        onSelected(language.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
