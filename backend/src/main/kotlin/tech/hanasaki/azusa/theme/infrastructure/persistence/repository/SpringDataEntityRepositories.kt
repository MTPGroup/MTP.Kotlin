package tech.hanasaki.azusa.theme.infrastructure.persistence.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.theme.infrastructure.persistence.entity.ThemeEntity
import java.util.*

@Repository
interface SpringDataThemeEntityRepository : CrudRepository<ThemeEntity, UUID> {
    fun findByAuthorId(authorId: UUID): List<ThemeEntity>
}