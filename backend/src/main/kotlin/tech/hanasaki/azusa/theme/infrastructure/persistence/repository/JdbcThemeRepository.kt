package tech.hanasaki.azusa.theme.infrastructure.persistence.repository

import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.shared.ThemeId
import tech.hanasaki.azusa.shared.UserId
import tech.hanasaki.azusa.theme.domain.model.Theme
import tech.hanasaki.azusa.theme.domain.repository.ThemeRepository
import tech.hanasaki.azusa.theme.infrastructure.persistence.mapper.ThemeMapper

@Repository
class JdbcThemeRepository(
    private val themeRepository: SpringDataThemeEntityRepository,
    private val mapper: ThemeMapper,
) : ThemeRepository {
    override fun findByThemeId(id: ThemeId): Theme? =
        themeRepository.findById(id.value).orElse(null)?.let(mapper::toDomain)

    override fun findByAuthorId(authorId: UserId): List<Theme> =
        themeRepository.findByAuthorId(authorId.value).map(mapper::toDomain)

    override fun save(theme: Theme) {
        val exists = themeRepository.existsById(theme.id.value)
        val entity = mapper.toEntity(theme, !exists)
        themeRepository.save(entity)
    }

    override fun deleteByThemeId(id: ThemeId) {
        themeRepository.deleteById(id.value)
    }
}
