package tech.hanasaki.azusa.modules.auth.infrastructure.adapter

import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import tech.hanasaki.azusa.modules.auth.SmtpConfig
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.shared.domain.port.EmailService
import java.util.*

class EmailServiceImpl(private val config: SmtpConfig) : EmailService {
    override suspend fun sendHtml(to: Email, subject: String, html: String) {
        if (!config.enabled) return

        val props = Properties().apply {
            put("mail.smtp.host", config.host)
            put("mail.smtp.port", config.port.toString())
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", config.tls.toString())
            put("mail.smtp.starttls.required", config.tls.toString())
        }
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(config.username, config.password)
            }
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(config.from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to.value))
            setSubject(subject, "UTF-8")
            setContent(html, "text/html; charset=UTF-8")
        }

        Transport.send(message)
    }
}