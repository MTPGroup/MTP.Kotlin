package tech.hanasaki.momotalk_plus.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.viewmodel.AppViewModel
import tech.hanasaki.momotalk_plus.features.home.presentation.ui.HomeScreen
import tech.hanasaki.momotalk_plus.features.login.presentation.ui.ForgotPasswordScreen
import tech.hanasaki.momotalk_plus.features.login.presentation.ui.LoginScreen
import tech.hanasaki.momotalk_plus.features.login.presentation.ui.RegisterScreen

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
fun AppNavigation(
    appViewModel: AppViewModel = koinViewModel(),
) {
    val navController = rememberNavController()
    val appState by appViewModel.uiState.collectAsState()
    NavHost(
        navController = navController,
        startDestination = if (appState.isLoggedIn) NavigationRoute.Home else NavigationRoute.Login,
    ) {
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

        composable<NavigationRoute.Home> {
            HomeScreen(
                onNavigateToChat = { chatId ->
                    navController.navigate(NavigationRoute.Chat(chatId))
                },
                onNavigateToProfile = {
                    TODO("Navigate to profile not implemented yet")
                }
            )
        }

        composable<NavigationRoute.Chat> { backStackEntry ->
            val chat = backStackEntry.toRoute<NavigationRoute.Chat>()
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("聊天室: ${chat.sessionId}")
            }
        }

        composable<NavigationRoute.Settings> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("设置页面")
            }
        }
    }
}