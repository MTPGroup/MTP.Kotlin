package tech.hanasaki.momotalk_plus.features.home.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import tech.hanasaki.momotalk_plus.features.home.presentation.ui.widgets.HomeTopAppBar
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
                is HomeSideEffect.NavigateToNewChat -> onNavigateToChat
                HomeSideEffect.NavigateToProfile -> onNavigateToProfile
                HomeSideEffect.NavigateToLogin -> {
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
            topBar = {
                HomeTopAppBar(
                    title = when (uiState.currentTab) {
                        HomeTab.Chats -> ""
                        HomeTab.Contacts -> "联系人"
                    },
                    avatarUrl = currentUser?.picture,
                    username = currentUser?.name ?: "未登录",
                    onIntent = onIntent,
                    onAvatarClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                )
            },
            bottomBar = {
                HomeBottomNavigationBar(
                    currentTab = uiState.currentTab,
                    onTabSelected = { tab -> onIntent(HomeIntent.TabSelected(tab)) }
                )
            },
        ) { paddingValues ->
            NavHost(
                navController = tabNavController,
                startDestination = HomeTab.Chats,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable<HomeTab.Chats> {
                    ChatsScreen(onNavigateToChat = onNavigateToChat)
                }
                composable<HomeTab.Contacts> {
                    ContactsScreen()
                }
            }
        }
    }
}

