package tech.hanasaki.azusa.modules.chat.domain.events

import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

sealed class ChatEvent : DomainEvent {
    abstract val chatId: ChatId
    override val aggregateId: String = chatId.toString()
    override val aggregateType: String = "Chat"
}

data class ChatCreated(
    override val chatId: ChatId,
    val ownerId: UserId,
    val characterId: CharacterId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "chat.created",
    override val occurredOn: Instant = Clock.System.now(),
) : ChatEvent()
