package tech.hanasaki.momotalk_plus.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.features.login.presentation.ui.ForgotPasswordScreen
import tech.hanasaki.momotalk_plus.features.login.presentation.ui.LoginScreen
import tech.hanasaki.momotalk_plus.features.login.presentation.ui.RegisterScreen
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.LoginViewModel

@Serializable
sealed interface NavigationRoute {
    @Serializable
    data object Login : NavigationRoute

    @Serializable
    data object Register : NavigationRoute

    @Serializable
    data object ForgotPassword : NavigationRoute

    @Serializable
    data object Home : NavigationRoute

    @Serializable
    data object Settings : NavigationRoute

    @Serializable
    data object Profile : NavigationRoute

    @Serializable
    data class Chat(val sessionId: String) : NavigationRoute
}


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NavigationRoute.Login) {
        composable<NavigationRoute.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavigationRoute.Home) {
                        popUpTo(NavigationRoute.Login) { inclusive = true }
                    }
                },
                onForgotPassword = {
                    navController.navigate(NavigationRoute.ForgotPassword)
                },
                onRegister = {
                    navController.navigate(NavigationRoute.Register)
                },
            )
        }

        composable<NavigationRoute.ForgotPassword> {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<NavigationRoute.Register> {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<NavigationRoute.Home> { }
    }
}