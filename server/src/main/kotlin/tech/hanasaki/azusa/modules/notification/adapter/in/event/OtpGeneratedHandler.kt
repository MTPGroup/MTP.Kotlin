package tech.hanasaki.azusa.modules.notification.adapter.`in`.event

import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.modules.notification.application.port.`in`.NotificationUseCasePort
import tech.hanasaki.azusa.shared.domain.event.OtpGeneratedIntegrationEvent
import tech.hanasaki.azusa.shared.port.`in`.IntegrationEventHandlerPort

class OtpGeneratedHandler(
    private val notificationService: NotificationUseCasePort,
) : IntegrationEventHandlerPort<OtpGeneratedIntegrationEvent> {
    private val logger = LoggerFactory.getLogger(OtpGeneratedHandler::class.java)

    override suspend fun invoke(event: OtpGeneratedIntegrationEvent) {
        try {
            val subject = when (event.type) {
                "VERIFY_EMAIL" -> "验证您的邮箱 - Azusa"
                "RESET_PASSWORD" -> "重置您的密码 - Azusa"
                "SIGN_IN" -> "登录验证码 - Azusa"
                else -> "验证码 - Azusa"
            }

            val title = when (event.type) {
                "VERIFY_EMAIL" -> "验证您的邮箱"
                "RESET_PASSWORD" -> "重置密码"
                "SIGN_IN" -> "登录验证"
                else -> "验证码"
            }

            val description = when (event.type) {
                "VERIFY_EMAIL" -> "感谢您注册 Azusa！请使用以下验证码完成邮箱验证："
                "RESET_PASSWORD" -> "您正在重置密码，请使用以下验证码完成操作："
                "SIGN_IN" -> "您正在登录 Azusa，请使用以下验证码完成登录："
                else -> "请使用以下验证码："
            }

            notificationService.sendEmail(
                to = event.email,
                subject = subject,
                templateName = "otp-verification.ftl",
                model = mapOf(
                    "title" to title,
                    "description" to description,
                    "code" to event.code,
                    "expireMinutes" to 5,
                ),
            )

            logger.info("OTP email sent to: ${event.email}")
        } catch (e: Exception) {
            logger.error("Failed to send OTP email to ${event.email}: ${e.message}", e)
        }
    }
}