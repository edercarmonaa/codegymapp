package mx.com.karedit.codegymapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.domain.model.MobileChallenge

@Composable
fun ToDoTaskCard(
    challenge: MobileChallenge,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onCompleteClick: (() -> Unit)? = null
) {
    val isCompleted = challenge.status == "completed"
    val metadataColor = when (challenge.status) {
        "expired", "missed", "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val clickableModifier = onClick?.let { modifier.clickable(onClick = it) } ?: modifier

    Card(
        modifier = clickableModifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusCircle(
                isCompleted = isCompleted,
                onClick = if (!isCompleted) onCompleteClick else null
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = challenge.platformName,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    text = challenge.metadataLabel(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = metadataColor
                )
            }
        }
    }
}

@Composable
private fun StatusCircle(isCompleted: Boolean, onClick: (() -> Unit)?) {
    val color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val modifier = onClick?.let { Modifier.size(34.dp).clickable(onClick = it) } ?: Modifier.size(34.dp)

    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = modifier) {
            drawCircle(
                color = color,
                style = if (isCompleted) Fill else Stroke(width = 3.dp.toPx())
            )
        }
        if (isCompleted) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )
        }
    }
}

private fun MobileChallenge.metadataLabel(): String {
    val date = if (scheduledDate.isBlank()) "Sin fecha" else scheduledDate
    val recurrence = if (isRescheduled) " ↻" else ""
    return "CodeGymApp · $date$recurrence"
}
