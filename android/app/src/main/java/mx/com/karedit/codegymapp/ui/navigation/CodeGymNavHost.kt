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
import mx.com.karedit.codegymapp.ui.screens.login.LoginScreen
import mx.com.karedit.codegymapp.ui.screens.login.LoginViewModel
import mx.com.karedit.codegymapp.ui.screens.today.TodayScreen
import mx.com.karedit.codegymapp.ui.screens.today.TodayViewModel

@Composable
fun CodeGymNavHost(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = if (appContainer.authRepository.hasToken()) AppRoutes.Today else AppRoutes.Login

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
                    navController.navigate(AppRoutes.Today) {
                        popUpTo(AppRoutes.Login) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRoutes.Today) {
            val viewModel = remember { TodayViewModel(appContainer.authRepository) }
            TodayScreen(viewModel = viewModel)
        }
    }
}
