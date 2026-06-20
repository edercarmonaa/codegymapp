package mx.com.karedit.codegymapp.ui.screens.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.ui.components.DatePickerField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeSheet(
    viewModel: CreateChallengeViewModel,
    onCreated: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val selectedPlatform = state.platforms.firstOrNull { it.id == state.selectedPlatformId }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Nuevo reto", style = MaterialTheme.typography.headlineSmall)

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    enabled = !state.isLoading && !state.isSaving
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    state.platforms.forEach { platform ->
                        DropdownMenuItem(
                            text = { Text(platform.name) },
                            onClick = {
                                viewModel.selectPlatform(platform.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            DatePickerField(
                value = state.scheduledDate,
                label = "Fecha",
                enabled = !state.isSaving,
                onDateSelected = viewModel::updateScheduledDate
            )

            state.message?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving && state.selectedPlatformId != null,
                onClick = { viewModel.create(onCreated) }
            ) {
                Text(if (state.isSaving) "Guardando..." else "Crear reto")
            }
        }
    }
}
