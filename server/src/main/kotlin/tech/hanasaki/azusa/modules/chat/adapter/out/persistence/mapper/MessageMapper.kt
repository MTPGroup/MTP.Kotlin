package tech.hanasaki.azusa.modules.chat.adapter.out.persistence.mapper

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.modules.chat.domain.model.SenderType
import tech.hanasaki.azusa.modules.chat.adapter.out.serializer.MessageContentSerializer
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.table.MessageTable
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId

object MessageMapper {
    fun toDomain(row: ResultRow): Message {
        val contentJson = row[MessageTable.content]
        val content = MessageContentSerializer.deserialize(contentJson)

        val metadata = row[MessageTable.metadata]
        val metadataObj = if (metadata == JsonObject(emptyMap())) null else metadata

        return Message.reconstitute(
            id = tech.hanasaki.azusa.modules.chat.domain.model.MessageId(row[MessageTable.id]),
            chatId = ChatId(row[MessageTable.chatId]),
            senderType = SenderType.valueOf(row[MessageTable.senderType]),
            content = content,
            metadata = metadataObj,
            createdAt = row[MessageTable.createdAt],
        )
    }

    fun toEntity(domain: Message, target: UpdateBuilder<*>) {
        target[MessageTable.chatId] = domain.chatId.value
        target[MessageTable.senderType] = domain.senderType.name
        target[MessageTable.senderProfileId] = if (domain.senderType == SenderType.USER) {
            target[MessageTable.senderCharacterId] = null
            null
        } else {
            target[MessageTable.senderProfileId] = null
            null
        }

        val contentJson = MessageContentSerializer.serialize(domain.content)
        target[MessageTable.content] = contentJson

        target[MessageTable.metadata] = domain.metadata ?: JsonObject(emptyMap())
        target[MessageTable.updatedAt] = domain.createdAt
    }
}
