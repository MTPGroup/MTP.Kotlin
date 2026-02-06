package tech.hanasaki.azusa.modules.chat.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatPluginSubscription
import tech.hanasaki.azusa.modules.chat.domain.model.ChatPluginSubscriptionId
import tech.hanasaki.azusa.modules.chat.domain.repository.ChatPluginSubscriptionRepository
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.mapper.ChatPluginSubscriptionMapper
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.table.ChatPluginSubscriptionTable

class ExposedChatPluginSubscriptionRepository : ChatPluginSubscriptionRepository {
    override suspend fun findById(id: ChatPluginSubscriptionId): ChatPluginSubscription? =
        ChatPluginSubscriptionTable.selectAll()
            .where { ChatPluginSubscriptionTable.id eq id.value }
            .map(ChatPluginSubscriptionMapper::toDomain)
            .singleOrNull()

    override suspend fun findByChatId(chatId: ChatId): List<ChatPluginSubscription> =
        ChatPluginSubscriptionTable.selectAll()
            .where { ChatPluginSubscriptionTable.chatId eq chatId.value }
            .map(ChatPluginSubscriptionMapper::toDomain)

    override suspend fun findEnabledByChatId(chatId: ChatId): List<ChatPluginSubscription> =
        ChatPluginSubscriptionTable.selectAll()
            .where {
                (ChatPluginSubscriptionTable.chatId eq chatId.value) and
                        (ChatPluginSubscriptionTable.enabled eq true)
            }
            .map(ChatPluginSubscriptionMapper::toDomain)

    override suspend fun findByChatIdAndPluginId(
        chatId: ChatId,
        pluginId: PluginId,
    ): ChatPluginSubscription? = ChatPluginSubscriptionTable.selectAll()
        .where {
            (ChatPluginSubscriptionTable.chatId eq chatId.value) and
                    (ChatPluginSubscriptionTable.pluginId eq pluginId.value)
        }
        .map(ChatPluginSubscriptionMapper::toDomain)
        .singleOrNull()

    override suspend fun save(subscription: ChatPluginSubscription) {
        val updatedRows =
            ChatPluginSubscriptionTable.update({ ChatPluginSubscriptionTable.id eq subscription.id.value }) {
                ChatPluginSubscriptionMapper.toEntity(subscription, it)
            }
        if (updatedRows == 0) {
            ChatPluginSubscriptionTable.insert {
                it[ChatPluginSubscriptionTable.id] = subscription.id.value
                ChatPluginSubscriptionMapper.toEntity(subscription, it)
                it[ChatPluginSubscriptionTable.createdAt] = subscription.createdAt
            }
        }
    }

    override suspend fun deleteById(id: ChatPluginSubscriptionId) {
        ChatPluginSubscriptionTable.deleteWhere { ChatPluginSubscriptionTable.id eq id.value }
    }

    override suspend fun deleteByChatId(chatId: ChatId) {
        ChatPluginSubscriptionTable.deleteWhere { ChatPluginSubscriptionTable.chatId eq chatId.value }
    }
}
