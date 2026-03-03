package tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MessageContentDto(
    val type: String,
    val content: String? = null,
    val url: String? = null,
    val alt: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val language: String? = null,
    val mimeType: String? = null,
)

@Serializable
data class MessageResponseDto(
    val id: String,
    val senderType: String,
    val content: List<MessageContentDto>,
    val createdAt: String,
)

@Serializable
data class SendMessageRequest(
    val content: List<MessageContentDto>,
)

@Serializable
data class ToolCallStartData(
    val name: String,
    val arguments: kotlinx.serialization.json.JsonObject,
)

@Serializable
data class ToolCallResultData(
    val name: String,
    val result: String,
)
