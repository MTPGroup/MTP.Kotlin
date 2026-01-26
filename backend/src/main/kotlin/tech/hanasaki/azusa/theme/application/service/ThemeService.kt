package tech.hanasaki.azusa.theme.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.hanasaki.azusa.common.AuthorizationException
import tech.hanasaki.azusa.common.NotFoundException
import tech.hanasaki.azusa.common.ThemeId
import tech.hanasaki.azusa.common.UserId
import tech.hanasaki.azusa.theme.application.command.CreateThemeCommand
import tech.hanasaki.azusa.theme.application.command.UpdateThemeCommand
import tech.hanasaki.azusa.theme.domain.model.Theme
import tech.hanasaki.azusa.theme.domain.repository.ThemeRepository
import java.util.UUID
import kotlin.time.Clock

@Service
class ThemeService(
    private val themeRepository: ThemeRepository,
) {
    @Transactional
    fun listThemes(authorId: UserId): List<Theme> {
        return themeRepository.findByAuthorId(authorId)
    }

    @Transactional
    fun getTheme(authorId: UserId, themeId: ThemeId): Theme {
        val theme = themeRepository.findByThemeId(themeId)
            ?: throw NotFoundException("Theme not found")
        if (theme.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        return theme
    }

    @Transactional
    fun createTheme(authorId: UserId, cmd: CreateThemeCommand): Theme {
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

    @Transactional
    fun updateTheme(authorId: UserId, themeId: ThemeId, cmd: UpdateThemeCommand): Theme {
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

    @Transactional
    fun deleteTheme(authorId: UserId, themeId: ThemeId) {
        val existing = themeRepository.findByThemeId(themeId)
            ?: throw NotFoundException("Theme not found")
        if (existing.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        themeRepository.deleteByThemeId(themeId)
    }
}
