package tech.hanasaki.azusa.theme.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.hanasaki.azusa.shared.ApiException
import tech.hanasaki.azusa.shared.ThemeId
import tech.hanasaki.azusa.shared.UserId
import tech.hanasaki.azusa.theme.api.dto.CreateThemeRequest
import tech.hanasaki.azusa.theme.api.dto.UpdateThemeRequest
import tech.hanasaki.azusa.theme.application.service.ThemeService
import tech.hanasaki.azusa.theme.domain.model.Theme
import java.util.UUID

@RestController
@RequestMapping("/themes")
class ThemeController(
    private val themeService: ThemeService,
) {

    @GetMapping
    fun listThemes(authentication: Authentication): ResponseEntity<List<Theme>> {
        val userId = requireUserId(authentication)
        val themes = themeService.listThemes(userId)
        return ResponseEntity.ok(themes)
    }

    @GetMapping("/{themeId}")
    fun getTheme(
        authentication: Authentication,
        @PathVariable themeId: UUID,
    ): ResponseEntity<Theme> {
        val userId = requireUserId(authentication)
        val theme = themeService.getTheme(userId, ThemeId(themeId))
        return ResponseEntity.ok(theme)
    }

    @PostMapping
    fun createTheme(
        authentication: Authentication,
        @RequestBody request: CreateThemeRequest,
    ): ResponseEntity<Theme> {
        val userId = requireUserId(authentication)
        val theme = themeService.createTheme(userId, request.toCommand())
        return ResponseEntity.ok(theme)
    }

    @PutMapping("/{themeId}")
    fun updateTheme(
        authentication: Authentication,
        @PathVariable themeId: UUID,
        @RequestBody request: UpdateThemeRequest,
    ): ResponseEntity<Theme> {
        val userId = requireUserId(authentication)
        val theme = themeService.updateTheme(userId, ThemeId(themeId), request.toCommand())
        return ResponseEntity.ok(theme)
    }

    @DeleteMapping("/{themeId}")
    fun deleteTheme(
        authentication: Authentication,
        @PathVariable themeId: UUID,
    ): ResponseEntity<Void> {
        val userId = requireUserId(authentication)
        themeService.deleteTheme(userId, ThemeId(themeId))
        return ResponseEntity.noContent().build()
    }

    private fun requireUserId(authentication: Authentication): UserId {
        val subject = authentication.principal as? String
            ?: throw ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing authentication")
        val userId = runCatching { UUID.fromString(subject) }.getOrNull()
            ?: throw ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid subject")
        return UserId(userId)
    }
}
