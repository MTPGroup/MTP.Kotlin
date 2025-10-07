package tech.hanasaki.momotalk_plus.features.chats.presentation.state

import tech.hanasaki.momotalk_plus.core.domain.repository.ContactInfo
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Chat


data class ChatsState(
    val isLoading: Boolean = true,
    val chatList: List<Chat> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val showCreateChatDialog: Boolean = false,
    val availableContacts: List<ContactInfo> = emptyList(),
    val isLoadingContacts: Boolean = false,
    val isCreatingChat: Boolean = false,
) {
    val filteredChatList: List<Chat>
        get() =
            if (searchQuery.isBlank()) {
                chatList
            } else {
                chatList.filter {
                    it.title.contains(
                        searchQuery,
                        ignoreCase = true
                    )
                }
            }
}

sealed class ChatsIntent {
    data object LoadChats : ChatsIntent()
    data class DeleteChat(val chatId: String) : ChatsIntent()
    data class PinChat(val chatId: String) : ChatsIntent()
    data class ChatClicked(val chatId: String) : ChatsIntent()
    data class UpdateSearchQuery(val query: String) : ChatsIntent()
    data object ShowCreateChatDialog : ChatsIntent()
    data object DismissCreateChatDialog : ChatsIntent()
    data object LoadAvailableContacts : ChatsIntent()
    data class CreateChat(
        val characterId: String,
        val title: String,
        val description: String,
        val avatarUrl: String,
    ) : ChatsIntent()
}

sealed class ChatsSideEffect {
    data class NavigateToChatDetails(val chatId: String) : ChatsSideEffect()
    data class ShowToast(val message: String) : ChatsSideEffect()
}