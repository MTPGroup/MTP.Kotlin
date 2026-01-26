package tech.hanasaki.azusa.modules.notification.infrastructure.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.notification.domain.model.*
import tech.hanasaki.azusa.modules.notification.infrastructure.persistence.table.NotificationLogTable
import tech.hanasaki.azusa.shared.domain.model.UserId

/**
 * 通知日志映射器
 */
object NotificationLogMapper {

    fun toDomain(row: ResultRow): NotificationLog = NotificationLog.reconstitute(
        id = NotificationLogId(row[NotificationLogTable.id]),
        userId = row[NotificationLogTable.userId]?.let { UserId(it) },
        channel = NotificationChannel.valueOf(row[NotificationLogTable.channel]),
        recipient = row[NotificationLogTable.recipient],
        subject = row[NotificationLogTable.subject],
        content = row[NotificationLogTable.content],
        templateId = row[NotificationLogTable.templateId]?.let { NotificationTemplateId(it) },
        status = NotificationStatus.valueOf(row[NotificationLogTable.status]),
        errorMessage = row[NotificationLogTable.errorMessage],
        sentAt = row[NotificationLogTable.sentAt],
        createdAt = row[NotificationLogTable.createdAt],
    )

    fun toEntity(domain: NotificationLog, target: UpdateBuilder<*>) {
        target[NotificationLogTable.id] = domain.id.value
        target[NotificationLogTable.userId] = domain.userId?.value
        target[NotificationLogTable.channel] = domain.channel.name
        target[NotificationLogTable.recipient] = domain.recipient
        target[NotificationLogTable.subject] = domain.subject
        target[NotificationLogTable.content] = domain.content
        target[NotificationLogTable.templateId] = domain.templateId?.value
        target[NotificationLogTable.status] = domain.status.name
        target[NotificationLogTable.errorMessage] = domain.errorMessage
        target[NotificationLogTable.sentAt] = domain.sentAt
        target[NotificationLogTable.createdAt] = domain.createdAt
    }
}
