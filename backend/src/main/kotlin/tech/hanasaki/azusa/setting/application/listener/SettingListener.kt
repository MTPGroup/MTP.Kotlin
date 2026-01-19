package tech.hanasaki.azusa.setting.application.listener

import org.slf4j.Logger
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component
import tech.hanasaki.azusa.auth.UserRegisteredEvent
import tech.hanasaki.azusa.setting.application.service.SettingService

@Component
class SettingListener(
    private val service: SettingService,
    private val logger: Logger,
) {
    @ApplicationModuleListener
    fun onUserRegistered(event: UserRegisteredEvent) {
        try {
            service.createSetting(event.userId)
        } catch (e: Exception) {
            logger.error("Failed to create setting", e)
        }
    }
}