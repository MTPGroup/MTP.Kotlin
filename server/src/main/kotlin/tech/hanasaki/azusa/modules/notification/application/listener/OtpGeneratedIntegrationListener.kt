package tech.hanasaki.azusa.modules.notification.application.listener

import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.kernel.event.IntegrationEventListener
import tech.hanasaki.azusa.common.kernel.event.OtpGeneratedIntegrationEvent
import tech.hanasaki.azusa.modules.notification.application.service.NotificationService

class OtpGeneratedIntegrationListener(
    private val notificationService: NotificationService,
) : IntegrationEventListener<OtpGeneratedIntegrationEvent> {
    private val logger = LoggerFactory.getLogger(OtpGeneratedIntegrationListener::class.java)

    override suspend fun handle(event: OtpGeneratedIntegrationEvent) {
        try {
            val subject = when (event.type) {
                "VERIFY_EMAIL" -> "Verify your email - Azusa"
                "RESET_PASSWORD" -> "Reset your password - Azusa"
                "SIGN_IN" -> "Sign in code - Azusa"
                else -> "OTP Code - Azusa"
            }

            val html = renderOtpEmailHtml(event.code)

            notificationService.sendEmail(
                to = event.email,
                subject = subject,
                content = html,
            )

            logger.info("OTP email sent to: ${event.email}")
        } catch (e: Exception) {
            logger.error("Failed to send OTP email to ${event.email}: ${e.message}", e)
        }
    }

    private fun renderOtpEmailHtml(code: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Email Verification</title>
            </head>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2>Email Verification</h2>
                <p>Please use the following code to verify your email address:</p>
                <div style="background-color: #f5f5f5; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; margin: 20px 0;">
                    $code
                </div>
                <p>This code will expire shortly.</p>
                <p style="color: #666; font-size: 12px;">If you didn't request this code, please ignore this email.</p>
            </body>
            </html>
        """.trimIndent()
    }
}
