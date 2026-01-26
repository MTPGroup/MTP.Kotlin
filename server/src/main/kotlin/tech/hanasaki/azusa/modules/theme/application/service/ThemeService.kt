package tech.hanasaki.azusa.modules.theme.application.service

import tech.hanasaki.azusa.common.kernel.exception.AuthorizationException
import tech.hanasaki.azusa.common.kernel.exception.NotFoundException
import tech.hanasaki.azusa.common.kernel.model.ThemeId
import tech.hanasaki.azusa.common.kernel.model.UserId
import tech.hanasaki.azusa.modules.theme.application.command.CreateThemeCommand
import tech.hanasaki.azusa.modules.theme.application.command.UpdateThemeCommand
import tech.hanasaki.azusa.modules.theme.domain.model.Theme
import tech.hanasaki.azusa.modules.theme.domain.repository.ThemeRepository
import java.util.*
import kotlin.time.Clock

class ThemeService(
    private val themeRepository: ThemeRepository,
) {
    suspend fun listThemes(authorId: UserId): List<Theme> {
        return themeRepository.findByAuthorId(authorId)
    }

    suspend fun getTheme(authorId: UserId, themeId: ThemeId): Theme {
        val theme = themeRepository.findByThemeId(themeId)
            ?: throw NotFoundException("Theme not found")
        if (theme.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        return theme
    }

    suspend fun createTheme(authorId: UserId, cmd: CreateThemeCommand): Theme {
        val now = Clock.System.now()
        val theme = Theme(
            id = cmd.id ?: ThemeId(UUID.randomUUID()),
            authorId = authorId,
            name = cmd.name,
            description = cmd.description,
            previewUrl = cmd.previewUrl,
            data = cmd.data,
            downloadCount = 0,
            version = cmd.version,
            createdAt = now,
            updatedAt = now,
        )
        themeRepository.save(theme)
        return theme
    }

    suspend fun updateTheme(authorId: UserId, themeId: ThemeId, cmd: UpdateThemeCommand): Theme {
        val existing = themeRepository.findByThemeId(themeId)
            ?: throw NotFoundException("Theme not found")
        if (existing.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        val updated = existing.copy(
            name = cmd.name,
            description = cmd.description,
            previewUrl = cmd.previewUrl,
            data = cmd.data,
            version = cmd.version,
            updatedAt = Clock.System.now(),
        )
        themeRepository.save(updated)
        return updated
    }

    suspend fun deleteTheme(authorId: UserId, themeId: ThemeId) {
        val existing = themeRepository.findByThemeId(themeId)
            ?: throw NotFoundException("Theme not found")
        if (existing.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        themeRepository.deleteByThemeId(themeId)
    }
}
