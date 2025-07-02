package tech.hanasaki.momotalk_plus.features.home.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import tech.hanasaki.momotalk_plus.features.home.presentation.ui.widgets.HomeTopAppBar
import tech.hanasaki.momotalk_plus.features.home.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
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
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = "<UNK>", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
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
                            // 临时登出
                            appViewModel.logout()
//                            drawerState.open()
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

