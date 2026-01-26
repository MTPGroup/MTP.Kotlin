package tech.hanasaki.azusa.modules.notification.infrastructure.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationChannel
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationTemplate
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationTemplateId
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationTemplateType
import tech.hanasaki.azusa.modules.notification.infrastructure.persistence.table.NotificationTemplateTable

/**
 * 通知模板映射器
 */
object NotificationTemplateMapper {

    fun toDomain(row: ResultRow): NotificationTemplate = NotificationTemplate.reconstitute(
        id = NotificationTemplateId(row[NotificationTemplateTable.id]),
        type = NotificationTemplateType.valueOf(row[NotificationTemplateTable.type]),
        channel = NotificationChannel.valueOf(row[NotificationTemplateTable.channel]),
        name = row[NotificationTemplateTable.name],
        subject = row[NotificationTemplateTable.subject],
        content = row[NotificationTemplateTable.content],
        isActive = row[NotificationTemplateTable.isActive],
        createdAt = row[NotificationTemplateTable.createdAt],
        updatedAt = row[NotificationTemplateTable.updatedAt],
    )

    fun toEntity(domain: NotificationTemplate, target: UpdateBuilder<*>) {
        target[NotificationTemplateTable.id] = domain.id.value
        target[NotificationTemplateTable.type] = domain.type.name
        target[NotificationTemplateTable.channel] = domain.channel.name
        target[NotificationTemplateTable.name] = domain.name
        target[NotificationTemplateTable.subject] = domain.subject
        target[NotificationTemplateTable.content] = domain.content
        target[NotificationTemplateTable.isActive] = domain.isActive
        target[NotificationTemplateTable.createdAt] = domain.createdAt
        target[NotificationTemplateTable.updatedAt] = domain.updatedAt
    }
}
