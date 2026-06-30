package mx.com.karedit.codegymapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import kotlin.math.roundToInt

@Composable
fun ToDoTaskCard(
    challenge: MobileChallenge,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onCompleteClick: (() -> Unit)? = null,
    onActionsClick: (() -> Unit)? = null
) {
    val isCompleted = challenge.status == "completed"
    val canChangeStatus = challenge.status == "pending" || challenge.status == "expired"
    val canSwipeToComplete = canChangeStatus && onCompleteClick != null
    val canSwipeToActions = canChangeStatus && onActionsClick != null

    if (canSwipeToComplete || canSwipeToActions) {
        val swipeOffset = remember(challenge.id) { Animatable(0f) }
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current
        val hapticFeedback = LocalHapticFeedback.current

        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            val cardWidthPx = with(density) { maxWidth.toPx() }
            val threshold = cardWidthPx * 0.60f

            SwipeBackground(offsetX = swipeOffset.value)
            TaskCardContent(
                challenge = challenge,
                modifier = Modifier
                    .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                    .pointerInput(canSwipeToComplete, canSwipeToActions, cardWidthPx) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                val minOffset = if (canSwipeToActions) -cardWidthPx else 0f
                                val maxOffset = if (canSwipeToComplete) cardWidthPx else 0f
                                val nextOffset = (swipeOffset.value + dragAmount)
                                    .coerceIn(minOffset, maxOffset)
                                scope.launch { swipeOffset.snapTo(nextOffset) }
                            },
                            onDragEnd = {
                                val currentOffset = swipeOffset.value
                                when {
                                    currentOffset >= threshold && canSwipeToComplete -> {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onCompleteClick?.invoke()
                                    }
                                    currentOffset <= -threshold && canSwipeToActions -> {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onActionsClick?.invoke()
                                    }
                                }
                                scope.launch { swipeOffset.animateTo(0f) }
                            },
                            onDragCancel = {
                                scope.launch { swipeOffset.animateTo(0f) }
                            }
                        )
                    },
                isCompleted = isCompleted,
                onClick = onClick,
                onCompleteClick = onCompleteClick
            )
        }
    } else {
        TaskCardContent(
            challenge = challenge,
            modifier = modifier,
            isCompleted = isCompleted,
            onClick = onClick,
            onCompleteClick = onCompleteClick
        )
    }
}

@Composable
private fun TaskCardContent(
    challenge: MobileChallenge,
    modifier: Modifier,
    isCompleted: Boolean,
    onClick: (() -> Unit)?,
    onCompleteClick: (() -> Unit)?
) {
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
            Column(modifier = Modifier.weight(1f)) {
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
            }
        }
    }
}

@Composable
private fun SwipeBackground(offsetX: Float) {
    val isCompleting = offsetX > 0f
    val isMissing = offsetX < 0f
    val backgroundColor = when {
        isCompleting -> Color(0xFF2E7D50)
        isMissing -> Color(0xFF8E1D2D)
        else -> Color.Transparent
    }
    val alignment = if (isCompleting) Alignment.CenterStart else Alignment.CenterEnd
    val label = when {
        isCompleting -> "Cumplido"
        isMissing -> "Acciones"
        else -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 22.dp),
        contentAlignment = alignment
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
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
