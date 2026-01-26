package tech.hanasaki.azusa.modules.notification.application.handler

import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.platform.event.bus.InMemoryEventBus
import tech.hanasaki.azusa.modules.auth.domain.event.OtpGeneratedEvent
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.modules.notification.application.service.NotificationService

/**
 * 通知事件处理器 - 订阅需要发送通知的事件
 */
class NotificationEventHandler(
    private val eventBus: InMemoryEventBus,
    private val notificationService: NotificationService,
) {
    private val logger = LoggerFactory.getLogger(NotificationEventHandler::class.java)

    /**
     * 启动事件订阅
     * @param scope 协程作用域，用于管理订阅生命周期
     */
    fun startListening(scope: CoroutineScope) {
        // 订阅 OTP 生成事件
        eventBus.subscribe<OtpGeneratedEvent>(scope) { event ->
            handleOtpGenerated(event)
        }

        logger.info("NotificationEventHandler started listening for events")
    }

    /**
     * 处理 OTP 生成事件 - 发送 OTP 邮件
     */
    private suspend fun handleOtpGenerated(event: OtpGeneratedEvent) {
        logger.info("Handling OtpGeneratedEvent for email: ${event.email.value}")

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

        val data = mapOf(
            "code" to code,
            "title" to title,
            "description" to description
        )
        /*val template = freeMarkerConfig.getTemplate("email/otp-verification.html")

            return FreeMarkerContent(
                "",
                mapOf(

                ),
            ).toString()*/

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
}
