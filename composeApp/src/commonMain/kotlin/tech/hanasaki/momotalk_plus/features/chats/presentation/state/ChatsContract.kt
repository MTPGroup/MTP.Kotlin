package tech.hanasaki.momotalk_plus.features.chats.presentation.state

data class ChatListItem(
    val id: String,
    val avatarUrl: String?,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int
)

data class ChatsState(
    val isLoading: Boolean = true,
    val chatList: List<ChatListItem> = emptyList(),
    val error: String? = null
)

sealed class ChatsIntent {
    data class ChatClicked(val chatId: String) : ChatsIntent()
}

sealed class ChatsSideEffect {
    data class NavigateToChatDetails(val chatId: String) : ChatsSideEffect()
}