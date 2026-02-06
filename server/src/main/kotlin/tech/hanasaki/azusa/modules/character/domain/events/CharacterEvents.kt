package tech.hanasaki.azusa.modules.character.domain.events

import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

sealed class CharacterEvent : DomainEvent {
    abstract val characterId: CharacterId

    override val aggregateId: String
        get() = characterId.toString()
    override val aggregateType: String
        get() = "Character"
}

data class CharacterCreated(
    override val characterId: CharacterId,
    val authorId: UserId,
    val name: String,
    val isPublic: Boolean,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "character.created",
    override val occurredOn: Instant = Clock.System.now(),
) : CharacterEvent()

data class CharacterUpdated(
    override val characterId: CharacterId,
    val authorId: UserId,
    val name: String,
    val isPublic: Boolean,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "character.updated",
    override val occurredOn: Instant = Clock.System.now(),
) : CharacterEvent()

data class CharacterDeleted(
    override val characterId: CharacterId,
    val authorId: UserId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "character.deleted",
    override val occurredOn: Instant = Clock.System.now(),
) : CharacterEvent()
