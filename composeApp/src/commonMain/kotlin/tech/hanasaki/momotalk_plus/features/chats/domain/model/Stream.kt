package tech.hanasaki.momotalk_plus.features.chats.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class StreamEvent {
    @Serializable
    data class Delta(val text: String) : StreamEvent()

    @Serializable
    data class ToolCallStart(
        val name: String,
        val argumentsJson: String,
    ) : StreamEvent()

    @Serializable
    data class ToolCallResult(
        val name: String,
        val result: String,
    ) : StreamEvent()

    @Serializable
    data class Done(val fullContent: String) : StreamEvent()

    @Serializable
    data class Error(val message: String) : StreamEvent()
}
