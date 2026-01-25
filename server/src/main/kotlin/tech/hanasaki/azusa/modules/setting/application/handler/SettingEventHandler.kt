package tech.hanasaki.azusa.modules.setting.application.handler

import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.modules.setting.domain.model.Setting
import tech.hanasaki.azusa.modules.setting.domain.repository.SettingRepository
import tech.hanasaki.azusa.shared.domain.event.integration.InitializeUserResources
import tech.hanasaki.azusa.shared.domain.model.UserId
import tech.hanasaki.azusa.shared.infrastructure.event.bus.InMemoryEventBus
import java.util.*

/**
 * Setting 模块事件处理器
 *
 * 负责处理用户资源初始化事件，自动初始化用户设置。
 * 订阅通用的 InitializeUserResources 事件，实现与 Auth 模块的低耦合通信。
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
        eventBus.subscribe<InitializeUserResources>(scope) { event ->
            handleUserInitialization(event)
        }

        logger.info("SettingEventHandler started listening for InitializeUserResources events")
    }

    /**
     * 处理用户资源初始化事件 - 自动创建默认设置
     */
    private suspend fun handleUserInitialization(event: InitializeUserResources) {
        logger.info("Handling InitializeUserResources for user: ${event.userId}")

        try {
            val userId = UserId(UUID.fromString(event.userId))
            val setting = Setting.create(userId)
            settingRepository.save(setting)
            logger.info("Default setting created for user: ${event.userId}")
        } catch (e: Exception) {
            logger.error("Failed to create default setting for ${event.userId}: ${e.message}", e)
        }
    }
}
