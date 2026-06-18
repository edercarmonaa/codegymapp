package mx.com.karedit.codegymapp.ui.screens.create

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.ui.components.DatePickerField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineSheet(
    viewModel: CreateRoutineViewModel,
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
            Text("Nueva rutina", style = MaterialTheme.typography.headlineSmall)

            ExposedDropdownMenuBox(
                expanded = platformExpanded,
                onExpandedChange = { platformExpanded = !platformExpanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
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

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoutineFrequency.entries.forEach { frequency ->
                    FilterChip(
                        selected = state.frequency == frequency,
                        onClick = { viewModel.selectFrequency(frequency) },
                        label = { Text(frequency.label) },
                        enabled = !state.isSaving
                    )
                }
            }

            DatePickerField(
                value = state.startDate,
                label = "Fecha de inicio",
                enabled = !state.isSaving,
                onDateSelected = viewModel::updateStartDate
            )

            if (state.endDate.isBlank()) {
                TextButton(
                    enabled = !state.isSaving,
                    onClick = { viewModel.updateEndDate(state.startDate) }
                ) {
                    Text("Agregar fecha final")
                }
            } else {
                DatePickerField(
                    value = state.endDate,
                    label = "Fecha final",
                    enabled = !state.isSaving,
                    onDateSelected = viewModel::updateEndDate
                )
                TextButton(
                    enabled = !state.isSaving,
                    onClick = viewModel::clearEndDate
                ) {
                    Text("Quitar fecha final")
                }
            }

            if (state.frequency == RoutineFrequency.Weekly) {
                Text("Días de semana", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weekDays.forEach { day ->
                        FilterChip(
                            selected = day.value in state.weekDays,
                            onClick = { viewModel.toggleWeekDay(day.value) },
                            label = { Text(day.label) },
                            enabled = !state.isSaving
                        )
                    }
                }
            }

            if (state.frequency == RoutineFrequency.Monthly) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.monthDay,
                    onValueChange = viewModel::updateMonthDay,
                    label = { Text("Día del mes") },
                    singleLine = true,
                    enabled = !state.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            state.message?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving && state.selectedPlatformId != null,
                onClick = { viewModel.create(onCreated) }
            ) {
                Text(if (state.isSaving) "Guardando..." else "Crear rutina")
            }
        }
    }
}

private data class WeekDay(val value: Int, val label: String)

private val weekDays = listOf(
    WeekDay(1, "Lun"),
    WeekDay(2, "Mar"),
    WeekDay(3, "Mié"),
    WeekDay(4, "Jue"),
    WeekDay(5, "Vie"),
    WeekDay(6, "Sáb"),
    WeekDay(7, "Dom")
)
