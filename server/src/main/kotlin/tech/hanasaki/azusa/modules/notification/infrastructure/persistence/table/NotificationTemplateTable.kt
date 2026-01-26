package tech.hanasaki.azusa.modules.notification.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp


/**
 * 通知模板表
 */
object NotificationTemplateTable : Table("notification_templates") {
    val id = uuid("id")
    val type = varchar("type", 50)
    val channel = varchar("channel", 20)
    val name = varchar("name", 100)
    val subject = varchar("subject", 500).nullable()
    val content = text("content")
    val isActive = bool("is_active")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
