package tech.hanasaki.azusa.modules.notification.infrastructure.adapter

import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import tech.hanasaki.azusa.modules.notification.domain.port.EmailSender
import tech.hanasaki.azusa.modules.notification.infrastructure.service.EmailTemplateService
import java.util.*

/**
 * SMTP 配置
 */
data class SmtpConfig(
    val enabled: Boolean,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val from: String,
    val tls: Boolean = true,
)

/**
 * SMTP 邮件发送器实现
 */
class SmtpEmailSender(
    private val config: SmtpConfig,
    private val templateService: EmailTemplateService? = null,
) : EmailSender {

    override suspend fun sendHtml(to: String, subject: String, html: String) {
        if (!config.enabled) return

        val session = createSession()
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(config.from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject, "UTF-8")
            setContent(html, "text/html; charset=UTF-8")
        }

        Transport.send(message)
    }

    override suspend fun sendText(to: String, subject: String, text: String) {
        if (!config.enabled) return

        val session = createSession()
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(config.from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject, "UTF-8")
            setText(text, "UTF-8")
        }

        Transport.send(message)
    }

    override suspend fun sendTemplate(
        to: String,
        subject: String,
        templateName: String,
        model: Map<String, Any>,
    ) {
        if (!config.enabled) return
        if (templateService == null) {
            throw UnsupportedOperationException("EmailTemplateService not configured")
        }

        val html = templateService.renderTemplate(templateName, model)
        sendHtml(to, subject, html)
    }

    private fun createSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.host", config.host)
            put("mail.smtp.port", config.port.toString())
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", config.tls.toString())
            put("mail.smtp.starttls.required", config.tls.toString())
        }

        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(config.username, config.password)
            }
        })
    }
}
