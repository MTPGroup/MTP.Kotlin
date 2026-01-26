package tech.hanasaki.azusa.modules.notification

import io.ktor.server.config.*
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.modules.notification.application.service.NotificationService
import tech.hanasaki.azusa.modules.notification.domain.port.EmailSender
import tech.hanasaki.azusa.modules.notification.domain.repository.NotificationLogRepository
import tech.hanasaki.azusa.modules.notification.domain.repository.NotificationTemplateRepository
import tech.hanasaki.azusa.modules.notification.infrastructure.adapter.SmtpConfig
import tech.hanasaki.azusa.modules.notification.infrastructure.adapter.SmtpEmailSender
import tech.hanasaki.azusa.modules.notification.infrastructure.persistence.repository.ExposedNotificationLogRepository
import tech.hanasaki.azusa.modules.notification.infrastructure.persistence.repository.ExposedNotificationTemplateRepository

private val logger = LoggerFactory.getLogger("NotificationModule")

/**
 * 通知模块配置
 */
data class NotificationConfig(
    val smtp: SmtpConfig,
)

/**
 * 从应用配置读取通知模块配置
 */
fun ApplicationConfig.toNotificationConfig(): NotificationConfig {
    val smtp = config("smtp")
    return NotificationConfig(
        smtp = SmtpConfig(
            enabled = smtp.property("enabled").getString().toBoolean(),
            host = smtp.property("host").getString(),
            port = smtp.property("port").getString().toInt(),
            username = smtp.property("username").getString(),
            password = smtp.property("password").getString(),
            from = smtp.property("from").getString(),
            tls = smtp.propertyOrNull("tls")?.getString()?.toBoolean() ?: true,
        ),
    )
}

/**
 * 通知模块 Koin 定义
 */
fun notificationModule(config: ApplicationConfig) = module {
    val notificationConfig = config.toNotificationConfig()

    // Repositories
    single<NotificationLogRepository> { ExposedNotificationLogRepository() }
    single<NotificationTemplateRepository> { ExposedNotificationTemplateRepository() }

    // Ports / Adapters
    single<EmailSender> { SmtpEmailSender(notificationConfig.smtp) }

    // Application Service
    single {
        NotificationService(
            emailSender = get(),
            smsSender = null,   // TODO: Implement SmsSender when needed
            pushSender = null,  // TODO: Implement PushSender when needed
            logRepository = get(),
            templateRepository = get(),
        )
    }

    // 处理 OTP 生成事件 - 发送验证码邮件
    /*onIntegrationEvent<OtpGeneratedEvent> { event ->
        val notificationService = get<NotificationService>()
        try {
            val subject = when (event.otpType) {
                OtpType.VERIFY_EMAIL -> "Verify your email - Azusa"
                OtpType.RESET_PASSWORD -> "Reset your password - Azusa"
                OtpType.SIGN_IN -> "Sign in code - Azusa"
            }

            val html = renderOtpEmailHtml(event.otpType, event.code, event.expiresInMinutes)

            notificationService.sendEmail(
                to = event.email.value,
                subject = subject,
                content = html,
            )

            logger.info("OTP email sent to: ${event.email.value}")
        } catch (e: Exception) {
            logger.error("Failed to send OTP email to ${event.email.value}: ${e.message}", e)
        }
    }*/
}

/**
 * 渲染 OTP 邮件 HTML
 */
private fun renderOtpEmailHtml(type: OtpType, code: String, expiresInMinutes: Int): String {
    val title = when (type) {
        OtpType.VERIFY_EMAIL -> "Email Verification"
        OtpType.RESET_PASSWORD -> "Password Reset"
        OtpType.SIGN_IN -> "Sign In Code"
    }

    val description = when (type) {
        OtpType.VERIFY_EMAIL -> "Please use the following code to verify your email address:"
        OtpType.RESET_PASSWORD -> "Please use the following code to reset your password:"
        OtpType.SIGN_IN -> "Please use the following code to sign in:"
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>$title</title>
        </head>
        <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
            <h2>$title</h2>
            <p>$description</p>
            <div style="background-color: #f5f5f5; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; margin: 20px 0;">
                $code
            </div>
            <p>This code will expire in $expiresInMinutes minutes.</p>
            <p style="color: #666; font-size: 12px;">If you didn't request this code, please ignore this email.</p>
        </body>
        </html>
    """.trimIndent()
}
