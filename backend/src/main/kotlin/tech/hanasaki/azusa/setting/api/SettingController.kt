package tech.hanasaki.azusa.setting.api

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
import tech.hanasaki.azusa.setting.api.dto.CreateLLMConfigRequest
import tech.hanasaki.azusa.setting.api.dto.UpdateSettingRequest
import tech.hanasaki.azusa.setting.api.dto.UpdateLLMConfigRequest
import tech.hanasaki.azusa.setting.application.command.GetSettingCommand
import tech.hanasaki.azusa.setting.application.service.SettingService
import tech.hanasaki.azusa.setting.domain.model.LLMConfig
import tech.hanasaki.azusa.setting.domain.model.LLMConfigId
import tech.hanasaki.azusa.setting.domain.model.Setting
import tech.hanasaki.azusa.common.ApiException
import tech.hanasaki.azusa.common.UserId
import java.util.UUID

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

    @GetMapping("/llm-configs")
    fun listLlmConfigs(authentication: Authentication): ResponseEntity<Set<LLMConfig>> {
        val userId = requireUserId(authentication)
        val configs = settingService.listLlmConfigs(userId)
        return ResponseEntity.ok(configs)
    }

    @GetMapping("/llm-configs/{configId}")
    fun getLlmConfig(
        authentication: Authentication,
        @PathVariable configId: UUID,
    ): ResponseEntity<LLMConfig> {
        val userId = requireUserId(authentication)
        val config = settingService.getLlmConfig(userId, LLMConfigId(configId))
        return ResponseEntity.ok(config)
    }

    @PostMapping("/llm-configs")
    fun addLlmConfig(
        authentication: Authentication,
        @RequestBody request: CreateLLMConfigRequest,
    ): ResponseEntity<Setting> {
        val userId = requireUserId(authentication)
        val updatedSetting = settingService.addLlmConfig(userId, request.toDomain())
        return ResponseEntity.ok(updatedSetting)
    }

    @PutMapping("/llm-configs/{configId}")
    fun updateLlmConfig(
        authentication: Authentication,
        @PathVariable configId: UUID,
        @RequestBody request: UpdateLLMConfigRequest,
    ): ResponseEntity<Setting> {
        val userId = requireUserId(authentication)
        val updatedSetting = settingService.updateLlmConfig(userId, request.toDomain(LLMConfigId(configId)))
        return ResponseEntity.ok(updatedSetting)
    }

    @DeleteMapping("/llm-configs/{configId}")
    fun deleteLlmConfig(
        authentication: Authentication,
        @PathVariable configId: UUID,
    ): ResponseEntity<Setting> {
        val userId = requireUserId(authentication)
        val updatedSetting = settingService.deleteLlmConfig(userId, LLMConfigId(configId))
        return ResponseEntity.ok(updatedSetting)
    }

    @PostMapping("/llm-configs/{configId}/select")
    fun selectLlmConfig(
        authentication: Authentication,
        @PathVariable configId: UUID,
    ): ResponseEntity<Setting> {
        val userId = requireUserId(authentication)
        val updatedSetting = settingService.selectLlmConfig(userId, LLMConfigId(configId))
        return ResponseEntity.ok(updatedSetting)
    }

    private fun requireUserId(authentication: Authentication): UserId {
        val subject = authentication.principal as? String
            ?: throw ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing authentication")
        return UserId(UUID.fromString(subject))
    }
}
