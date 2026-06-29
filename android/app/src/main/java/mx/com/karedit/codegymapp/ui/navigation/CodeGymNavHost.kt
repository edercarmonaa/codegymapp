package mx.com.karedit.codegymapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.com.karedit.codegymapp.core.session.SessionEvent
import mx.com.karedit.codegymapp.di.AppContainer
import mx.com.karedit.codegymapp.ui.screens.account.AccountScreen
import mx.com.karedit.codegymapp.ui.screens.account.AccountViewModel
import mx.com.karedit.codegymapp.ui.screens.auth.AuthViewModel
import mx.com.karedit.codegymapp.ui.screens.biometric.BiometricUnlockScreen
import mx.com.karedit.codegymapp.ui.screens.challenges.ChallengesScreen
import mx.com.karedit.codegymapp.ui.screens.challenges.ChallengeStatusFilter
import mx.com.karedit.codegymapp.ui.screens.challenges.ChallengesViewModel
import mx.com.karedit.codegymapp.ui.screens.create.CreateChallengeViewModel
import mx.com.karedit.codegymapp.ui.screens.create.CreateGoalViewModel
import mx.com.karedit.codegymapp.ui.screens.create.CreateRoutineViewModel
import mx.com.karedit.codegymapp.ui.screens.create.RegisterCompletedChallengeViewModel
import mx.com.karedit.codegymapp.ui.screens.details.ChallengeDetailsViewModel
import mx.com.karedit.codegymapp.ui.screens.goals.GoalsScreen
import mx.com.karedit.codegymapp.ui.screens.goals.GoalsViewModel
import mx.com.karedit.codegymapp.ui.screens.home.HomeScreen
import mx.com.karedit.codegymapp.ui.screens.home.HomeViewModel
import mx.com.karedit.codegymapp.ui.screens.login.LoginScreen
import mx.com.karedit.codegymapp.ui.screens.login.LoginViewModel
import mx.com.karedit.codegymapp.ui.screens.notifications.NotificationsScreen
import mx.com.karedit.codegymapp.ui.screens.notifications.NotificationsViewModel
import mx.com.karedit.codegymapp.ui.screens.planned.PlannedScreen
import mx.com.karedit.codegymapp.ui.screens.planned.PlannedViewModel
import mx.com.karedit.codegymapp.ui.screens.settings.SettingsScreen
import mx.com.karedit.codegymapp.ui.screens.settings.SettingsViewModel
import mx.com.karedit.codegymapp.ui.screens.summary.SummaryScreen
import mx.com.karedit.codegymapp.ui.screens.summary.SummaryViewModel
import mx.com.karedit.codegymapp.ui.screens.today.TodayScreen
import mx.com.karedit.codegymapp.ui.screens.today.TodayViewModel

@Composable
fun CodeGymNavHost(
    appContainer: AppContainer,
    authViewModel: AuthViewModel,
    pendingNotificationRoute: String? = null,
    onPendingNotificationRouteHandled: () -> Unit = {},
    onAuthenticated: () -> Unit = {},
    navController: NavHostController = rememberNavController()
) {
    val authState by authViewModel.state.collectAsState()
    val startDestination = when {
        !appContainer.authRepository.hasToken() -> AppRoutes.Login
        authState.biometricRequest != null -> AppRoutes.BiometricUnlock
        else -> AppRoutes.Home
    }
    val navigateTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(AppRoutes.Home) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(appContainer.sessionManager) {
        appContainer.sessionManager.sessionEvents.collect { event ->
            when (event) {
                is SessionEvent.SessionExpired -> {
                    authViewModel.onSessionExpired(event.reason)
                    navController.navigate(AppRoutes.Login) {
                        popUpTo(0)
                    }
                }
                SessionEvent.SessionLocked -> {
                    authViewModel.onSessionLocked()
                    navController.navigate(AppRoutes.BiometricUnlock) {
                        popUpTo(0)
                    }
                }
            }
        }
    }

    LaunchedEffect(authState.biometricRequest?.id) {
        if (authState.biometricRequest != null && appContainer.authRepository.hasToken()) {
            navController.navigate(AppRoutes.BiometricUnlock) {
                popUpTo(AppRoutes.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    LaunchedEffect(pendingNotificationRoute, authState.isAuthenticated) {
        val route = pendingNotificationRoute ?: return@LaunchedEffect
        if (!appContainer.authRepository.hasToken() || !authState.isAuthenticated) {
            return@LaunchedEffect
        }

        navController.navigate(route) {
            popUpTo(AppRoutes.Home) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        onPendingNotificationRouteHandled()
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppRoutes.BiometricUnlock) {
            BiometricUnlockScreen(
                onUnlocked = {
                    authViewModel.onBiometricSucceeded()
                    onAuthenticated()
                    navController.navigate(AppRoutes.Home) {
                        popUpTo(AppRoutes.BiometricUnlock) { inclusive = true }
                    }
                },
                onBiometricFatalError = { message ->
                    authViewModel.onBiometricCancelledOrFailed(message)
                    navController.navigate(AppRoutes.Login) {
                        popUpTo(0)
                    }
                },
                onDisableBiometricAndLogout = {
                    authViewModel.disableBiometricAndLogout()
                    navController.navigate(AppRoutes.Login) {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(AppRoutes.Login) {
            val viewModel = remember { LoginViewModel(appContainer.authRepository) }
            LoginScreen(
                viewModel = viewModel,
                sessionMessage = authState.loginMessage,
                onLoginSuccess = {
                    authViewModel.onManualLoginSuccess()
                    onAuthenticated()
                    appContainer.fcmTokenRegistrar.registerCurrentToken()
                    navController.navigate(AppRoutes.Home) {
                        popUpTo(AppRoutes.Login) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRoutes.Home) {
            val viewModel = remember {
                HomeViewModel(
                    authRepository = appContainer.authRepository,
                    todayRepository = appContainer.todayRepository,
                    plannedRepository = appContainer.plannedRepository,
                    challengesRepository = appContainer.challengesRepository
                )
            }
            val createViewModel = remember { CreateChallengeViewModel(appContainer.createChallengeRepository) }
            val createRoutineViewModel = remember { CreateRoutineViewModel(appContainer.createRoutineRepository) }
            HomeScreen(
                viewModel = viewModel,
                createChallengeViewModel = createViewModel,
                createRoutineViewModel = createRoutineViewModel,
                onNavigate = navigateTab
            )
        }
        composable(AppRoutes.Account) {
            val viewModel = remember {
                AccountViewModel(
                    authRepository = appContainer.authRepository,
                    settingsRepository = appContainer.settingsRepository
                )
            }
            AccountScreen(
                viewModel = viewModel,
                onNavigate = navigateTab
            )
        }
        composable(AppRoutes.Settings) {
            val viewModel = remember {
                SettingsViewModel(
                    settingsRepository = appContainer.settingsRepository,
                    authRepository = appContainer.authRepository
                )
            }
            SettingsScreen(
                viewModel = viewModel,
                onNavigate = navigateTab
            )
        }
        composable(AppRoutes.Summary) {
            val viewModel = remember { SummaryViewModel(appContainer.summaryRepository) }
            SummaryScreen(
                viewModel = viewModel,
                onNavigate = navigateTab
            )
        }
        composable(AppRoutes.Goals) {
            val viewModel = remember { GoalsViewModel(appContainer.goalsRepository) }
            val createGoalViewModel = remember { CreateGoalViewModel(appContainer.goalsRepository) }
            GoalsScreen(
                viewModel = viewModel,
                createGoalViewModel = createGoalViewModel,
                onNavigate = navigateTab
            )
        }
        composable(AppRoutes.Notifications) {
            val viewModel = remember { NotificationsViewModel(appContainer.notificationsRepository) }
            NotificationsScreen(
                viewModel = viewModel,
                onNavigate = navigateTab
            )
        }
        composable(AppRoutes.Today) {
            val viewModel = remember {
                TodayViewModel(
                    authRepository = appContainer.authRepository,
                    todayRepository = appContainer.todayRepository
                )
            }
            val createViewModel = remember { CreateChallengeViewModel(appContainer.createChallengeRepository) }
            val createRoutineViewModel = remember { CreateRoutineViewModel(appContainer.createRoutineRepository) }
            val createGoalViewModel = remember { CreateGoalViewModel(appContainer.goalsRepository) }
            val registerCompletedViewModel = remember { RegisterCompletedChallengeViewModel(appContainer.createChallengeRepository) }
            val detailsViewModel = remember { ChallengeDetailsViewModel(appContainer.challengeDetailsRepository) }
            TodayScreen(
                viewModel = viewModel,
                createChallengeViewModel = createViewModel,
                createRoutineViewModel = createRoutineViewModel,
                createGoalViewModel = createGoalViewModel,
                registerCompletedChallengeViewModel = registerCompletedViewModel,
                challengeDetailsViewModel = detailsViewModel,
                onNavigate = navigateTab
            )
        }
        composable(AppRoutes.Planned) {
            val viewModel = remember { PlannedViewModel(appContainer.plannedRepository) }
            val createViewModel = remember { CreateChallengeViewModel(appContainer.createChallengeRepository) }
            val createRoutineViewModel = remember { CreateRoutineViewModel(appContainer.createRoutineRepository) }
            val createGoalViewModel = remember { CreateGoalViewModel(appContainer.goalsRepository) }
            val registerCompletedViewModel = remember { RegisterCompletedChallengeViewModel(appContainer.createChallengeRepository) }
            val detailsViewModel = remember { ChallengeDetailsViewModel(appContainer.challengeDetailsRepository) }
            PlannedScreen(
                viewModel = viewModel,
                createChallengeViewModel = createViewModel,
                createRoutineViewModel = createRoutineViewModel,
                createGoalViewModel = createGoalViewModel,
                registerCompletedChallengeViewModel = registerCompletedViewModel,
                challengeDetailsViewModel = detailsViewModel,
                onNavigate = navigateTab
            )
        }
        composable(AppRoutes.Challenges) {
            val viewModel = remember { ChallengesViewModel(appContainer.challengesRepository) }
            val createViewModel = remember { CreateChallengeViewModel(appContainer.createChallengeRepository) }
            val createRoutineViewModel = remember { CreateRoutineViewModel(appContainer.createRoutineRepository) }
            val createGoalViewModel = remember { CreateGoalViewModel(appContainer.goalsRepository) }
            val registerCompletedViewModel = remember { RegisterCompletedChallengeViewModel(appContainer.createChallengeRepository) }
            val detailsViewModel = remember { ChallengeDetailsViewModel(appContainer.challengeDetailsRepository) }
            ChallengesScreen(
                viewModel = viewModel,
                createChallengeViewModel = createViewModel,
                createRoutineViewModel = createRoutineViewModel,
                createGoalViewModel = createGoalViewModel,
                registerCompletedChallengeViewModel = registerCompletedViewModel,
                challengeDetailsViewModel = detailsViewModel,
                onNavigate = navigateTab
            )
        }
        composable(AppRoutes.ChallengesExpired) {
            val viewModel = remember {
                ChallengesViewModel(
                    challengesRepository = appContainer.challengesRepository,
                    initialStatus = ChallengeStatusFilter.Expired
                )
            }
            val createViewModel = remember { CreateChallengeViewModel(appContainer.createChallengeRepository) }
            val createRoutineViewModel = remember { CreateRoutineViewModel(appContainer.createRoutineRepository) }
            val createGoalViewModel = remember { CreateGoalViewModel(appContainer.goalsRepository) }
            val registerCompletedViewModel = remember { RegisterCompletedChallengeViewModel(appContainer.createChallengeRepository) }
            val detailsViewModel = remember { ChallengeDetailsViewModel(appContainer.challengeDetailsRepository) }
            ChallengesScreen(
                viewModel = viewModel,
                createChallengeViewModel = createViewModel,
                createRoutineViewModel = createRoutineViewModel,
                createGoalViewModel = createGoalViewModel,
                registerCompletedChallengeViewModel = registerCompletedViewModel,
                challengeDetailsViewModel = detailsViewModel,
                onNavigate = navigateTab
            )
        }
    }
}
