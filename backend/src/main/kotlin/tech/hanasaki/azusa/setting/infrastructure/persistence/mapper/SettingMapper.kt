package tech.hanasaki.azusa.setting.infrastructure.persistence.mapper

import org.springframework.stereotype.Component
import tech.hanasaki.azusa.setting.domain.model.*
import tech.hanasaki.azusa.setting.infrastructure.persistence.entity.LLMConfigEntity
import tech.hanasaki.azusa.setting.infrastructure.persistence.entity.SettingEntity
import tech.hanasaki.azusa.shared.ThemeId
import tech.hanasaki.azusa.shared.UserId
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Component
class SettingMapper {

    fun toDomain(entity: SettingEntity): Setting {
        return Setting(
            uid = UserId(entity.id),
            theme = AppTheme.valueOf(entity.theme),
            activeThemeId = entity.activeThemeId?.let { ThemeId(it) },
            activeLlmConfigId = LLMConfigId(entity.activeLlmConfigId),
            llmConfigs = entity.llmConfigs.map { toLLMConfigDomain(it) }.toSet(),
            createdAt = entity.createdAt.toKotlinInstant(),
            updatedAt = entity.updatedAt.toKotlinInstant()
        )
    }

    fun toEntity(domain: Setting, isNewRecord: Boolean = false): SettingEntity {
        return SettingEntity(
            uid = domain.uid.value,
            theme = domain.theme.name,
            llmConfigs = domain.llmConfigs.map { toLLMConfigEntity(it, isNewRecord) }.toSet(),
            activeThemeId = domain.activeThemeId?.value,
            activeLlmConfigId = domain.activeLlmConfigId.value,
            createdAt = domain.createdAt.toJavaInstant(),
            updatedAt = domain.updatedAt.toJavaInstant(),
        ).apply {
            this.isNewRecord = isNewRecord
        }
    }

    private fun toLLMConfigDomain(entity: LLMConfigEntity): LLMConfig {
        return LLMConfig(
            id = LLMConfigId(entity.id),
            provider = LLMProvider.valueOf(entity.provider),
            baseUrl = entity.baseUrl,
            apiKey = entity.apiKey,
            model = entity.model,
            temperature = entity.temperature,
            maxTokens = entity.maxTokens,
            runOnClient = entity.runOnClient
        )
    }

    private fun toLLMConfigEntity(domain: LLMConfig, isNewRecord: Boolean): LLMConfigEntity {
        return LLMConfigEntity(
            id = domain.id.value,
            provider = domain.provider.name,
            baseUrl = domain.baseUrl,
            apiKey = domain.apiKey,
            model = domain.model,
            temperature = domain.temperature,
            maxTokens = domain.maxTokens,
            runOnClient = domain.runOnClient
        ).apply {
            this.isNewRecord = isNewRecord
        }
    }
}