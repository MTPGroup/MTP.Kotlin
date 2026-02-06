package tech.hanasaki.azusa.modules.notification.config

import io.ktor.server.config.*
import tech.hanasaki.azusa.shared.infrastructure.config.optionalBoolean
import tech.hanasaki.azusa.shared.infrastructure.config.optionalInt
import tech.hanasaki.azusa.shared.infrastructure.config.optionalString
import tech.hanasaki.azusa.shared.infrastructure.config.requireString

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
 * 从应用配置读取通知模块配置
 */
fun ApplicationConfig.readSmtpConfig(): SmtpConfig {
    return SmtpConfig(
        enabled = optionalBoolean("smtp.enabled") ?: false,
        host = optionalString("smtp.host") ?: "localhost",
        port = optionalInt("smtp.port") ?: 465,
        username = requireString("smtp.username"),
        password = requireString("smtp.password"),
        from = requireString("smtp.from"),
        tls = optionalBoolean("smtp.tls") ?: true,
    )
}
