package tech.hanasaki.momotalk_plus.features.chats.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class StreamEvent {
    @Serializable
    data class ReflectionChunk(val content: String) : StreamEvent()

    @Serializable
    data class Token(val content: String) : StreamEvent()

    @Serializable
    data class Error(val content: String) : StreamEvent()

    @Serializable
    data object Final : StreamEvent()
}

@Serializable
data class StreamChunk(
    val type: String,
    val content: String? = null,
)
