package tech.hanasaki.azusa.modules.chat.domain.events

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.common.kernel.event.DomainEvent
import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.kernel.model.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * 聊天创建事件
 */

@Serializable
data class ChatCreatedEvent(
    val chatId: ChatId,
    val ownerId: UserId,
    val characterId: CharacterId,
    @Contextual
    override val eventId: Uuid = Uuid.random(),
    @Contextual
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent
