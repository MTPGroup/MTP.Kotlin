package tech.hanasaki.azusa.modules.character.domain.events

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.common.kernel.event.DomainEvent
import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.kernel.model.UserId
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class CharacterCreatedEvent(
    val characterId: CharacterId,
    val authorId: UserId,
    val name: String,
    val isPublic: Boolean,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    @Contextual
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

@Serializable
data class CharacterUpdatedEvent(
    val characterId: CharacterId,
    val authorId: UserId,
    val name: String,
    val isPublic: Boolean,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    @Contextual
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

@Serializable
data class CharacterDeletedEvent(
    val characterId: CharacterId,
    val authorId: UserId,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    @Contextual
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent
