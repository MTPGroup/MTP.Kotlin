package tech.hanasaki.momotalk_plus.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.viewmodel.AppViewModel
import tech.hanasaki.momotalk_plus.features.about.presentation.ui.AboutScreen
import tech.hanasaki.momotalk_plus.features.auth.presentation.ui.ForgotPasswordScreen
import tech.hanasaki.momotalk_plus.features.auth.presentation.ui.LoginScreen
import tech.hanasaki.momotalk_plus.features.auth.presentation.ui.RegisterScreen
import tech.hanasaki.momotalk_plus.features.home.presentation.ui.HomeScreen
import tech.hanasaki.momotalk_plus.features.profile.presentation.ui.ProfileScreen
import tech.hanasaki.momotalk_plus.features.settings.presentation.ui.PrivacyPolicyScreen
import tech.hanasaki.momotalk_plus.features.settings.presentation.ui.SettingsScreen
import tech.hanasaki.momotalk_plus.features.settings.presentation.ui.TermsOfServiceScreen
import tech.hanasaki.momotalk_plus.features.splash.presentation.ui.SplashScreen

@Serializable
sealed interface NavigationRoute {
    @Serializable
    data object Splash : NavigationRoute

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
    data object About : NavigationRoute

    @Serializable
    data object PrivacyPolicy : NavigationRoute

    @Serializable
    data object TermsOfService : NavigationRoute

    @Serializable
    data object Profile : NavigationRoute
}

@Serializable
object AuthGraph


@Composable
fun AppNavigation(
    appViewModel: AppViewModel = koinViewModel(),
) {
    val navController = rememberNavController()
    val appState by appViewModel.uiState.collectAsState()

    // 监听初始化状态，完成后导航到合适的页面
    LaunchedEffect(appState.isLoading, appState.isLoggedIn) {
        if (appState.isLoading) return@LaunchedEffect

        if (appState.isLoggedIn) {
            navController.navigate(NavigationRoute.Home) {
                popUpTo(NavigationRoute.Splash) { inclusive = true }
            }
        } else {
            navController.navigate(NavigationRoute.Login) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Splash,
    ) {
        composable<NavigationRoute.Splash> {
            SplashScreen()
        }

        navigation<AuthGraph>(
            startDestination = NavigationRoute.Login,
        ) {
            composable<NavigationRoute.Login> {
                LoginScreen(
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
        }


        composable<NavigationRoute.Home> {
            HomeScreen(
                currentUser = appState.currentUser,
                onNavigateToProfile = {
                    navController.navigate(NavigationRoute.Profile)
                },
                onNavigateToSettings = {
                    navController.navigate(NavigationRoute.Settings)
                },
                onLogout = {
                    appViewModel.logout()
                }
            )
        }

        composable<NavigationRoute.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = {
                    navController.navigate(NavigationRoute.About)
                },
                onNavigateToPrivacyPolicy = {
                    navController.navigate(NavigationRoute.PrivacyPolicy)
                },
                onNavigateToTermsOfService = {
                    navController.navigate(NavigationRoute.TermsOfService)
                }
            )
        }

        composable<NavigationRoute.About> {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<NavigationRoute.PrivacyPolicy> {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<NavigationRoute.TermsOfService> {
            TermsOfServiceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<NavigationRoute.Profile> {
            ProfileScreen(
                currentUser = appState.currentUser,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    appViewModel.logout()
                }
            )
        }
    }
}