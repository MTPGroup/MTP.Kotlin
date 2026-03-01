package tech.hanasaki.azusa.modules.notification.application.service

import tech.hanasaki.azusa.modules.notification.application.port.`in`.NotificationUseCasePort
import tech.hanasaki.azusa.modules.notification.application.port.out.EmailSenderPort
import tech.hanasaki.azusa.modules.notification.application.port.out.PushSenderPort
import tech.hanasaki.azusa.modules.notification.application.port.out.SmsSenderPort
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationChannel
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationLog
import tech.hanasaki.azusa.modules.notification.domain.port.NotificationLogRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.InternalServerException
import tech.hanasaki.azusa.shared.domain.exception.ValidationException
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class NotificationService(
    private val emailSender: EmailSenderPort,
    private val smsSender: SmsSenderPort?,
    private val pushSender: PushSenderPort?,
    private val logRepository: NotificationLogRepositoryPort,
    private val tx: TransactionalPort,
) : NotificationUseCasePort {
    override suspend fun sendEmail(
        to: String,
        subject: String,
        content: String?,
        templateName: String?,
        model: Map<String, Any>,
        userId: UserId?,
    ) {
        if (content == null && templateName == null) {
            throw ValidationException("content 或 templateName 至少提供一个")
        }

        val log = NotificationLog.create(
            userId = userId,
            channel = NotificationChannel.EMAIL,
            recipient = to,
            subject = subject,
            content = content ?: "template:$templateName",
        )

        tx.execute {
            try {
                if (templateName != null) {
                    emailSender.sendTemplate(to, subject, templateName, model)
                } else {
                    emailSender.sendHtml(to, subject, content!!)
                }
                logRepository.save(log.markAsSent())
            } catch (e: Exception) {
                logRepository.save(log.markAsFailed(e.message ?: "未知错误"))
                throw e
            }
        }
    }

    override suspend fun sendSms(
        to: String,
        content: String,
        userId: UserId?,
    ) {
        val sender = smsSender ?: throw InternalServerException("SMS发送器未配置")

        val log = NotificationLog.create(
            userId = userId,
            channel = NotificationChannel.SMS,
            recipient = to,
            subject = null,
            content = content,
        )

        tx.execute {
            try {
                sender.send(to, content)
                logRepository.save(log.markAsSent())
            } catch (e: Exception) {
                logRepository.save(log.markAsFailed(e.message ?: "未知错误"))
                throw e
            }
        }
    }

    override suspend fun sendPush(
        deviceToken: String,
        title: String,
        body: String,
        userId: UserId?,
        data: Map<String, String>?,
    ) {
        val sender = pushSender ?: throw InternalServerException("推送器未配置")

        val log = NotificationLog.create(
            userId = userId,
            channel = NotificationChannel.PUSH,
            recipient = deviceToken,
            subject = title,
            content = body,
        )

        tx.execute {
            try {
                sender.send(deviceToken, title, body, data)
                logRepository.save(log.markAsSent())
            } catch (e: Exception) {
                logRepository.save(log.markAsFailed(e.message ?: "未知的错误"))
                throw e
            }
        }
    }
}
