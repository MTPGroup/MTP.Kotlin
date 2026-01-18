package tech.hanasaki.azusa.auth.internal.infrastructure.external

import freemarker.template.Configuration
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils
import tech.hanasaki.azusa.auth.internal.application.port.NotificationService
import tech.hanasaki.azusa.auth.internal.config.SmtpConfig
import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.OtpType

@Service
class EmailNotificationService(
    private val config: SmtpConfig,
    private val freemarkerConfig: Configuration,
) : NotificationService {

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

    override fun sendOtp(email: Email, code: String, type: OtpType) {
        if (!config.enabled) return

        val subject = when (type) {
            OtpType.VERIFY_EMAIL -> "验证您的邮箱"
            OtpType.RESET_PASSWORD -> "重置您的密码"
            OtpType.SIGN_IN -> "登录验证"
        }

        val model = mapOf("subject" to subject, "code" to code, "minutes" to 3)
        val template = freemarkerConfig.getTemplate("email/otp.html")
        val content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model)

        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")
        helper.setFrom(config.from)
        helper.setTo(email.value)
        helper.setSubject(subject)
        helper.setText(content, true)
        mailSender.send(mimeMessage)
    }
}