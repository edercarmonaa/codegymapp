package mx.com.karedit.codegymapp.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import mx.com.karedit.codegymapp.domain.model.MobileChallenge
import mx.com.karedit.codegymapp.domain.model.hasRequiredCompletionData
import mx.com.karedit.codegymapp.ui.components.ToDoTaskCard
import mx.com.karedit.codegymapp.ui.feedback.rememberCodeGymHapticSnackbar
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymSectionScaffold
import mx.com.karedit.codegymapp.ui.screens.create.CreateChallengeSheet
import mx.com.karedit.codegymapp.ui.screens.create.CreateChallengeViewModel
import mx.com.karedit.codegymapp.ui.screens.create.CreateGoalSheet
import mx.com.karedit.codegymapp.ui.screens.create.CreateGoalViewModel
import mx.com.karedit.codegymapp.ui.screens.create.CreateRoutineSheet
import mx.com.karedit.codegymapp.ui.screens.create.CreateRoutineViewModel
import mx.com.karedit.codegymapp.ui.screens.create.QuickCreateActionSheet
import mx.com.karedit.codegymapp.ui.screens.create.RegisterCompletedChallengeSheet
import mx.com.karedit.codegymapp.ui.screens.create.RegisterCompletedChallengeViewModel
import mx.com.karedit.codegymapp.ui.screens.details.ChallengeQuickActionsSheet
import mx.com.karedit.codegymapp.ui.screens.details.ChallengeDetailsSheet
import mx.com.karedit.codegymapp.ui.screens.details.ChallengeDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    createChallengeViewModel: CreateChallengeViewModel,
    createRoutineViewModel: CreateRoutineViewModel,
    createGoalViewModel: CreateGoalViewModel,
    registerCompletedChallengeViewModel: RegisterCompletedChallengeViewModel,
    challengeDetailsViewModel: ChallengeDetailsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var todayExpanded by remember { mutableStateOf(true) }
    var expiredExpanded by remember { mutableStateOf(false) }
    var selectedChallenge by remember { mutableStateOf<MobileChallenge?>(null) }
    var quickActionChallenge by remember { mutableStateOf<MobileChallenge?>(null) }
    var showQuickActions by remember { mutableStateOf(false) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showRoutineSheet by remember { mutableStateOf(false) }
    var showGoalSheet by remember { mutableStateOf(false) }
    var showRegisterCompletedSheet by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val todayLabel = remember { todayDisplayLabel() }
    val snackbarHostState = remember { SnackbarHostState() }
    val showHapticSnackbar = rememberCodeGymHapticSnackbar(snackbarHostState)

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        showHapticSnackbar(message)
        viewModel.snackbarShown()
    }

    CodeGymSectionScaffold(
        onBackHome = { onNavigate(AppRoutes.Home) },
        snackbarHostState = snackbarHostState,
        collapsedTitle = "Mi día",
        collapsedSubtitle = todayLabel,
        isCollapsed = scrollState.value > 96,
        onFabClick = { showQuickActions = true }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Mi día",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = todayLabel,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    onChallengeClick = { selectedChallenge = it },
                    onCompleteClick = { challenge ->
                        if (challenge.hasRequiredCompletionData()) {
                            viewModel.completeChallenge(challenge.id)
                        } else {
                            selectedChallenge = challenge
                        }
                    },
                    onActionsClick = { quickActionChallenge = it }
                )
                ChallengeSection(
                    title = "Vencidos pendientes",
                    count = state.expiredChallenges.size,
                    expanded = expiredExpanded,
                    emptyText = "Sin retos vencidos por revisar.",
                    challenges = state.expiredChallenges,
                    onToggle = { expiredExpanded = !expiredExpanded },
                    onChallengeClick = { selectedChallenge = it },
                    onCompleteClick = { challenge ->
                        if (challenge.hasRequiredCompletionData()) {
                            viewModel.completeChallenge(challenge.id)
                        } else {
                            selectedChallenge = challenge
                        }
                    },
                    onActionsClick = { quickActionChallenge = it }
                )
            }
        }
    }

    selectedChallenge?.let { challenge ->
        ChallengeDetailsSheet(
            challenge = challenge,
            viewModel = challengeDetailsViewModel,
            isActionLoading = state.isActionLoading,
            onSaved = { message ->
                selectedChallenge = null
                showHapticSnackbar(message)
                viewModel.load()
            },
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

    quickActionChallenge?.let { challenge ->
        ChallengeQuickActionsSheet(
            challenge = challenge,
            onReschedule = { scheduledDate ->
                quickActionChallenge = null
                viewModel.rescheduleChallenge(challenge.id, scheduledDate)
            },
            onMiss = {
                quickActionChallenge = null
                viewModel.missChallenge(challenge.id)
            },
            onCancel = {
                quickActionChallenge = null
                viewModel.cancelChallenge(challenge.id)
            },
            onDismiss = { quickActionChallenge = null }
        )
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
                showHapticSnackbar(message)
                viewModel.load()
                selectedChallenge = null
            },
            onDismiss = { showCreateSheet = false }
        )
    }

    if (showRoutineSheet) {
        CreateRoutineSheet(
            viewModel = createRoutineViewModel,
            onCreated = { message ->
                showRoutineSheet = false
                showHapticSnackbar(message)
                viewModel.load()
                selectedChallenge = null
            },
            onDismiss = { showRoutineSheet = false }
        )
    }

    if (showRegisterCompletedSheet) {
        RegisterCompletedChallengeSheet(
            viewModel = registerCompletedChallengeViewModel,
            onCreated = { message ->
                showRegisterCompletedSheet = false
                showHapticSnackbar(message)
                viewModel.load()
                selectedChallenge = null
            },
            onDismiss = { showRegisterCompletedSheet = false }
        )
    }

    if (showGoalSheet) {
        CreateGoalSheet(
            viewModel = createGoalViewModel,
            onCreated = { message ->
                showGoalSheet = false
                showHapticSnackbar(message)
            },
            onDismiss = { showGoalSheet = false }
        )
    }
}

@Composable
private fun TodayLoading() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
    onChallengeClick: (MobileChallenge) -> Unit,
    onCompleteClick: (MobileChallenge) -> Unit,
    onActionsClick: (MobileChallenge) -> Unit
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
                                onClick = { onChallengeClick(challenge) },
                                onCompleteClick = { onCompleteClick(challenge) },
                                onActionsClick = { onActionsClick(challenge) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun todayDisplayLabel(): String {
    val today = LocalDate.now()
    val days = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
    val months = listOf(
        "enero",
        "febrero",
        "marzo",
        "abril",
        "mayo",
        "junio",
        "julio",
        "agosto",
        "septiembre",
        "octubre",
        "noviembre",
        "diciembre"
    )

    return "${days[today.dayOfWeek.value - 1]}, ${today.dayOfMonth} de ${months[today.monthValue - 1]}"
}
