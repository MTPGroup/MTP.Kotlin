package tech.hanasaki.azusa.modules.chat.adapter.out.persistence.mapper

import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.modules.chat.domain.model.SenderType
import tech.hanasaki.azusa.modules.chat.adapter.out.serializer.MessageContentSerializer
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.table.MessageTable
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.MessageId

object MessageMapper {
    fun toDomain(row: ResultRow): Message {
        val contentJson = row[MessageTable.content]
        val content = MessageContentSerializer.deserialize(contentJson)

        val metadata = row[MessageTable.metadata]
        val metadataObj = if (metadata == JsonObject(emptyMap())) null else metadata

        val senderType = SenderType.valueOf(row[MessageTable.senderType].uppercase())
        val senderId = when (senderType) {
            SenderType.USER -> row[MessageTable.senderProfileId]!!
            SenderType.CHARACTER -> row[MessageTable.senderCharacterId]!!
        }

        return Message.reconstitute(
            id = MessageId(row[MessageTable.id]),
            chatId = ChatId(row[MessageTable.chatId]),
            senderType = senderType,
            senderId = senderId,
            content = content,
            metadata = metadataObj,
            createdAt = row[MessageTable.createdAt],
        )
    }

    fun toEntity(domain: Message, target: UpdateBuilder<*>) {
        target[MessageTable.chatId] = domain.chatId.value
        target[MessageTable.senderType] = domain.senderType.name.lowercase()

        when (domain.senderType) {
            SenderType.USER -> {
                target[MessageTable.senderProfileId] = domain.senderId
                target[MessageTable.senderCharacterId] = null
            }
            SenderType.CHARACTER -> {
                target[MessageTable.senderProfileId] = null
                target[MessageTable.senderCharacterId] = domain.senderId
            }
        }

        val contentJson = MessageContentSerializer.serialize(domain.content)
        target[MessageTable.content] = contentJson

        target[MessageTable.metadata] = domain.metadata ?: JsonObject(emptyMap())
        target[MessageTable.updatedAt] = domain.createdAt
    }
}
