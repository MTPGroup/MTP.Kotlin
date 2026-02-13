package tech.hanasaki.azusa.modules.chat.application.service

import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfig
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig

fun buildSystemPrompt(
    originPrompt: String,
    chatSystemPrompt: String?,
): String {
    val parts = mutableListOf<String>()
    if (originPrompt.isNotBlank()) {
        parts.add(originPrompt)
    }
    if (!chatSystemPrompt.isNullOrBlank()) {
        parts.add(chatSystemPrompt)
    }
    return parts.joinToString("\n\n")
}

fun applyLLMOverrides(
    base: LLMConfig,
    chatConfig: ChatConfig?,
): LLMConfig {
    if (chatConfig == null) return base
    return base.copy(
        temperature = chatConfig.temperature?.toFloat() ?: base.temperature,
        maxTokens = chatConfig.maxTokens ?: base.maxTokens,
    )
}

fun trimHistoryByTokenBudget(messages: List<Message>, budget: Int): List<Message> {
    var remaining = budget
    val result = mutableListOf<Message>()
    for (msg in messages.asReversed()) {
        val tokens = estimateTokens(msg.getPlainText())
        if (remaining - tokens < 0 && result.isNotEmpty()) break
        result.add(msg)
        remaining -= tokens
    }
    return result.asReversed()
}

private fun estimateTokens(text: String): Int {
    var count = 0
    for (char in text) {
        count += if (char.code > 0x4E00) 2 else 1
    }
    return count / 3
}
