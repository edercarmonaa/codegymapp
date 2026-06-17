package mx.com.karedit.codegymapp.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import mx.com.karedit.codegymapp.ui.components.ToDoTaskCard
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymDrawerScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var todayExpanded by remember { mutableStateOf(true) }
    var expiredExpanded by remember { mutableStateOf(false) }
    var selectedChallenge by remember { mutableStateOf<MobileChallenge?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short
        )
        viewModel.snackbarShown()
    }

    CodeGymDrawerScaffold(
        title = "Mi día",
        selectedRoute = AppRoutes.Today,
        onNavigate = onNavigate,
        snackbarHostState = snackbarHostState,
        todayCount = state.todayChallenges.size + state.expiredChallenges.size
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Hola${state.user?.name?.let { ", $it" } ?: ""}",
                style = MaterialTheme.typography.headlineSmall
            )

            if (state.isLoading) {
                TodayLoading()
            } else {
                ChallengeSection(
                    title = "Pendientes de hoy",
                    count = state.todayChallenges.size,
                    expanded = todayExpanded,
                    emptyText = "Sin retos pendientes para hoy.",
                    challenges = state.todayChallenges,
                    onToggle = { todayExpanded = !todayExpanded },
                    onChallengeClick = { selectedChallenge = it }
                )
                ChallengeSection(
                    title = "Vencidos pendientes",
                    count = state.expiredChallenges.size,
                    expanded = expiredExpanded,
                    emptyText = "Sin retos vencidos por revisar.",
                    challenges = state.expiredChallenges,
                    onToggle = { expiredExpanded = !expiredExpanded },
                    onChallengeClick = { selectedChallenge = it }
                )
            }
        }
    }

    selectedChallenge?.let { challenge ->
        ChallengeDetailSheet(
            challenge = challenge,
            isActionLoading = state.isActionLoading,
            onComplete = {
                viewModel.completeChallenge(challenge.id)
                selectedChallenge = null
            },
            onMiss = {
                viewModel.missChallenge(challenge.id)
                selectedChallenge = null
            },
            onDismiss = { selectedChallenge = null }
        )
    }
}

@Composable
private fun TodayLoading() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator()
        repeat(3) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LoadingLine(widthFraction = 0.45f)
                    LoadingLine(widthFraction = 0.75f)
                }
            }
        }
    }
}

@Composable
private fun LoadingLine(widthFraction: Float) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(14.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun ChallengeSection(
    title: String,
    count: Int,
    expanded: Boolean,
    emptyText: String,
    challenges: List<MobileChallenge>,
    onToggle: () -> Unit,
    onChallengeClick: (MobileChallenge) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text("$count reto${if (count == 1) "" else "s"}", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = onToggle) {
                    Text(if (expanded) "Ocultar" else "Ver")
                }
            }

            if (expanded) {
                if (challenges.isEmpty()) {
                    Text(emptyText, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        challenges.forEach { challenge ->
                            ToDoTaskCard(
                                challenge = challenge,
                                onClick = { onChallengeClick(challenge) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChallengeDetailSheet(
    challenge: MobileChallenge,
    isActionLoading: Boolean,
    onComplete: () -> Unit,
    onMiss: () -> Unit,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val canClose = challenge.status == "pending" || challenge.status == "expired"

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(challenge.platformName, style = MaterialTheme.typography.headlineSmall)
            Text(challenge.statusLabel(), style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider()

            DetailRow(label = "Fecha", value = challenge.scheduledDate)
            if (challenge.title.isNotBlank()) {
                DetailRow(label = "Reto", value = challenge.title)
            }
            if (challenge.difficulty.isNotBlank()) {
                DetailRow(label = "Dificultad", value = challenge.difficulty)
            }
            if (challenge.timeSpentMinutes > 0) {
                DetailRow(label = "Tiempo", value = "${challenge.timeSpentMinutes} min")
            }
            if (challenge.notes.isNotBlank()) {
                DetailRow(label = "Notas", value = challenge.notes)
            }
            if (challenge.isRescheduled) {
                DetailRow(label = "Reprogramado", value = "Sí")
            }
            if (!challenge.challengeUrl.isNullOrBlank()) {
                TextButton(onClick = { uriHandler.openUri(challenge.challengeUrl) }) {
                    Text("Abrir reto")
                }
            }
            if (canClose) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = !isActionLoading,
                        onClick = onComplete
                    ) {
                        Text("Completar")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = !isActionLoading,
                        onClick = onMiss
                    ) {
                        Text("No realizado")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
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
