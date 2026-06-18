package mx.com.karedit.codegymapp.ui.screens.challenges

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import mx.com.karedit.codegymapp.ui.components.ToDoTaskCard
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymSectionScaffold
import mx.com.karedit.codegymapp.ui.screens.create.CreateChallengeSheet
import mx.com.karedit.codegymapp.ui.screens.create.CreateChallengeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    viewModel: ChallengesViewModel,
    createChallengeViewModel: CreateChallengeViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        viewModel.snackbarShown()
    }

    CodeGymSectionScaffold(
        onBackHome = { onNavigate(AppRoutes.Home) },
        snackbarHostState = snackbarHostState,
        onFabClick = { showCreateSheet = true }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Retos", style = MaterialTheme.typography.displayMedium)
            Text("Mes ${state.month}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            onCompleteClick = { viewModel.completeChallenge(challenge.id) }
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
