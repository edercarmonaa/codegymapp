package mx.com.karedit.codegymapp.ui.screens.challenges

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    viewModel: ChallengesViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        viewModel.snackbarShown()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Retos") })
        },
        bottomBar = {
            CodeGymBottomBar(
                selectedRoute = AppRoutes.Challenges,
                onRouteSelected = onNavigate
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Mes ${state.month}", style = MaterialTheme.typography.headlineSmall)
            StatusFilters(
                selected = state.status,
                onSelected = viewModel::selectStatus
            )

            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.challenges.isEmpty()) {
                Text("No hay retos para este filtro.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.challenges.forEach { challenge ->
                        ChallengeListCard(challenge = challenge)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFilters(
    selected: ChallengeStatusFilter,
    onSelected: (ChallengeStatusFilter) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChallengeStatusFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(filter.label) }
            )
        }
    }
}

@Composable
private fun ChallengeListCard(challenge: MobileChallenge) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(challenge.platformName, style = MaterialTheme.typography.titleSmall)
            Text(challenge.scheduledDate, style = MaterialTheme.typography.bodySmall)
            if (challenge.title.isNotBlank()) {
                Text(challenge.title, style = MaterialTheme.typography.bodySmall)
            }
            Text(challenge.statusLabel(), style = MaterialTheme.typography.labelMedium)
        }
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
