package tech.hanasaki.momotalk_plus.features.chats.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.features.chats.presentation.navigation.ChatsRoute

@Composable
fun ChatsScreen(
    currentUser: UserProfile?,
    onAvatarClick: () -> Unit,
    onSetBottomBarVisibility: (Boolean) -> Unit,
) {
    val chatsNavController: NavHostController = rememberNavController()

    LaunchedEffect(chatsNavController) {
        chatsNavController.currentBackStackEntryFlow.collect { entry ->
            onSetBottomBarVisibility(entry.destination.route?.contains("ChatsList") == true)
        }
    }

    NavHost(
        chatsNavController,
        startDestination = ChatsRoute.ChatsList
    ) {
        composable<ChatsRoute.ChatsList> {
            ChatListPage(
                currentUser = currentUser,
                onAvatarClick = onAvatarClick,
                onNavigateToChatDetails = { chatId ->
                    chatsNavController.navigate(ChatsRoute.ChatDetail(chatId))
                },
            )
        }

        composable<ChatsRoute.ChatDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<ChatsRoute.ChatDetail>()
            ChatDetailPage(
                chatId = route.chatId,
                currentUser = currentUser,
                onNavigateBack = { chatsNavController.popBackStack() }
            )
        }
    }

}
