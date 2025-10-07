package tech.hanasaki.momotalk_plus.features.chats.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class ChatsRoute {
    @Serializable
    object ChatsList : ChatsRoute()

    @Serializable
    data class ChatDetail(val chatId: String) : ChatsRoute()
}