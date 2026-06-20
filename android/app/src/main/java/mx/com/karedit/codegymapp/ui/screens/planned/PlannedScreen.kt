package mx.com.karedit.codegymapp.ui.screens.planned

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import mx.com.karedit.codegymapp.ui.components.ToDoTaskCard
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymSectionScaffold
import mx.com.karedit.codegymapp.ui.screens.create.CreateChallengeSheet
import mx.com.karedit.codegymapp.ui.screens.create.CreateChallengeViewModel
import mx.com.karedit.codegymapp.ui.screens.details.ChallengeDetailsSheet
import mx.com.karedit.codegymapp.ui.screens.details.ChallengeDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannedScreen(
    viewModel: PlannedViewModel,
    createChallengeViewModel: CreateChallengeViewModel,
    challengeDetailsViewModel: ChallengeDetailsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateSheet by remember { mutableStateOf(false) }
    var selectedChallenge by remember { mutableStateOf<MobileChallenge?>(null) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short
        )
        viewModel.snackbarShown()
    }

    CodeGymSectionScaffold(
        onBackHome = { onNavigate(AppRoutes.Home) },
        snackbarHostState = snackbarHostState,
        collapsedTitle = "Planeado",
        collapsedSubtitle = "Todo planeado",
        isCollapsed = scrollState.value > 96,
        onFabClick = { showCreateSheet = true }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Planeado", style = MaterialTheme.typography.displayMedium)
            Text("Todo planeado", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.challenges.isEmpty()) {
                Text("No hay retos pendientes próximos.", style = MaterialTheme.typography.bodyMedium)
            } else {
                state.challenges
                    .groupBy { it.scheduledDate }
                    .forEach { (date, challenges) ->
                        PlannedDateGroup(
                            date = date,
                            challenges = challenges,
                            onChallengeClick = { selectedChallenge = it },
                            onComplete = viewModel::completeChallenge,
                            onMiss = viewModel::missChallenge
                        )
                    }
            }
        }
    }

    if (showCreateSheet) {
        CreateChallengeSheet(
            viewModel = createChallengeViewModel,
            onCreated = { message ->
                showCreateSheet = false
                scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
                viewModel.load()
            },
            onDismiss = { showCreateSheet = false }
        )
    }

    selectedChallenge?.let { challenge ->
        ChallengeDetailsSheet(
            challenge = challenge,
            viewModel = challengeDetailsViewModel,
            isActionLoading = false,
            onSaved = { message ->
                selectedChallenge = null
                scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
                viewModel.load()
            },
            onComplete = {
                selectedChallenge = null
                viewModel.completeChallenge(challenge.id)
            },
            onMiss = {
                selectedChallenge = null
                viewModel.missChallenge(challenge.id)
            },
            onDismiss = { selectedChallenge = null }
        )
    }
}

@Composable
private fun PlannedDateGroup(
    date: String,
    challenges: List<MobileChallenge>,
    onChallengeClick: (MobileChallenge) -> Unit,
    onComplete: (Int) -> Unit,
    onMiss: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(date, style = MaterialTheme.typography.titleMedium)
        challenges.forEach { challenge ->
            ToDoTaskCard(
                challenge = challenge,
                onClick = { onChallengeClick(challenge) },
                onCompleteClick = { onComplete(challenge.id) },
                onMissClick = { onMiss(challenge.id) }
            )
        }
    }
}
