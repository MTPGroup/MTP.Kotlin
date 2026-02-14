package tech.hanasaki.azusa.modules.chat.domain.model

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * ChatConfig 值对象 — 嵌入 Chat 聚合根
 */
data class ChatConfig(
    var temperature: Double?,
    var maxTokens: Int?,
    var topP: Double?,
    var systemPrompt: String?,
    val createdAt: Instant,
    var updatedAt: Instant,
) {
    companion object {
        fun create(
            temperature: Double? = null,
            maxTokens: Int? = null,
            topP: Double? = null,
            systemPrompt: String? = null,
        ): ChatConfig {
            val now = Clock.System.now()
            return ChatConfig(
                temperature = temperature,
                maxTokens = maxTokens,
                topP = topP,
                systemPrompt = systemPrompt,
                createdAt = now,
                updatedAt = now,
            )
        }

        fun reconstitute(
            temperature: Double?,
            maxTokens: Int?,
            topP: Double?,
            systemPrompt: String?,
            createdAt: Instant,
            updatedAt: Instant,
        ): ChatConfig = ChatConfig(
            temperature = temperature,
            maxTokens = maxTokens,
            topP = topP,
            systemPrompt = systemPrompt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

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

    fun updateSystemPrompt(systemPrompt: String?) {
        this.systemPrompt = systemPrompt
        this.updatedAt = Clock.System.now()
    }
}
