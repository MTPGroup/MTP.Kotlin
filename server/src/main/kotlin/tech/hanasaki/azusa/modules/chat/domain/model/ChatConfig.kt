package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.shared.domain.model.base.AggregateRoot
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@JvmInline
@Serializable
value class ChatConfigId(val value: Uuid)

/**
 * ChatConfig 实体
 */
class ChatConfig private constructor(
    val id: ChatConfigId,
    val chatId: ChatId,
    var temperature: Double?,
    var maxTokens: Int?,
    var topP: Double?,
    var systemPrompt: String?,
    val createdAt: Instant,
    var updatedAt: Instant,
) : AggregateRoot() {
    companion object {
        /**
         * 创建聊天配置
         */
        fun create(
            chatId: ChatId,
            temperature: Double? = null,
            maxTokens: Int? = null,
            topP: Double? = null,
            systemPrompt: String? = null,
        ): ChatConfig {
            val now = Clock.System.now()
            return ChatConfig(
                id = ChatConfigId(Uuid.random()),
                chatId = chatId,
                temperature = temperature,
                maxTokens = maxTokens,
                topP = topP,
                systemPrompt = systemPrompt,
                createdAt = now,
                updatedAt = now,
            )
        }

        /**
         * 从持久化层重建配置
         */
        fun reconstitute(
            id: ChatConfigId,
            chatId: ChatId,
            temperature: Double?,
            maxTokens: Int?,
            topP: Double?,
            systemPrompt: String?,
            createdAt: Instant,
            updatedAt: Instant,
        ): ChatConfig = ChatConfig(
            id = id,
            chatId = chatId,
            temperature = temperature,
            maxTokens = maxTokens,
            topP = topP,
            systemPrompt = systemPrompt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    /**
     * 获取或创建聊天配置
     */
    fun getOrCreate(chatId: ChatId): ChatConfig {
        val now = Clock.System.now()
        return ChatConfig(
            id = ChatConfigId(Uuid.random()),
            chatId = chatId,
            temperature = null,
            maxTokens = null,
            topP = null,
            systemPrompt = null,
            createdAt = now,
            updatedAt = now,
        )
    }

    /**
     * 更新 LLM 参数
     */
    fun updateLLMParams(
        temperature: Double? = null,
        maxTokens: Int? = null,
        topP: Double? = null,
    ) {
        this.temperature = temperature
        this.maxTokens = maxTokens
        this.topP = topP
        this.updatedAt = Clock.System.now()
    }

    /**
     * 更新系统提示词
     */
    fun updateSystemPrompt(systemPrompt: String?) {
        this.systemPrompt = systemPrompt
        this.updatedAt = Clock.System.now()
    }
}
