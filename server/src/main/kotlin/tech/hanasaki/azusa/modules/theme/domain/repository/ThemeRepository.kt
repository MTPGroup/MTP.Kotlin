package tech.hanasaki.azusa.modules.theme.domain.repository

import tech.hanasaki.azusa.modules.theme.domain.model.Theme
import tech.hanasaki.azusa.shared.domain.model.vo.ThemeId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface ThemeRepository {
    suspend fun findByThemeId(id: ThemeId): Theme?
    suspend fun findByAuthorId(authorId: UserId): List<Theme>
    suspend fun save(theme: Theme)
    suspend fun deleteByThemeId(id: ThemeId)
}
