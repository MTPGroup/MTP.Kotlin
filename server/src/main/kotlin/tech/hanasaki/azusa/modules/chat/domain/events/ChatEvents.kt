package tech.hanasaki.azusa.modules.chat.domain.events

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.model.CharacterId
import tech.hanasaki.azusa.shared.domain.model.UserId
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 聊天创建事件
 */
@Serializable
data class ChatCreatedEvent(
    val chatId: ChatId,
    val ownerId: UserId,
    val characterId: CharacterId,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    @Contextual
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent
