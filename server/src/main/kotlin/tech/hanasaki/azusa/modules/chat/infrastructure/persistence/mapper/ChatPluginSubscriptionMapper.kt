package tech.hanasaki.azusa.modules.chat.infrastructure.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.chat.domain.model.ChatPluginSubscription
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.table.ChatPluginSubscriptionTable
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.common.domain.model.PluginId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatPluginSubscriptionId

object ChatPluginSubscriptionMapper {
    fun toDomain(row: ResultRow): ChatPluginSubscription = ChatPluginSubscription.reconstitute(
        id = ChatPluginSubscriptionId(row[ChatPluginSubscriptionTable.id]),
        chatId = ChatId(row[ChatPluginSubscriptionTable.chatId]),
        pluginId = PluginId(row[ChatPluginSubscriptionTable.pluginId]),
        enabled = row[ChatPluginSubscriptionTable.enabled],
        config = row[ChatPluginSubscriptionTable.config],
        createdAt = row[ChatPluginSubscriptionTable.createdAt],
    )

    fun toEntity(domain: ChatPluginSubscription, target: UpdateBuilder<*>) {
        target[ChatPluginSubscriptionTable.chatId] = domain.chatId.value
        target[ChatPluginSubscriptionTable.pluginId] = domain.pluginId.value
        target[ChatPluginSubscriptionTable.enabled] = domain.enabled
        target[ChatPluginSubscriptionTable.config] = domain.config
    }
}
