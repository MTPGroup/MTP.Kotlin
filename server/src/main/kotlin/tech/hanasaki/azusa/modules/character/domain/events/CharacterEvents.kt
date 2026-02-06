package tech.hanasaki.azusa.modules.character.domain.events

import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

sealed class CharacterEvent : DomainEvent {
    abstract val characterId: CharacterId

    override val aggregateId: String = characterId.toString()
    override val aggregateType: String = "Character"
    override val occurredOn: Instant = Clock.System.now()
}

data class CharacterCreated(
    override val characterId: CharacterId,
    val authorId: UserId,
    val name: String,
    val isPublic: Boolean,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "character.created",
) : CharacterEvent()

data class CharacterUpdated(
    override val characterId: CharacterId,
    val authorId: UserId,
    val name: String,
    val isPublic: Boolean,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "character.updated",
) : CharacterEvent()

data class CharacterDeleted(
    override val characterId: CharacterId,
    val authorId: UserId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "character.deleted",
) : CharacterEvent()
