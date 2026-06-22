package mx.com.karedit.codegymapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.com.karedit.codegymapp.core.session.SessionEvent
import mx.com.karedit.codegymapp.di.AppContainer
import mx.com.karedit.codegymapp.ui.screens.account.AccountScreen
import mx.com.karedit.codegymapp.ui.screens.account.AccountViewModel
import mx.com.karedit.codegymapp.ui.screens.challenges.ChallengesScreen
import mx.com.karedit.codegymapp.ui.screens.challenges.ChallengesViewModel
import mx.com.karedit.codegymapp.ui.screens.create.CreateChallengeViewModel
import mx.com.karedit.codegymapp.ui.screens.create.CreateRoutineViewModel
import mx.com.karedit.codegymapp.ui.screens.details.ChallengeDetailsViewModel
import mx.com.karedit.codegymapp.ui.screens.home.HomeScreen
import mx.com.karedit.codegymapp.ui.screens.home.HomeViewModel
import mx.com.karedit.codegymapp.ui.screens.login.LoginScreen
import mx.com.karedit.codegymapp.ui.screens.login.LoginViewModel
import mx.com.karedit.codegymapp.ui.screens.notifications.NotificationsScreen
import mx.com.karedit.codegymapp.ui.screens.notifications.NotificationsViewModel
import mx.com.karedit.codegymapp.ui.screens.planned.PlannedScreen
import mx.com.karedit.codegymapp.ui.screens.planned.PlannedViewModel
import mx.com.karedit.codegymapp.ui.screens.summary.SummaryScreen
import mx.com.karedit.codegymapp.ui.screens.summary.SummaryViewModel
import mx.com.karedit.codegymapp.ui.screens.today.TodayScreen
import mx.com.karedit.codegymapp.ui.screens.today.TodayViewModel

@Composable
fun CodeGymNavHost(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = if (appContainer.authRepository.hasToken()) AppRoutes.Home else AppRoutes.Login
    val navigateTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(AppRoutes.Home) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(appContainer.sessionManager) {
        appContainer.sessionManager.sessionEvents.collect { event ->
            if (event is SessionEvent.SessionExpired) {
                navController.navigate(AppRoutes.Login) {
                    popUpTo(0)
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppRoutes.Login) {
            val viewModel = remember { LoginViewModel(appContainer.authRepository) }
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
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
            val viewModel = remember { AccountViewModel(appContainer.authRepository) }
            AccountScreen(
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
            val detailsViewModel = remember { ChallengeDetailsViewModel(appContainer.challengeDetailsRepository) }
            TodayScreen(
                viewModel = viewModel,
                createChallengeViewModel = createViewModel,
                challengeDetailsViewModel = detailsViewModel,
                onNavigate = navigateTab
            )
        }
        composable(AppRoutes.Planned) {
            val viewModel = remember { PlannedViewModel(appContainer.plannedRepository) }
            val createViewModel = remember { CreateChallengeViewModel(appContainer.createChallengeRepository) }
            val detailsViewModel = remember { ChallengeDetailsViewModel(appContainer.challengeDetailsRepository) }
            PlannedScreen(
                viewModel = viewModel,
                createChallengeViewModel = createViewModel,
                challengeDetailsViewModel = detailsViewModel,
                onNavigate = navigateTab
            )
        }
        composable(AppRoutes.Challenges) {
            val viewModel = remember { ChallengesViewModel(appContainer.challengesRepository) }
            val createViewModel = remember { CreateChallengeViewModel(appContainer.createChallengeRepository) }
            val detailsViewModel = remember { ChallengeDetailsViewModel(appContainer.challengeDetailsRepository) }
            ChallengesScreen(
                viewModel = viewModel,
                createChallengeViewModel = createViewModel,
                challengeDetailsViewModel = detailsViewModel,
                onNavigate = navigateTab
            )
        }
    }
}
