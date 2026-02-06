package tech.hanasaki.azusa.modules.notification

import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.notification.adapter.`in`.event.OtpGeneratedHandler
import tech.hanasaki.azusa.modules.notification.adapter.out.persistence.repository.ExposedNotificationLogRepository
import tech.hanasaki.azusa.modules.notification.adapter.out.sender.SmtpEmailSender
import tech.hanasaki.azusa.modules.notification.application.port.`in`.NotificationUseCasePort
import tech.hanasaki.azusa.modules.notification.application.port.out.EmailSenderPort
import tech.hanasaki.azusa.modules.notification.application.service.NotificationService
import tech.hanasaki.azusa.modules.notification.config.SmtpConfig
import tech.hanasaki.azusa.modules.notification.config.readSmtpConfig
import tech.hanasaki.azusa.modules.notification.domain.port.NotificationLogRepositoryPort
import tech.hanasaki.azusa.shared.domain.event.OtpGeneratedIntegrationEvent
import tech.hanasaki.azusa.shared.infrastructure.event.onIntegrationEvent


/**
 * 通知模块 Koin 定义
 */
fun notificationModule(config: ApplicationConfig) = module {
    single<NotificationLogRepositoryPort> { ExposedNotificationLogRepository() }

    single<Configuration> {
        Configuration(Configuration.VERSION_2_3_32).apply {
            defaultEncoding = "UTF-8"
            templateLoader = ClassTemplateLoader(
                javaClass.classLoader,
                "templates/email"
            )
            fallbackOnNullLoopVariable = false
        }
    }
    single<SmtpConfig> { config.readSmtpConfig() }

    single<EmailSenderPort> {
        SmtpEmailSender(
            get(),
            get()
        )
    }

    single<NotificationUseCasePort> {
        NotificationService(
            get(),
            null,   // TODO: Implement SmsSender when needed
            null,  // TODO: Implement PushSender when needed
            get(),
            get(),
        )
    }

    // 订阅集成事件
    onIntegrationEvent<OtpGeneratedIntegrationEvent>("auth.otp.generated") {
        OtpGeneratedHandler(get())
    }
}
