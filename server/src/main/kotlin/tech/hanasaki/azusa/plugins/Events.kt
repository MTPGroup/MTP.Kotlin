package tech.hanasaki.azusa.plugins

import io.ktor.server.application.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Events")

/**
 * 事件系统配置
 *
 * 负责：
 * - 启动/停止 OutboxPoller（事件可靠性保证）
 * - 初始化 IntegrationEventBus 的事件处理器
 */
fun Application.configureEvents() {
//    val outboxPoller by inject<OutboxPoller>()

    // 应用启动后初始化事件系统
    monitor.subscribe(ApplicationStarted) {
        logger.info("Application started, initializing event system")

        logger.info("Event system initialized")
    }

    // 应用停止前清理
    monitor.subscribe(ApplicationStopping) {
        logger.info("Application stopping, shutting down event system")
    }
}
