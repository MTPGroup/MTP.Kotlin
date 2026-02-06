package tech.hanasaki.azusa.modules.notification.application.service

import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationChannel
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationLog
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationTemplateId
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationTemplateType
import tech.hanasaki.azusa.modules.notification.domain.port.EmailSender
import tech.hanasaki.azusa.modules.notification.domain.port.PushSender
import tech.hanasaki.azusa.modules.notification.domain.port.SmsSender
import tech.hanasaki.azusa.modules.notification.domain.repository.NotificationLogRepository
import tech.hanasaki.azusa.modules.notification.domain.repository.NotificationTemplateRepository

/**
 * 通知服务 - 统一管理所有通知渠道
 */
class NotificationService(
    private val emailSender: EmailSender,
    private val smsSender: SmsSender?,
    private val pushSender: PushSender?,
    private val logRepository: NotificationLogRepository,
    private val templateRepository: NotificationTemplateRepository,
) {
    /**
     * 发送邮件通知
     */
    suspend fun sendEmail(
        to: String,
        subject: String,
        content: String,
        userId: UserId? = null,
        templateId: NotificationTemplateId? = null,
    ) {
        val log = NotificationLog.create(
            userId = userId,
            channel = NotificationChannel.EMAIL,
            recipient = to,
            subject = subject,
            content = content,
            templateId = templateId,
        )

        try {
            emailSender.sendHtml(to, subject, content)
            logRepository.save(log.markAsSent())
        } catch (e: Exception) {
            logRepository.save(log.markAsFailed(e.message ?: "Unknown error"))
            throw e
        }
    }

    /**
     * 发送短信通知
     */
    suspend fun sendSms(
        to: String,
        content: String,
        userId: UserId? = null,
        templateId: NotificationTemplateId? = null,
    ) {
        val sender = smsSender ?: throw UnsupportedOperationException("SMS sender not configured")

        val log = NotificationLog.create(
            userId = userId,
            channel = NotificationChannel.SMS,
            recipient = to,
            subject = null,
            content = content,
            templateId = templateId,
        )

        try {
            sender.send(to, content)
            logRepository.save(log.markAsSent())
        } catch (e: Exception) {
            logRepository.save(log.markAsFailed(e.message ?: "Unknown error"))
            throw e
        }
    }

    /**
     * 发送推送通知
     */
    suspend fun sendPush(
        deviceToken: String,
        title: String,
        body: String,
        userId: UserId? = null,
        templateId: NotificationTemplateId? = null,
        data: Map<String, String>? = null,
    ) {
        val sender = pushSender ?: throw UnsupportedOperationException("Push sender not configured")

        val log = NotificationLog.create(
            userId = userId,
            channel = NotificationChannel.PUSH,
            recipient = deviceToken,
            subject = title,
            content = body,
            templateId = templateId,
        )

        try {
            sender.send(deviceToken, title, body, data)
            logRepository.save(log.markAsSent())
        } catch (e: Exception) {
            logRepository.save(log.markAsFailed(e.message ?: "Unknown error"))
            throw e
        }
    }

    /**
     * 使用模板发送通知
     */
    suspend fun sendWithTemplate(
        templateType: NotificationTemplateType,
        channel: NotificationChannel,
        recipient: String,
        variables: Map<String, String>,
        userId: UserId? = null,
    ) {
        val template = templateRepository.findActiveByTypeAndChannel(templateType, channel)
            ?: throw IllegalArgumentException("Template not found for type $templateType and channel $channel")

        val rendered = template.render(variables)

        when (channel) {
            NotificationChannel.EMAIL -> sendEmail(
                to = recipient,
                subject = rendered.subject ?: "",
                content = rendered.content,
                userId = userId,
                templateId = template.id,
            )

            NotificationChannel.SMS -> sendSms(
                to = recipient,
                content = rendered.content,
                userId = userId,
                templateId = template.id,
            )

            NotificationChannel.PUSH -> sendPush(
                deviceToken = recipient,
                title = rendered.subject ?: "",
                body = rendered.content,
                userId = userId,
                templateId = template.id,
            )

            NotificationChannel.WEBSOCKET -> {
                // WebSocket 通知由其他服务处理
                throw UnsupportedOperationException("WebSocket notifications should be handled separately")
            }
        }
    }

    /**
     * 发送 OTP 验证码邮件
     */
    suspend fun sendOtpEmail(
        to: String,
        code: String,
        subject: String,
        title: String,
        description: String,
    ) {
        // 使用默认模板
        emailSender.sendTemplate(
            to = to,
            subject = subject,
            templateName = "otp-verification.ftl",
            model = mapOf(
                "title" to title,
                "description" to description,
                "code" to code,
                "expireMinutes" to 5,
            ),
        )
    }
}
