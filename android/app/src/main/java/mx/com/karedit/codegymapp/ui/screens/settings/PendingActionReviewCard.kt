package mx.com.karedit.codegymapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squareup.moshi.Moshi
import mx.com.karedit.codegymapp.data.local.entity.PendingActionEntity
import mx.com.karedit.codegymapp.data.sync.ActionTypes
import mx.com.karedit.codegymapp.domain.model.ChallengeDifficulty
import mx.com.karedit.codegymapp.domain.model.difficultyKey
import mx.com.karedit.codegymapp.ui.components.DatePickerField
import mx.com.karedit.codegymapp.ui.components.DifficultyPicker

@Composable
fun PendingActionReviewCard(
    action: PendingActionEntity,
    difficulties: List<ChallengeDifficulty>,
    onRetry: (Long, String, String) -> Unit
) {
    val fields = remember(action.payloadJson) {
        runCatching { Moshi.Builder().build().adapter(Map::class.java).fromJson(action.payloadJson) }.getOrNull()
    }
    var difficulty by remember(action.id, action.payloadJson) {
        mutableStateOf(difficultyKey(fields?.get("difficulty") as? String ?: ""))
    }
    var date by remember(action.id, action.payloadJson) {
        mutableStateOf(fields?.get("scheduledDate") as? String ?: "")
    }
    val hasDifficulty = action.type in setOf(ActionTypes.CHALLENGE_CREATE, ActionTypes.CHALLENGE_MANUAL_CREATE, ActionTypes.CHALLENGE_SAVE_DETAILS)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Acción pendiente de revisión", style = MaterialTheme.typography.titleMedium)
            Text(fields?.get("title") as? String ?: when (action.type) {
                ActionTypes.CHALLENGE_RESCHEDULE -> "Reprogramación de reto"
                ActionTypes.CHALLENGE_CANCEL -> "Cancelación de reto"
                ActionTypes.CHALLENGE_COMPLETE -> "Completar reto"
                else -> "Actualización pendiente"
            })
            Text(action.lastError, color = MaterialTheme.colorScheme.error)
            if (hasDifficulty) {
                DifficultyPicker(difficulty, difficulties, true) { difficulty = it }
            }
            if (action.type == ActionTypes.CHALLENGE_RESCHEDULE) {
                DatePickerField(date, "Nueva fecha", onDateSelected = { date = it })
            }
            Text("Corrige los datos disponibles o revisa el reto antes de reintentar.")
            Button(onClick = { onRetry(action.id, difficulty, date) }) { Text("Reintentar sincronización") }
        }
    }
}
