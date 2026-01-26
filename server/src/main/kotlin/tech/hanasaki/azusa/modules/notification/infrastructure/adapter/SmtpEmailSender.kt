package tech.hanasaki.azusa.modules.notification.infrastructure.adapter

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import tech.hanasaki.azusa.modules.notification.domain.port.EmailSender
import java.util.Properties

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
class SmtpEmailSender(private val config: SmtpConfig) : EmailSender {

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
