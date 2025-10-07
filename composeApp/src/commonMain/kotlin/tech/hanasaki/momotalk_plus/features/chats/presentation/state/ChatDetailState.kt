package tech.hanasaki.momotalk_plus.features.chats.presentation.state

import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message

data class ChatDetailState(
    val inputMessage: String = "",
    val chatId: String = "",
    val title: String = "",
    val avatar: String? = null,
    val characterName: String = "",
    val characterAvatar: String? = null,
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = true,
    val isStreaming: Boolean = false,
    val streamingMessageId: String? = null,
    val isTyping: Boolean = false,
    val error: String? = null,
)

sealed class ChatDetailIntent {
    data class LoadChat(val chatId: String) : ChatDetailIntent()
    data class InputMessageChanged(val message: String) : ChatDetailIntent()
    data class SendMessage(
        val chatId: String,
        val message: String,
        val currentUser: UserProfile?,
    ) : ChatDetailIntent()

    data class ClearChatHistory(val chatId: String) : ChatDetailIntent()
    data object ClearError : ChatDetailIntent()
}

sealed class ChatDetailSideEffect {
    data class ShowToast(val message: String) : ChatDetailSideEffect()
    data object ScrollToBottom : ChatDetailSideEffect()
}