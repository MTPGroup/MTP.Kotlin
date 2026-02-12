package tech.hanasaki.azusa.modules.chat.adapter.out.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfig
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.table.ChatConfigTable
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfigId

object ChatConfigMapper {
    fun toDomain(row: ResultRow): ChatConfig = ChatConfig.reconstitute(
        id = ChatConfigId(row[ChatConfigTable.id]),
        chatId = ChatId(row[ChatConfigTable.chatId]),
        temperature = row[ChatConfigTable.temperature]?.toDouble(),
        maxTokens = row[ChatConfigTable.maxTokens],
        topP = row[ChatConfigTable.topP]?.toDouble(),
        systemPrompt = row[ChatConfigTable.systemPrompt],
        createdAt = row[ChatConfigTable.createdAt],
        updatedAt = row[ChatConfigTable.updatedAt],
    )

    fun toEntity(domain: ChatConfig, target: UpdateBuilder<*>) {
        target[ChatConfigTable.chatId] = domain.chatId.value
        target[ChatConfigTable.temperature] = domain.temperature?.toBigDecimal()
        target[ChatConfigTable.maxTokens] = domain.maxTokens
        target[ChatConfigTable.topP] = domain.topP?.toBigDecimal()
        target[ChatConfigTable.systemPrompt] = domain.systemPrompt
        target[ChatConfigTable.updatedAt] = domain.updatedAt
    }
}
