package tech.hanasaki.azusa.modules.notification.domain.model

import kotlinx.serialization.Contextual
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * 通知日志 ID
 */

@JvmInline
value class NotificationLogId(@Contextual val value: Uuid)

/**
 * 通知状态
 */
enum class NotificationStatus {
    PENDING,    // 待发送
    SENT,       // 已发送
    FAILED,     // 发送失败
    DELIVERED,  // 已送达
}

/**
 * 通知日志实体 - 记录所有通知的发送记录
 */
data class NotificationLog(
    val id: NotificationLogId,
    val userId: UserId?,             // 接收用户（可选，匿名通知无用户）
    val channel: NotificationChannel,
    val recipient: String,           // 接收地址（邮箱/手机号/设备ID）
    val subject: String?,            // 主题（邮件有，短信无）
    val content: String,             // 通知内容
    val templateId: NotificationTemplateId?, // 使用的模板（可选）
    val status: NotificationStatus,
    val errorMessage: String?,       // 失败时的错误信息
    val sentAt: Instant?,            // 发送时间
    val createdAt: Instant,
) {
    companion object {
        /**
         * 创建待发送的通知日志
         */
        fun create(
            userId: UserId?,
            channel: NotificationChannel,
            recipient: String,
            subject: String?,
            content: String,
            templateId: NotificationTemplateId? = null,
        ): NotificationLog = NotificationLog(
            id = NotificationLogId(Uuid.random()),
            userId = userId,
            channel = channel,
            recipient = recipient,
            subject = subject,
            content = content,
            templateId = templateId,
            status = NotificationStatus.PENDING,
            errorMessage = null,
            sentAt = null,
            createdAt = Clock.System.now(),
        )

        /**
         * 从持久化层重建
         */
        fun reconstitute(
            id: NotificationLogId,
            userId: UserId?,
            channel: NotificationChannel,
            recipient: String,
            subject: String?,
            content: String,
            templateId: NotificationTemplateId?,
            status: NotificationStatus,
            errorMessage: String?,
            sentAt: Instant?,
            createdAt: Instant,
        ): NotificationLog = NotificationLog(
            id = id,
            userId = userId,
            channel = channel,
            recipient = recipient,
            subject = subject,
            content = content,
            templateId = templateId,
            status = status,
            errorMessage = errorMessage,
            sentAt = sentAt,
            createdAt = createdAt,
        )
    }

    /**
     * 标记为已发送
     */
    fun markAsSent(): NotificationLog = copy(
        status = NotificationStatus.SENT,
        sentAt = Clock.System.now(),
    )

    /**
     * 标记为失败
     */
    fun markAsFailed(error: String): NotificationLog = copy(
        status = NotificationStatus.FAILED,
        errorMessage = error,
    )
}
