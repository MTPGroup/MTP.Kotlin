package tech.hanasaki.azusa.modules.setting.application.handler

import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.modules.setting.domain.model.Setting
import tech.hanasaki.azusa.modules.setting.domain.repository.SettingRepository
import tech.hanasaki.azusa.shared.domain.event.integration.UserCreatedIntegrationEvent
import tech.hanasaki.azusa.shared.infrastructure.event.service.InMemoryEventBus

/**
 * Setting 模块事件处理器
 *
 * 负责处理用户创建事件，自动初始化用户设置
 */
class SettingEventHandler(
    private val eventBus: InMemoryEventBus,
    private val settingRepository: SettingRepository,
) {
    private val logger = LoggerFactory.getLogger(SettingEventHandler::class.java)

    /**
     * 启动事件订阅
     * @param scope 协程作用域，用于管理订阅生命周期
     */
    fun startListening(scope: CoroutineScope) {
        eventBus.subscribe<UserCreatedIntegrationEvent>(scope) { event ->
            handleUserCreated(event)
        }

        logger.info("SettingEventHandler started listening for events")
    }

    /**
     * 处理用户创建事件 - 自动创建默认设置
     */
    private suspend fun handleUserCreated(event: UserCreatedIntegrationEvent) {
        logger.info("Handling UserCreatedIntegrationEvent for user: ${event.userId}")

        try {
            val setting = Setting.create(event.userId)
            settingRepository.save(setting)
            logger.info("Default setting created for user: ${event.userId}")
        } catch (e: Exception) {
            logger.error("Failed to create default setting for ${event.userId}: ${e.message}", e)
        }
    }
}
