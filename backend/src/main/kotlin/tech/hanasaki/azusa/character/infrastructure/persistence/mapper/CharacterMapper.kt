package tech.hanasaki.azusa.character.infrastructure.persistence.mapper

import org.springframework.stereotype.Component
import tech.hanasaki.azusa.character.domain.model.Character
import tech.hanasaki.azusa.character.infrastructure.persistence.entity.CharacterEntity
import tech.hanasaki.azusa.shared.CharacterId
import tech.hanasaki.azusa.shared.UserId
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Component
class CharacterMapper {
    fun toDomain(entity: CharacterEntity): Character = Character(
        id = CharacterId(entity.id),
        authorId = UserId(entity.authorId),
        name = entity.name,
        avatar = entity.avatar,
        bio = entity.bio,
        originPrompt = entity.originPrompt,
        isPublic = entity.isPublic,
        createdAt = entity.createdAt.toKotlinInstant(),
        updatedAt = entity.updatedAt.toKotlinInstant(),
    )

    fun toEntity(character: Character, isNewRecord: Boolean = false): CharacterEntity = CharacterEntity(
        id = character.id.value,
        authorId = character.authorId.value,
        name = character.name,
        avatar = character.avatar,
        bio = character.bio,
        originPrompt = character.originPrompt,
        isPublic = character.isPublic,
        createdAt = character.createdAt.toJavaInstant(),
        updatedAt = character.updatedAt.toJavaInstant(),
    ).apply {
        this.isNewRecord = isNewRecord
    }
}
