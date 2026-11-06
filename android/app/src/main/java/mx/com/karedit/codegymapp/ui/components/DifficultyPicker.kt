package mx.com.karedit.codegymapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import mx.com.karedit.codegymapp.domain.model.ChallengeDifficulty
import mx.com.karedit.codegymapp.domain.model.difficultyKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyPicker(
    value: String,
    options: List<ChallengeDifficulty>,
    enabled: Boolean,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = options.firstOrNull { it.value == difficultyKey(value) }
    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled),
            value = selected?.label ?: value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Dificultad") },
            placeholder = { Text("Selecciona una dificultad") },
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Sin seleccionar") }, onClick = { onSelected(""); expanded = false })
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option.label) }, onClick = {
                    onSelected(option.value)
                    expanded = false
                })
            }
        }
    }
}
