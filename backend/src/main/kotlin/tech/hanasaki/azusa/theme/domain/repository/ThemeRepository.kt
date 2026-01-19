package tech.hanasaki.azusa.theme.domain.repository

import tech.hanasaki.azusa.shared.ThemeId
import tech.hanasaki.azusa.theme.domain.model.Theme

interface ThemeRepository {
    fun findByThemeId(id: ThemeId): Theme
    fun save(theme: Theme)
}