package tech.hanasaki.azusa.modules.chat.adapter.out.serializer

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import tech.hanasaki.azusa.modules.chat.domain.model.MessageContent

/**
 * MessageContent 序列化器 - 用于 JSONB 类型转换
 */
object MessageContentSerializer {

    /**
     * 将 List<MessageContent> 序列化为 JsonArray
     */
    fun serialize(contents: List<MessageContent>): JsonArray {
        return JsonArray(
            contents.map { content ->
                when (content) {
                    is MessageContent.Text -> JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("text"),
                            "content" to JsonPrimitive(content.content),
                        ),
                    )

                    is MessageContent.Image -> JsonObject(
                        mutableMapOf(
                            "type" to JsonPrimitive("image"),
                            "url" to JsonPrimitive(content.url),
                        ).apply {
                            content.alt?.let { put("alt", JsonPrimitive(it)) }
                        },
                    )

                    is MessageContent.File -> JsonObject(
                        mutableMapOf(
                            "type" to JsonPrimitive("file"),
                            "url" to JsonPrimitive(content.url),
                            "fileName" to JsonPrimitive(content.fileName),
                        ).apply {
                            content.fileSize?.let { put("fileSize", JsonPrimitive(it)) }
                        },
                    )

                    is MessageContent.Code -> JsonObject(
                        mutableMapOf(
                            "type" to JsonPrimitive("code"),
                            "code" to JsonPrimitive(content.code),
                        ).apply {
                            content.language?.let { put("language", JsonPrimitive(it)) }
                        },
                    )
                }
            },
        )
    }

    /**
     * 将 JsonArray 反序列化为 List<MessageContent>
     */
    fun deserialize(json: JsonElement): List<MessageContent> {
        if (json !is JsonArray) {
            throw SerializationException("Expected JsonArray, got ${json::class.simpleName}")
        }

        return json.map { element ->
            if (element !is JsonObject) {
                throw SerializationException("Expected JsonObject in array, got ${element::class.simpleName}")
            }

            val type = element["type"]?.jsonPrimitive?.content
                ?: throw SerializationException("Missing 'type' field in message content")

            when (type) {
                "text" -> {
                    val content = element["content"]?.jsonPrimitive?.content
                        ?: throw SerializationException("Missing 'content' field in text message")
                    MessageContent.Text(content)
                }

                "image" -> {
                    val url = element["url"]?.jsonPrimitive?.content
                        ?: throw SerializationException("Missing 'url' field in image message")
                    val alt = element["alt"]?.jsonPrimitive?.content
                    MessageContent.Image(url, alt)
                }

                "file" -> {
                    val url = element["url"]?.jsonPrimitive?.content
                        ?: throw SerializationException("Missing 'url' field in file message")
                    val fileName = element["fileName"]?.jsonPrimitive?.content
                        ?: throw SerializationException("Missing 'fileName' field in file message")
                    val fileSize = element["fileSize"]?.jsonPrimitive?.content?.toLongOrNull()
                    MessageContent.File(url, fileName, fileSize)
                }

                "code" -> {
                    val code = element["code"]?.jsonPrimitive?.content
                        ?: throw SerializationException("Missing 'code' field in code message")
                    val language = element["language"]?.jsonPrimitive?.content
                    MessageContent.Code(code, language)
                }

                else -> throw SerializationException("Unknown message content type: $type")
            }
        }
    }
}
