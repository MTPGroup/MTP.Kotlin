package tech.hanasaki.azusa.modules.notification.adapter.out.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp


/**
 * 通知日志表
 */

object NotificationLogTable : Table("notification_logs") {
    val id = uuid("id")
    val userId = uuid("user_id").nullable()
    val channel = varchar("channel", 20)
    val recipient = varchar("recipient", 255)
    val subject = varchar("subject", 500).nullable()
    val content = text("content")
    val status = varchar("status", 20)
    val errorMessage = text("error_message").nullable()
    val sentAt = timestamp("sent_at").nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
