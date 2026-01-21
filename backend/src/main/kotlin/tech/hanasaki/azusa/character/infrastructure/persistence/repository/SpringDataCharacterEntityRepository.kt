package tech.hanasaki.azusa.character.infrastructure.persistence.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.character.infrastructure.persistence.entity.CharacterEntity
import java.util.UUID

@Repository
interface SpringDataCharacterEntityRepository : CrudRepository<CharacterEntity, UUID> {
    fun findByAuthorId(authorId: UUID): List<CharacterEntity>
    fun findByIsPublicTrue(): List<CharacterEntity>
}
