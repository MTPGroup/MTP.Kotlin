package tech.hanasaki.azusa.modules.character.infrastructure.persistence.repository

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.modules.character.domain.repository.CharacterRepository
import tech.hanasaki.azusa.modules.character.infrastructure.persistence.table.CharacterTable
import tech.hanasaki.azusa.shared.domain.model.CharacterId
import tech.hanasaki.azusa.shared.domain.model.UserId
import tech.hanasaki.azusa.shared.infrastructure.database.dbQuery

class ExposedCharacterRepository : CharacterRepository {
    override suspend fun findById(id: CharacterId): Character? = dbQuery {
        CharacterTable.selectAll()
            .where { CharacterTable.id eq id.value }
            .map(::toDomain)
            .singleOrNull()
    }

    override suspend fun findByAuthorId(authorId: UserId): List<Character> = dbQuery {
        CharacterTable.selectAll()
            .where { CharacterTable.authorId eq authorId.value }
            .map(::toDomain)
    }

    override suspend fun findPublicCharacters(): List<Character> = dbQuery {
        CharacterTable.selectAll()
            .where { CharacterTable.isPublic eq true }
            .map(::toDomain)
    }

    override suspend fun save(character: Character): Unit = dbQuery {
        val updatedRows = CharacterTable.update({ CharacterTable.id eq character.id.value }) {
            it[authorId] = character.authorId.value
            it[name] = character.name
            it[avatar] = character.avatar
            it[bio] = character.bio
            it[originPrompt] = character.originPrompt
            it[isPublic] = character.isPublic
            it[updatedAt] = character.updatedAt.toLocalDateTime(TimeZone.UTC)
        }
        if (updatedRows == 0) {
            CharacterTable.insert {
                it[id] = character.id.value
                it[authorId] = character.authorId.value
                it[name] = character.name
                it[avatar] = character.avatar
                it[bio] = character.bio
                it[originPrompt] = character.originPrompt
                it[isPublic] = character.isPublic
                it[createdAt] = character.createdAt.toLocalDateTime(TimeZone.UTC)
                it[updatedAt] = character.updatedAt.toLocalDateTime(TimeZone.UTC)
            }
        }
    }

    override suspend fun deleteById(id: CharacterId): Unit = dbQuery {
        CharacterTable.deleteWhere { CharacterTable.id eq id.value }
    }

    private fun toDomain(row: ResultRow): Character = Character(
        id = CharacterId(row[CharacterTable.id]),
        authorId = UserId(row[CharacterTable.authorId]),
        name = row[CharacterTable.name],
        avatar = row[CharacterTable.avatar],
        bio = row[CharacterTable.bio],
        originPrompt = row[CharacterTable.originPrompt],
        isPublic = row[CharacterTable.isPublic],
        createdAt = row[CharacterTable.createdAt].toInstant(TimeZone.UTC),
        updatedAt = row[CharacterTable.updatedAt].toInstant(TimeZone.UTC),
    )
}
