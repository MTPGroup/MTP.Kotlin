package tech.hanasaki.azusa.modules.character.adapter.out.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.character.adapter.out.persistence.table.CharacterTable
import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.shared.domain.model.vo.AvatarUrl
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

object CharacterMapper {
    fun toDomain(row: ResultRow): Character = Character.reconstitute(
        id = CharacterId(row[CharacterTable.id]),
        authorId = UserId(row[CharacterTable.authorId]),
        name = row[CharacterTable.name],
        avatar = row[CharacterTable.avatar]?.let { AvatarUrl(it) },
        bio = row[CharacterTable.bio],
        originPrompt = row[CharacterTable.originPrompt],
        isPublic = row[CharacterTable.isPublic],
        createdAt = row[CharacterTable.createdAt],
        updatedAt = row[CharacterTable.updatedAt],
    )

    fun toEntity(domain: Character, target: UpdateBuilder<*>) {
        target[CharacterTable.authorId] = domain.authorId.value
        target[CharacterTable.name] = domain.name
        target[CharacterTable.avatar] = domain.avatar?.value
        target[CharacterTable.bio] = domain.bio
        target[CharacterTable.originPrompt] = domain.originPrompt
        target[CharacterTable.isPublic] = domain.isPublic
    }
}