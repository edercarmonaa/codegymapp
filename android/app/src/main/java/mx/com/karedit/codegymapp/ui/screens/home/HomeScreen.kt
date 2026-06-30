package mx.com.karedit.codegymapp.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.screens.create.CreateChallengeSheet
import mx.com.karedit.codegymapp.ui.screens.create.CreateChallengeViewModel
import mx.com.karedit.codegymapp.ui.screens.create.CreateGoalSheet
import mx.com.karedit.codegymapp.ui.screens.create.CreateGoalViewModel
import mx.com.karedit.codegymapp.ui.screens.create.CreateRoutineSheet
import mx.com.karedit.codegymapp.ui.screens.create.CreateRoutineViewModel
import mx.com.karedit.codegymapp.ui.screens.create.QuickCreateActionSheet
import mx.com.karedit.codegymapp.ui.screens.create.RegisterCompletedChallengeSheet
import mx.com.karedit.codegymapp.ui.screens.create.RegisterCompletedChallengeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    createChallengeViewModel: CreateChallengeViewModel,
    createRoutineViewModel: CreateRoutineViewModel,
    createGoalViewModel: CreateGoalViewModel,
    registerCompletedChallengeViewModel: RegisterCompletedChallengeViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val user = state.user
    var showCreateSheet by remember { mutableStateOf(false) }
    var showRoutineSheet by remember { mutableStateOf(false) }
    var showGoalSheet by remember { mutableStateOf(false) }
    var showRegisterCompletedSheet by remember { mutableStateOf(false) }
    var showQuickActions by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(AppRoutes.Account) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(initial = user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "C")
                Spacer(modifier = Modifier.width(18.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.name?.ifBlank { user.username } ?: "CodeGymApp",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = user?.email ?: "Challenge tracking",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "⌕",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeItem(
                    symbol = "☼",
                    label = "Mi día",
                    count = state.todayCount,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { onNavigate(AppRoutes.Today) }
                )
                HomeItem(
                    symbol = "▣",
                    label = "Planeado",
                    count = state.plannedCount,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { onNavigate(AppRoutes.Planned) }
                )
                HomeItem(
                    symbol = "⌂",
                    label = "Retos",
                    count = state.challengesCount,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { onNavigate(AppRoutes.Challenges) }
                )
                HomeItem(
                    symbol = "▤",
                    label = "Resumen",
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = { onNavigate(AppRoutes.Summary) }
                )
                HomeItem(
                    symbol = "◎",
                    label = "Metas",
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { onNavigate(AppRoutes.Goals) }
                )
                HomeItem(
                    symbol = "!",
                    label = "Notificaciones",
                    color = MaterialTheme.colorScheme.error,
                    onClick = { onNavigate(AppRoutes.Notifications) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            HomeBottomActions(
                onCreateAction = { showQuickActions = true }
            )
        }
    }

    if (showQuickActions) {
        QuickCreateActionSheet(
            onScheduleChallenge = {
                showQuickActions = false
                showCreateSheet = true
            },
            onRegisterCompletedChallenge = {
                showQuickActions = false
                showRegisterCompletedSheet = true
            },
            onCreateRoutine = {
                showQuickActions = false
                showRoutineSheet = true
            },
            onCreateGoal = {
                showQuickActions = false
                createGoalViewModel.startCreate()
                showGoalSheet = true
            },
            onDismiss = { showQuickActions = false }
        )
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

    if (showRoutineSheet) {
        CreateRoutineSheet(
            viewModel = createRoutineViewModel,
            onCreated = { message ->
                showRoutineSheet = false
                scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
                viewModel.load()
            },
            onDismiss = { showRoutineSheet = false }
        )
    }

    if (showRegisterCompletedSheet) {
        RegisterCompletedChallengeSheet(
            viewModel = registerCompletedChallengeViewModel,
            onCreated = { message ->
                showRegisterCompletedSheet = false
                scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
                viewModel.load()
            },
            onDismiss = { showRegisterCompletedSheet = false }
        )
    }

    if (showGoalSheet) {
        CreateGoalSheet(
            viewModel = createGoalViewModel,
            onCreated = { message ->
                showGoalSheet = false
                scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
            },
            onDismiss = { showGoalSheet = false }
        )
    }
}

@Composable
private fun HomeBottomActions(
    onCreateAction: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        FloatingActionButton(onClick = onCreateAction) {
            Text("+", style = MaterialTheme.typography.displaySmall)
        }
    }
}

@Composable
private fun Avatar(initial: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(58.dp)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(color = Color(0xFFEDEDED))
        }
        Text(
            text = initial,
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black
        )
    }
}

@Composable
private fun HomeItem(
    symbol: String,
    label: String,
    color: Color,
    count: Int? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            modifier = Modifier.width(56.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f)
        )
        count?.let {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
