package tech.hanasaki.momotalk_plus.features.home.presentation.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.features.chats.presentation.ui.ChatsScreen
import tech.hanasaki.momotalk_plus.features.contacts.presentation.ui.ContactsScreen
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeIntent
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeSideEffect
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeTab
import tech.hanasaki.momotalk_plus.features.home.presentation.ui.widgets.HomeBottomNavigationBar
import tech.hanasaki.momotalk_plus.features.home.presentation.ui.widgets.HomeDrawerContent
import tech.hanasaki.momotalk_plus.features.home.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    currentUser: User?,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by homeViewModel.container.stateFlow.collectAsState()
    val onIntent = homeViewModel::onIntent

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(homeViewModel.container) {
        homeViewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is HomeSideEffect.NavigateToSettings -> onNavigateToSettings()
                is HomeSideEffect.NavigateToProfile -> onNavigateToProfile()
                is HomeSideEffect.NavigateToLogin -> {
                    scope.launch {
                        drawerState.close()
                        onLogout()
                    }
                }
            }
        }
    }

    val tabNavController = rememberNavController()

    LaunchedEffect(uiState.currentTab) {
        tabNavController.navigate(uiState.currentTab) {
            popUpTo(tabNavController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                currentUser = currentUser,
                onSettingsClick = { onIntent(HomeIntent.SettingsClicked) },
                onProfileClick = { onIntent(HomeIntent.ProfileClicked) },
                onLogoutClick = {
                    onIntent(HomeIntent.LogoutClicked)
                },
            )
        },
    ) {
        Scaffold(
            bottomBar = {
                if (uiState.showBottomBar) {
                    HomeBottomNavigationBar(
                        currentTab = uiState.currentTab,
                        onTabSelected = { tab -> onIntent(HomeIntent.TabSelected(tab)) }
                    )
                }
            },
        ) {
            NavHost(
                navController = tabNavController,
                startDestination = HomeTab.Chats,
            ) {
                composable<HomeTab.Chats>(
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) {
                    ChatsScreen(
                        currentUser = currentUser,
                        onAvatarClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        onSetBottomBarVisibility = { visible ->
                            onIntent(HomeIntent.SetBottomBarVisibility(visible))
                        },
                    )
                }
                composable<HomeTab.Contacts>(
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) {
                    ContactsScreen(
                        currentUser = currentUser,
                        onAvatarClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        onSetBottomBarVisibility = { visible ->
                            onIntent(HomeIntent.SetBottomBarVisibility(visible))
                        }
                    )
                }
            }
        }
    }
}

