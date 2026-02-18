package tech.hanasaki.azusa.modules.chat.adapter.`in`.web.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.chat.domain.model.Chat
import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfig
import tech.hanasaki.azusa.modules.chat.domain.model.ChatPluginSubscription
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.infrastructure.web.response.PagedResponse
import kotlin.uuid.Uuid

@Serializable
data class ChatResponse(
    val id: Uuid,
    val name: String?,
    val lastMessage: String?,
    val characterId: Uuid?,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class MessageResponse(
    val id: Uuid,
    val senderType: String,
    val content: List<MessageContentDto>,
    val createdAt: String,
)

@Serializable
data class ChatConfigResponse(
    val temperature: Double?,
    val maxTokens: Int?,
    val topP: Double?,
    val systemPrompt: String?,
)

@Serializable
data class PluginSubscriptionResponse(
    val id: Uuid,
    val pluginId: Uuid,
    val enabled: Boolean,
    val config: JsonObject,
)

typealias PagedChatResponse = PagedResponse<ChatResponse>
typealias PagedMessageResponse = PagedResponse<MessageResponse>

fun Chat.toResponse(): ChatResponse = ChatResponse(
    id = id.value,
    name = name,
    lastMessage = lastMessage,
    characterId = getCharacter()?.value,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

fun Message.toResponse(): MessageResponse = MessageResponse(
    id = id.value,
    senderType = senderType.name,
    content = content.map { it.toDto() },
    createdAt = createdAt.toString(),
)

fun ChatConfig.toResponse(): ChatConfigResponse = ChatConfigResponse(
    temperature = temperature,
    maxTokens = maxTokens,
    topP = topP,
    systemPrompt = systemPrompt,
)

fun ChatPluginSubscription.toResponse(): PluginSubscriptionResponse = PluginSubscriptionResponse(
    id = id.value,
    pluginId = pluginId.value,
    enabled = enabled,
    config = config,
)

fun PageResult<Chat>.toResponse(): PagedChatResponse = PagedChatResponse(
    items = items.map { it.toResponse() },
    total = total,
    page = page,
    limit = limit,
    totalPages = totalPages,
    hasNext = hasNext,
    hasPrevious = hasPrevious,
)

fun PageResult<Message>.toMessageResponse(): PagedMessageResponse = PagedMessageResponse(
    items = items.map { it.toResponse() },
    total = total,
    page = page,
    limit = limit,
    totalPages = totalPages,
    hasNext = hasNext,
    hasPrevious = hasPrevious,
)
