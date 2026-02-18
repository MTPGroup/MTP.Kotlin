package tech.hanasaki.azusa.modules.setting.adapter.out.persistence.repository

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.setting.adapter.out.persistence.mapper.LLMConfigMapper
import tech.hanasaki.azusa.modules.setting.adapter.out.persistence.mapper.SettingMapper
import tech.hanasaki.azusa.modules.setting.adapter.out.persistence.table.LlmConfigsTable
import tech.hanasaki.azusa.modules.setting.adapter.out.persistence.table.SettingsTable
import tech.hanasaki.azusa.modules.setting.domain.model.Setting
import tech.hanasaki.azusa.modules.setting.domain.port.SettingRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

class ExposedSettingRepository : SettingRepositoryPort {
    override suspend fun findByUserId(userId: UserId): Setting? {
        val settingRow = SettingsTable.selectAll()
            .where { SettingsTable.id eq userId.value }
            .singleOrNull() ?: return null
        val llmRows = LlmConfigsTable.selectAll()
            .where { LlmConfigsTable.settingId eq userId.value }.toList()
        return SettingMapper.toDomain(settingRow, llmRows)
    }

    override suspend fun save(setting: Setting) {
        val updatedRows = SettingsTable.update({ SettingsTable.id eq setting.uid.value }) {
            SettingMapper.toEntity(setting, it)
            it[updatedAt] = setting.updatedAt
        }
        if (updatedRows == 0) {
            SettingsTable.insert {
                it[id] = setting.uid.value
                SettingMapper.toEntity(setting, it)
                it[createdAt] = setting.createdAt
                it[updatedAt] = setting.updatedAt
            }
        }

        // Delete LLMConfigs that are no longer in the domain model
        val domainConfigIds = setting.llmConfigs.map { it.id.value }.toSet()
        val existingConfigIds = LlmConfigsTable.selectAll()
            .where { LlmConfigsTable.settingId eq setting.uid.value }
            .map { it[LlmConfigsTable.id] }
            .toSet()
        val toDelete = existingConfigIds - domainConfigIds
        if (toDelete.isNotEmpty()) {
            for (configId in toDelete) {
                LlmConfigsTable.deleteWhere { LlmConfigsTable.id eq configId }
            }
        }

        // Upsert remaining configs
        setting.llmConfigs.forEach { config ->
            val configUpdated = LlmConfigsTable.update({ LlmConfigsTable.id eq config.id.value }) {
                it[settingId] = setting.uid.value
                LLMConfigMapper.toEntity(config, it)
            }
            if (configUpdated == 0) {
                LlmConfigsTable.insert {
                    it[id] = config.id.value
                    it[settingId] = setting.uid.value
                    LLMConfigMapper.toEntity(config, it)
                }
            }
        }
    }
}
