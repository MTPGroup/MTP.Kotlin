package tech.hanasaki.momotalk_plus.features.home.presentation.ui

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
import tech.hanasaki.momotalk_plus.app.viewmodel.AppViewModel
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
    onNavigateToChat: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = koinViewModel(),
    appViewModel: AppViewModel = koinViewModel(),
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val onIntent = homeViewModel::processIntent

    val appState by appViewModel.uiState.collectAsState()
    val currentUser = appState.currentUser

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = homeViewModel.sideEffect) {
        homeViewModel.sideEffect.collect { effect ->
            when (effect) {
                is HomeSideEffect.NavigateToNewChat -> onNavigateToChat("")
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
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                currentUser = currentUser,
                onProfileClick = { onIntent(HomeIntent.ProfileClicked) },
                onLogoutClick = {
                    onIntent(HomeIntent.LogoutClicked)
                }
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
                composable<HomeTab.Chats> {
                    ChatsScreen(
                        currentUser = currentUser,
                        onAvatarClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        onNavigateToChat = onNavigateToChat
                    )
                }
                composable<HomeTab.Contacts> {
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

