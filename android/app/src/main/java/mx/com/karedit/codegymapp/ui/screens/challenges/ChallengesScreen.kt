package mx.com.karedit.codegymapp.ui.screens.challenges

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
fun ChallengesScreen(
    viewModel: ChallengesViewModel,
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
        snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        viewModel.snackbarShown()
    }

    CodeGymSectionScaffold(
        onBackHome = { onNavigate(AppRoutes.Home) },
        snackbarHostState = snackbarHostState,
        collapsedTitle = "Retos",
        collapsedSubtitle = state.monthLabel,
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
            Text("Retos", style = MaterialTheme.typography.displayMedium)
            MonthSelector(
                monthLabel = state.monthLabel,
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth,
                onCurrent = viewModel::currentMonth
            )
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
                        ToDoTaskCard(
                            challenge = challenge,
                            onClick = { selectedChallenge = challenge },
                            onCompleteClick = { viewModel.completeChallenge(challenge.id) },
                            onMissClick = { viewModel.missChallenge(challenge.id) }
                        )
                    }
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
private fun MonthSelector(
    monthLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCurrent: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onPrevious, modifier = Modifier.width(52.dp)) {
            Text("‹", style = MaterialTheme.typography.headlineMedium)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.headlineSmall
            )
            TextButton(onClick = onCurrent) {
                Text("Mes actual")
            }
        }
        TextButton(onClick = onNext, modifier = Modifier.width(52.dp)) {
            Text("›", style = MaterialTheme.typography.headlineMedium)
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
