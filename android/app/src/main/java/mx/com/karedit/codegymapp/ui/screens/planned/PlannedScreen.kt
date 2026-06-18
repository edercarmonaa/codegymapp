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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import mx.com.karedit.codegymapp.ui.components.ToDoTaskCard
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymSectionScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannedScreen(
    viewModel: PlannedViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short
        )
        viewModel.snackbarShown()
    }

    CodeGymSectionScaffold(
        title = "Planeado",
        onBackHome = { onNavigate(AppRoutes.Home) },
        snackbarHostState = snackbarHostState
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Próximos retos", style = MaterialTheme.typography.headlineSmall)

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
                            onComplete = viewModel::completeChallenge
                        )
                    }
            }
        }
    }
}

@Composable
private fun PlannedDateGroup(
    date: String,
    challenges: List<MobileChallenge>,
    onComplete: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(date, style = MaterialTheme.typography.titleMedium)
        challenges.forEach { challenge ->
            ToDoTaskCard(
                challenge = challenge,
                onCompleteClick = { onComplete(challenge.id) }
            )
        }
    }
}
