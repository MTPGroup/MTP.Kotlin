package tech.hanasaki.azusa.modules.notification.domain.model

import kotlinx.serialization.Contextual
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 通知模板 ID
 */
@JvmInline
value class NotificationTemplateId(@Contextual val value: UUID)

/**
 * 通知模板类型
 */
enum class NotificationTemplateType {
    OTP_VERIFICATION,    // OTP 验证码
    WELCOME,             // 欢迎邮件
    PASSWORD_RESET,      // 密码重置
    ACCOUNT_DELETION,    // 账号删除确认
    SYSTEM_ALERT,        // 系统告警
}

/**
 * 通知模板实体 - 管理通知内容模板
 */
data class NotificationTemplate(
    val id: NotificationTemplateId,
    val type: NotificationTemplateType,
    val channel: NotificationChannel,
    val name: String,                // 模板名称
    val subject: String?,            // 邮件主题模板（支持变量替换）
    val content: String,             // 内容模板（支持变量替换）
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        /**
         * 创建新模板
         */
        fun create(
            type: NotificationTemplateType,
            channel: NotificationChannel,
            name: String,
            subject: String?,
            content: String,
        ): NotificationTemplate {
            val now = Clock.System.now()
            return NotificationTemplate(
                id = NotificationTemplateId(UUID.randomUUID()),
                type = type,
                channel = channel,
                name = name,
                subject = subject,
                content = content,
                isActive = true,
                createdAt = now,
                updatedAt = now,
            )
        }

        /**
         * 从持久化层重建
         */
        fun reconstitute(
            id: NotificationTemplateId,
            type: NotificationTemplateType,
            channel: NotificationChannel,
            name: String,
            subject: String?,
            content: String,
            isActive: Boolean,
            createdAt: Instant,
            updatedAt: Instant,
        ): NotificationTemplate = NotificationTemplate(
            id = id,
            type = type,
            channel = channel,
            name = name,
            subject = subject,
            content = content,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    /**
     * 渲染模板内容
     * 将模板中的变量占位符替换为实际值
     *
     * @param variables 变量映射，如 mapOf("code" to "123456", "name" to "张三")
     */
    fun render(variables: Map<String, String>): RenderedNotification {
        var renderedSubject = subject
        var renderedContent = content

        variables.forEach { (key, value) ->
            renderedSubject = renderedSubject?.replace("{{$key}}", value)
            renderedContent = renderedContent.replace("{{$key}}", value)
        }

        return RenderedNotification(
            subject = renderedSubject,
            content = renderedContent,
        )
    }
}

/**
 * 渲染后的通知内容
 */
data class RenderedNotification(
    val subject: String?,
    val content: String,
)
