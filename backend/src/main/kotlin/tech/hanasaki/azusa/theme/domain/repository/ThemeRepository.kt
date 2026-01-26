package tech.hanasaki.azusa.theme.domain.repository

import tech.hanasaki.azusa.common.ThemeId
import tech.hanasaki.azusa.common.UserId
import tech.hanasaki.azusa.theme.domain.model.Theme

interface ThemeRepository {
    fun findByThemeId(id: ThemeId): Theme?
    fun findByAuthorId(authorId: UserId): List<Theme>
    fun save(theme: Theme)
    fun deleteByThemeId(id: ThemeId)
}
