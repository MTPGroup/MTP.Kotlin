package tech.hanasaki.azusa.modules.setting.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.common.adapter.out.persistence.dbQuery
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.setting.domain.model.Setting
import tech.hanasaki.azusa.modules.setting.domain.repository.SettingRepository
import tech.hanasaki.azusa.modules.setting.infrastructure.persistence.mapper.LLMConfigMapper
import tech.hanasaki.azusa.modules.setting.infrastructure.persistence.mapper.SettingMapper
import tech.hanasaki.azusa.modules.setting.infrastructure.persistence.table.LlmConfigsTable
import tech.hanasaki.azusa.modules.setting.infrastructure.persistence.table.SettingsTable

class ExposedSettingRepository : SettingRepository {
    override suspend fun findByUserId(userId: UserId): Setting? = dbQuery {
        val settingRow = SettingsTable.selectAll()
            .where { SettingsTable.id eq userId.value }
            .singleOrNull() ?: return@dbQuery null
        val llmRows = LlmConfigsTable.selectAll()
            .where { LlmConfigsTable.settingId eq userId.value }.toList()
        SettingMapper.toDomain(settingRow, llmRows)
    }

    override suspend fun save(setting: Setting): Unit = dbQuery {
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

        setting.llmConfigs.forEach { config ->
            val updatedRows = LlmConfigsTable.update({ LlmConfigsTable.id eq config.id.value }) {
                it[settingId] = setting.uid.value
                LLMConfigMapper.toEntity(config, it)
            }
            if (updatedRows == 0) {
                LlmConfigsTable.insert {
                    it[id] = config.id.value
                    it[settingId] = setting.uid.value
                    LLMConfigMapper.toEntity(config, it)
                }
            }
        }
    }
}
