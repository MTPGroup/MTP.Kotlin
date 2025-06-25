package tech.hanasaki.momotalk_plus.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import tech.hanasaki.momotalk_plus.features.login.presentation.ui.LoginScreen
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.LoginViewModel

object AppDestinations {
    const val LOGIN = "login"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val CHAT = "chat"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppDestinations.LOGIN) {
        composable(AppDestinations.LOGIN) {
            val loginViewModel = koinInject<LoginViewModel>()
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestinations.HOME) {
                        popUpTo(AppDestinations.HOME) { inclusive = true }
                    }
                },
                loginViewModel = loginViewModel
            )
        }
    }
}