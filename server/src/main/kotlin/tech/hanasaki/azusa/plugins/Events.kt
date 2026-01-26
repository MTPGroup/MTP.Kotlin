package tech.hanasaki.azusa.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.platform.event.outbox.OutboxPoller
import tech.hanasaki.azusa.modules.auth.application.handler.AuthEventHandler
import tech.hanasaki.azusa.modules.notification.application.handler.NotificationEventHandler
import tech.hanasaki.azusa.modules.setting.application.handler.SettingEventHandler

private val logger = LoggerFactory.getLogger("Events")

/**
 * 事件系统配置
 *
 * 负责：
 * - 启动/停止 OutboxPoller（事件可靠性保证）
 * - 启动各模块的事件处理器
 */
fun Application.configureEvents() {
    val outboxPoller by inject<OutboxPoller>()
    val authEventHandler by inject<AuthEventHandler>()
    val settingEventHandler by inject<SettingEventHandler>()
    val notificationEventHandler by inject<NotificationEventHandler>()

    // 事件处理器的协程作用域
    val eventScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 应用启动后初始化事件系统
    monitor.subscribe(ApplicationStarted) {
        logger.info("Application started, initializing event system")

        // 启动 Outbox 轮询器
        outboxPoller.start()

        // 启动各模块事件处理器
        notificationEventHandler.startListening(eventScope)  // 必须在 authEventHandler 之前，因为 auth 发布的事件会被 notification 处理
        authEventHandler.startListening(eventScope)
        settingEventHandler.startListening(eventScope)

        logger.info("Event system initialized")
    }

    // 应用停止前清理
    monitor.subscribe(ApplicationStopping) {
        logger.info("Application stopping, shutting down event system")
        outboxPoller.stop()
    }
}
