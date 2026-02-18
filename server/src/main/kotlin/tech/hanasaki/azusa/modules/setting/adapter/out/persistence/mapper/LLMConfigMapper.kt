package tech.hanasaki.azusa.modules.setting.adapter.out.persistence.mapper

import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.setting.adapter.out.persistence.table.LlmConfigsTable.apiKey
import tech.hanasaki.azusa.modules.setting.adapter.out.persistence.table.LlmConfigsTable.baseUrl
import tech.hanasaki.azusa.modules.setting.adapter.out.persistence.table.LlmConfigsTable.maxTokens
import tech.hanasaki.azusa.modules.setting.adapter.out.persistence.table.LlmConfigsTable.model
import tech.hanasaki.azusa.modules.setting.adapter.out.persistence.table.LlmConfigsTable.provider
import tech.hanasaki.azusa.modules.setting.adapter.out.persistence.table.LlmConfigsTable.runOnClient
import tech.hanasaki.azusa.modules.setting.adapter.out.persistence.table.LlmConfigsTable.temperature
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig

object LLMConfigMapper {
    fun toEntity(domain: LLMConfig, target: UpdateBuilder<*>) {
        target[provider] = domain.provider.name
        target[baseUrl] = domain.baseUrl
        target[apiKey] = domain.apiKey
        target[model] = domain.model
        target[temperature] = domain.temperature
        target[maxTokens] = domain.maxTokens
        target[runOnClient] = domain.runOnClient
    }
}