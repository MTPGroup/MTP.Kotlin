package tech.hanasaki.azusa.modules.notification.application.listener

import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.domain.event.IntegrationEventListener
import tech.hanasaki.azusa.common.domain.event.OtpGeneratedIntegrationEvent
import tech.hanasaki.azusa.modules.notification.application.service.NotificationService

class OtpGeneratedIntegrationListener(
    private val notificationService: NotificationService,
) : IntegrationEventListener<OtpGeneratedIntegrationEvent> {
    private val logger = LoggerFactory.getLogger(OtpGeneratedIntegrationListener::class.java)

    override suspend fun handle(event: OtpGeneratedIntegrationEvent) {
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

            notificationService.sendOtpEmail(
                to = event.email,
                code = event.code,
                subject = subject,
                title = title,
                description = description,
            )

            logger.info("OTP email sent to: ${event.email}")
        } catch (e: Exception) {
            logger.error("Failed to send OTP email to ${event.email}: ${e.message}", e)
        }
    }
}
