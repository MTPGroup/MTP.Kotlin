package tech.hanasaki.azusa.auth.internal.infrastructure.external

import freemarker.template.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils
import tech.hanasaki.azusa.auth.internal.application.service.EmailService
import tech.hanasaki.azusa.auth.internal.config.SmtpConfig
import tech.hanasaki.azusa.auth.internal.domain.model.Email

@Service
class EmailServiceImpl(
    private val config: SmtpConfig,
    private val freemarkerConfig: Configuration,
) : EmailService {

    private val mailSender by lazy {
        JavaMailSenderImpl().apply {
            host = config.host
            port = config.port
            username = config.username
            password = config.password
            defaultEncoding = "UTF-8"
            javaMailProperties["mail.smtp.auth"] = "true"
            javaMailProperties["mail.smtp.starttls.enable"] = config.tls.toString()
            javaMailProperties["mail.smtp.starttls.required"] = config.tls.toString()
            javaMailProperties["mail.smtp.connectiontimeout"] = "5000"
            javaMailProperties["mail.smtp.timeout"] = "5000"
            javaMailProperties["mail.smtp.writetimeout"] = "5000"
        }
    }

    override suspend fun sendHtml(to: Email, subject: String, html: String) {
        if (!config.enabled) return

        withContext(Dispatchers.IO) {
            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

            helper.setFrom(config.from)
            helper.setTo(to.value)
            helper.setSubject(subject)
            helper.setText(html, true)

            mailSender.send(mimeMessage)
        }
    }

    override suspend fun sendTemplate(to: Email, subject: String, templateName: String, model: Map<String, Any>) {
        val content = withContext(Dispatchers.IO) {
            val template = freemarkerConfig.getTemplate(templateName)
            FreeMarkerTemplateUtils.processTemplateIntoString(template, model)
        }
        sendHtml(to, subject, content)
    }
}
