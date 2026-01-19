package tech.hanasaki.azusa.setting.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import tech.hanasaki.azusa.setting.api.dto.UpdateSettingRequest
import tech.hanasaki.azusa.setting.application.command.GetSettingCommand
import tech.hanasaki.azusa.setting.application.service.SettingService
import tech.hanasaki.azusa.setting.domain.model.Setting
import tech.hanasaki.azusa.shared.ApiException
import tech.hanasaki.azusa.shared.UserId
import java.util.*

@RestController
@RequestMapping("/settings")
class SettingController(
    private val settingService: SettingService,
) {

    @GetMapping
    fun getSetting(authentication: Authentication): ResponseEntity<Setting> {
        val userId = requireUserId(authentication)
        val setting = settingService.getSetting(GetSettingCommand(userId))
        return ResponseEntity.ok(setting)
    }

    @PutMapping
    fun updateSetting(
        authentication: Authentication,
        @RequestBody request: UpdateSettingRequest,
    ): ResponseEntity<Setting> {
        val userId = requireUserId(authentication)
        val updatedSetting = settingService.updateSetting(userId, request.toCommand())
        return ResponseEntity.ok(updatedSetting)
    }

    private fun requireUserId(authentication: Authentication): UserId {
        val subject = authentication.principal as? String
            ?: throw ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing authentication")
        return UserId(UUID.fromString(subject))
    }
}