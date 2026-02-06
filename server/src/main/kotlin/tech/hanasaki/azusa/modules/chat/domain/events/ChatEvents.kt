package tech.hanasaki.azusa.modules.chat.domain.events

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
sealed class ChatEvent : DomainEvent {
    abstract val chatId: ChatId
    override val aggregateId: String = chatId.toString()
    override val aggregateType: String = "Chat"
    override val occurredOn: Instant = Clock.System.now()
}

/**
 * 聊天创建事件
 */

@Serializable
data class ChatCreated(
    override val chatId: ChatId,
    val ownerId: UserId,
    val characterId: CharacterId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "chat.created",
) : ChatEvent()
