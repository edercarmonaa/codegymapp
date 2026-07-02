package mx.com.karedit.codegymapp.ui.screens.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.domain.model.MobileGoal
import mx.com.karedit.codegymapp.ui.components.ListSkeleton
import mx.com.karedit.codegymapp.ui.feedback.rememberCodeGymHapticSnackbar
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymSectionScaffold
import mx.com.karedit.codegymapp.ui.screens.create.CreateGoalSheet
import mx.com.karedit.codegymapp.ui.screens.create.CreateGoalViewModel

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel,
    createGoalViewModel: CreateGoalViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val showHapticSnackbar = rememberCodeGymHapticSnackbar(snackbarHostState)
    val scrollState = rememberScrollState()
    var showCreateGoalSheet by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf<MobileGoal?>(null) }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        showHapticSnackbar(message)
        viewModel.snackbarShown()
    }

    CodeGymSectionScaffold(
        onBackHome = { onNavigate(AppRoutes.Home) },
        snackbarHostState = snackbarHostState,
        collapsedTitle = "Metas",
        collapsedSubtitle = "${state.goals.size} activas",
        isCollapsed = scrollState.value > 96,
        onFabClick = {
            createGoalViewModel.startCreate()
            selectedGoal = null
            showCreateGoalSheet = true
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Metas", style = MaterialTheme.typography.displayMedium)
            Text(
                text = "${state.goals.size} activas",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                state.isLoading -> ListSkeleton(count = 3)
                state.goals.isEmpty() -> Text("No hay metas activas.", style = MaterialTheme.typography.bodyLarge)
                else -> state.goals.forEach { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = {
                            createGoalViewModel.startEdit(goal)
                            selectedGoal = goal
                            showCreateGoalSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showCreateGoalSheet) {
        CreateGoalSheet(
            viewModel = createGoalViewModel,
            onCreated = { message ->
                showCreateGoalSheet = false
                showHapticSnackbar(message)
                viewModel.load()
            },
            onDismiss = { showCreateGoalSheet = false }
        )
    }
}

@Composable
private fun GoalCard(goal: MobileGoal, onClick: () -> Unit) {
    val progress = (goal.progressPercent / 100.0).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.goalTypeLabel, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "${goal.periodTypeLabel} · vence ${goal.periodEnd}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${goal.progressPercent.asPercent()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${goal.currentValue} / ${goal.targetValue} ${goal.unit}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = goal.scopeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (goal.autoRenew) {
                Text(
                    text = "Renovación automática",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun Double.asPercent(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format("%.1f", this)
    }
