package tech.hanasaki.azusa.modules.character.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.modules.character.domain.repository.CharacterRepository
import tech.hanasaki.azusa.modules.character.infrastructure.persistence.mapper.CharacterMapper
import tech.hanasaki.azusa.modules.character.infrastructure.persistence.table.CharacterTable
import tech.hanasaki.azusa.shared.domain.model.CharacterId
import tech.hanasaki.azusa.shared.domain.model.PageResult
import tech.hanasaki.azusa.shared.domain.model.UserId
import tech.hanasaki.azusa.shared.infrastructure.database.dbQuery

class ExposedCharacterRepository : CharacterRepository {
    override suspend fun findById(id: CharacterId): Character? = dbQuery {
        CharacterTable.selectAll()
            .where { CharacterTable.id eq id.value }
            .map(CharacterMapper::toDomain)
            .singleOrNull()
    }

    override suspend fun findByAuthorId(authorId: UserId): List<Character> = dbQuery {
        CharacterTable.selectAll()
            .where { CharacterTable.authorId eq authorId.value }
            .map(CharacterMapper::toDomain)
    }

    override suspend fun findPublicCharacters(): List<Character> = dbQuery {
        CharacterTable.selectAll()
            .where { CharacterTable.isPublic eq true }
            .map(CharacterMapper::toDomain)
    }

    override suspend fun save(character: Character): Unit = dbQuery {
        val updatedRows = CharacterTable.update({ CharacterTable.id eq character.id.value }) {
            CharacterMapper.toEntity(character, it)
            it[updatedAt] = character.updatedAt
        }
        if (updatedRows == 0) {
            CharacterTable.insert {
                it[id] = character.id.value
                CharacterMapper.toEntity(character, it)
                it[createdAt] = character.createdAt
                it[updatedAt] = character.updatedAt
            }
        }
    }

    override suspend fun deleteById(id: CharacterId): Unit = dbQuery {
        CharacterTable.deleteWhere { CharacterTable.id eq id.value }
    }

    override suspend fun findByAuthorIdPaged(authorId: UserId, page: Int, limit: Int): PageResult<Character> = dbQuery {
        val total = CharacterTable.selectAll()
            .where { CharacterTable.authorId eq authorId.value }
            .count()

        val items = CharacterTable.selectAll()
            .where { CharacterTable.authorId eq authorId.value }
            .orderBy(CharacterTable.updatedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(CharacterMapper::toDomain)

        PageResult(items, total, page, limit)
    }

    override suspend fun findPublicCharactersPaged(page: Int, limit: Int): PageResult<Character> = dbQuery {
        val total = CharacterTable.selectAll()
            .where { CharacterTable.isPublic eq true }
            .count()

        val items = CharacterTable.selectAll()
            .where { CharacterTable.isPublic eq true }
            .orderBy(CharacterTable.updatedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(CharacterMapper::toDomain)

        PageResult(items, total, page, limit)
    }

    override suspend fun searchPublicCharacters(query: String, page: Int, limit: Int): PageResult<Character> = dbQuery {
        val searchPattern = "%${query.lowercase()}%"
        val condition = { (CharacterTable.isPublic eq true) and (CharacterTable.name.lowerCase() like searchPattern) }

        val total = CharacterTable.selectAll()
            .where(condition)
            .count()

        val items = CharacterTable.selectAll()
            .where(condition)
            .orderBy(CharacterTable.updatedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(CharacterMapper::toDomain)

        PageResult(items, total, page, limit)
    }

}
