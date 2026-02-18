package tech.hanasaki.azusa.modules.chat.adapter.`in`.web.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.chat.domain.model.MessageContent

@Serializable
data class SendMessageRequest(val content: List<MessageContentDto>)

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

fun MessageContentDto.toDomain(): MessageContent = when (type) {
    "text" -> MessageContent.Text(content = content ?: "")
    "image" -> MessageContent.Image(url = url ?: "", alt = alt)
    "file" -> MessageContent.File(url = url ?: "", fileName = fileName ?: "", fileSize = fileSize)
    "code" -> MessageContent.Code(code = content ?: "", language = language)
    "audio" -> MessageContent.Audio(url = url ?: "", mimeType = mimeType)
    "video" -> MessageContent.Video(url = url ?: "", mimeType = mimeType)
    "pdf" -> MessageContent.Pdf(url = url ?: "", fileName = fileName)
    else -> MessageContent.Text(content = content ?: "")
}

@Serializable
data class ToolCallStartData(val name: String, val arguments: JsonObject)

@Serializable
data class ToolCallResultData(val name: String, val result: String)

fun MessageContent.toDto(): MessageContentDto = when (this) {
    is MessageContent.Text -> MessageContentDto(type = "text", content = content)
    is MessageContent.Image -> MessageContentDto(type = "image", url = url, alt = alt)
    is MessageContent.File -> MessageContentDto(type = "file", url = url, fileName = fileName, fileSize = fileSize)
    is MessageContent.Code -> MessageContentDto(type = "code", content = code, language = language)
    is MessageContent.Audio -> MessageContentDto(type = "audio", url = url, mimeType = mimeType)
    is MessageContent.Video -> MessageContentDto(type = "video", url = url, mimeType = mimeType)
    is MessageContent.Pdf -> MessageContentDto(type = "pdf", url = url, fileName = fileName)
}
