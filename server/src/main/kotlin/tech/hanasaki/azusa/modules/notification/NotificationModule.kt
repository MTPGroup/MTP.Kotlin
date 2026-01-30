package tech.hanasaki.azusa.modules.notification

import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.common.kernel.event.OtpGeneratedIntegrationEvent
import tech.hanasaki.azusa.common.platform.event.di.integrationEventHandler
import tech.hanasaki.azusa.modules.notification.application.listener.OtpGeneratedIntegrationListener
import tech.hanasaki.azusa.modules.notification.application.service.NotificationService
import tech.hanasaki.azusa.modules.notification.domain.port.EmailSender
import tech.hanasaki.azusa.modules.notification.domain.repository.NotificationLogRepository
import tech.hanasaki.azusa.modules.notification.domain.repository.NotificationTemplateRepository
import tech.hanasaki.azusa.modules.notification.infrastructure.adapter.SmtpConfig
import tech.hanasaki.azusa.modules.notification.infrastructure.adapter.SmtpEmailSender
import tech.hanasaki.azusa.modules.notification.infrastructure.persistence.repository.ExposedNotificationLogRepository
import tech.hanasaki.azusa.modules.notification.infrastructure.persistence.repository.ExposedNotificationTemplateRepository
import tech.hanasaki.azusa.modules.notification.infrastructure.service.EmailTemplateService

/**
 * 通知模块配置
 */
data class NotificationConfig(
    val smtp: SmtpConfig,
)

/**
 * 从应用配置读取通知模块配置
 */
fun ApplicationConfig.toNotificationConfig(): NotificationConfig {
    val smtp = config("smtp")
    return NotificationConfig(
        smtp = SmtpConfig(
            enabled = smtp.property("enabled").getString().toBoolean(),
            host = smtp.property("host").getString(),
            port = smtp.property("port").getString().toInt(),
            username = smtp.property("username").getString(),
            password = smtp.property("password").getString(),
            from = smtp.property("from").getString(),
            tls = smtp.propertyOrNull("tls")?.getString()?.toBoolean() ?: true,
        ),
    )
}

/**
 * 通知模块 Koin 定义
 */
fun notificationModule(config: ApplicationConfig) = module {
    val notificationConfig = config.toNotificationConfig()

    // Repositories
    single<NotificationLogRepository> { ExposedNotificationLogRepository() }
    single<NotificationTemplateRepository> { ExposedNotificationTemplateRepository() }

    // FreeMarker Configuration
    single<Configuration> {
        Configuration(Configuration.VERSION_2_3_32).apply {
            defaultEncoding = "UTF-8"
            setTemplateLoader(ClassTemplateLoader(
                javaClass.classLoader,
                "templates/email"
            ))
            fallbackOnNullLoopVariable = false
        }
    }

    // Services
    single { EmailTemplateService(get()) }

    // Ports / Adapters
    single<EmailSender> {
        SmtpEmailSender(
            config = notificationConfig.smtp,
            templateService = get(),
        )
    }

    // Application Service
    single {
        NotificationService(
            emailSender = get(),
            smsSender = null,   // TODO: Implement SmsSender when needed
            pushSender = null,  // TODO: Implement PushSender when needed
            logRepository = get(),
            templateRepository = get(),
        )
    }

    // Event Listeners
    single<OtpGeneratedIntegrationListener> { OtpGeneratedIntegrationListener(get()) }

    integrationEventHandler<OtpGeneratedIntegrationEvent, OtpGeneratedIntegrationListener>()
}
