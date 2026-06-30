package mx.com.karedit.codegymapp.ui.screens.notifications

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.domain.model.MobileNotification
import mx.com.karedit.codegymapp.ui.components.ListSkeleton
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymSectionScaffold
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        viewModel.snackbarShown()
    }

    CodeGymSectionScaffold(
        onBackHome = { onNavigate(AppRoutes.Home) },
        snackbarHostState = snackbarHostState,
        collapsedTitle = "Notificaciones",
        collapsedSubtitle = "${state.unreadCount} sin leer",
        isCollapsed = scrollState.value > 96,
        showFab = false
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Notificaciones", style = MaterialTheme.typography.displayMedium)
            Text(
                text = "${state.unreadCount} sin leer",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                state.isLoading -> ListSkeleton(count = 4)
                state.notifications.isEmpty() -> Text("No hay avisos pendientes.", style = MaterialTheme.typography.bodyLarge)
                else -> {
                    val unread = state.notifications.filterNot { it.isRead }
                    val history = state.notifications.filter { it.isRead }

                    NotificationSection(
                        title = "No leídas",
                        emptyText = "No hay notificaciones nuevas.",
                        notifications = unread,
                        onMarkRead = viewModel::markRead,
                        onDelete = viewModel::delete
                    )
                    NotificationSection(
                        title = "Historial",
                        emptyText = "No hay notificaciones leídas.",
                        notifications = history,
                        onMarkRead = viewModel::markRead,
                        onDelete = viewModel::delete
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationSection(
    title: String,
    emptyText: String,
    notifications: List<MobileNotification>,
    onMarkRead: (MobileNotification) -> Unit,
    onDelete: (MobileNotification) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        if (notifications.isEmpty()) {
            Text(emptyText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            notifications.forEach { notification ->
                NotificationCard(
                    notification = notification,
                    onMarkRead = { onMarkRead(notification) },
                    onDelete = { onDelete(notification) }
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: MobileNotification,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit
) {
    val canMarkRead = !notification.isRead
    val canDelete = notification.isRead
    val swipeOffset = remember(notification.id, notification.isRead) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cardWidthPx = with(density) { maxWidth.toPx() }
        val threshold = cardWidthPx * 0.60f

        NotificationSwipeBackground(offsetX = swipeOffset.value)
        NotificationCardContent(
            notification = notification,
            modifier = Modifier
                .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                .pointerInput(canMarkRead, canDelete, cardWidthPx) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val minOffset = if (canDelete) -cardWidthPx else 0f
                            val maxOffset = if (canMarkRead) cardWidthPx else 0f
                            val nextOffset = (swipeOffset.value + dragAmount)
                                .coerceIn(minOffset, maxOffset)
                            scope.launch { swipeOffset.snapTo(nextOffset) }
                        },
                        onDragEnd = {
                            val currentOffset = swipeOffset.value
                            when {
                                currentOffset >= threshold && canMarkRead -> onMarkRead()
                                currentOffset <= -threshold && canDelete -> onDelete()
                            }
                            scope.launch { swipeOffset.animateTo(0f) }
                        },
                        onDragCancel = {
                            scope.launch { swipeOffset.animateTo(0f) }
                        }
                    )
                }
        )
    }
}

@Composable
private fun NotificationCardContent(
    notification: MobileNotification,
    modifier: Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (!notification.isRead) {
                    Text(
                        text = "Nuevo",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(notification.message, style = MaterialTheme.typography.bodyLarge)
            if (!notification.isRead) {
                Text(
                    text = "Desliza a la derecha para marcar como leída",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "Desliza a la izquierda para eliminar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (notification.createdAt.isNotBlank()) {
                Text(
                    text = notification.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NotificationSwipeBackground(offsetX: Float) {
    val isMarkingRead = offsetX > 0f
    val isDeleting = offsetX < 0f
    val backgroundColor = when {
        isMarkingRead -> Color(0xFF2E7D50)
        isDeleting -> Color(0xFF8E1D2D)
        else -> Color.Transparent
    }
    val alignment = if (isMarkingRead) Alignment.CenterStart else Alignment.CenterEnd
    val label = when {
        isMarkingRead -> "Leída"
        isDeleting -> "Eliminar"
        else -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
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
