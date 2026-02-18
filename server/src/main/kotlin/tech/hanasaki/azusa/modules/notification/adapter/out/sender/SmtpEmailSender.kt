package tech.hanasaki.azusa.modules.notification.adapter.out.sender

import freemarker.template.Configuration
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import tech.hanasaki.azusa.modules.notification.application.port.out.EmailSenderPort
import tech.hanasaki.azusa.modules.notification.config.SmtpConfig
import java.io.StringWriter
import java.util.*


/**
 * SMTP 邮件发送器实现
 */
class SmtpEmailSender(
    private val smtpConfig: SmtpConfig,
    private val freemarkerConfig: Configuration,
) : EmailSenderPort {
    private val logger = KotlinLogging.logger { }

    override suspend fun sendHtml(to: String, subject: String, html: String) {
        if (!smtpConfig.enabled) return

        val session = createSession()
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(smtpConfig.from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject, "UTF-8")
            setContent(html, "text/html; charset=UTF-8")
        }

        Transport.send(message)
    }

    override suspend fun sendText(to: String, subject: String, text: String) {
        if (!smtpConfig.enabled) return

        val session = createSession()
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(smtpConfig.from))
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
        if (!smtpConfig.enabled) {
            logger.warn { "SMTP 未启用，邮件未实际发送: $to" }
            return
        }

        val html = renderTemplate(templateName, model)
        sendHtml(to, subject, html)
    }

    private fun createSession(): Session {
        val useSSL = smtpConfig.tls && smtpConfig.port == 465
        val props = Properties().apply {
            put("mail.smtp.host", smtpConfig.host)
            put("mail.smtp.port", smtpConfig.port.toString())
            put("mail.smtp.auth", "true")
            if (useSSL) {
                put("mail.smtp.ssl.enable", "true")
            } else {
                put("mail.smtp.starttls.enable", smtpConfig.tls.toString())
                put("mail.smtp.starttls.required", smtpConfig.tls.toString())
            }
            put("mail.smtp.connectiontimeout", "5000")
            put("mail.smtp.timeout", "5000")
            put("mail.smtp.writetimeout", "5000")
        }

        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(smtpConfig.username, smtpConfig.password)
            }
        })
    }

    private fun renderTemplate(templateName: String, model: Map<String, Any>): String {
        val template = freemarkerConfig.getTemplate(templateName)
        val writer = StringWriter()
        template.process(model, writer)
        return writer.toString()
    }
}
