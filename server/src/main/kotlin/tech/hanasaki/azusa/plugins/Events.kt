package tech.hanasaki.azusa.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.platform.event.listener.RedisStreamListener
import tech.hanasaki.azusa.common.platform.event.outbox.OutboxPoller

private val logger = LoggerFactory.getLogger("Events")

/**
 * 事件系统配置
 */
fun Application.configureEvents() {
    val outboxPoller by inject<OutboxPoller>()
    val redisStreamListener by inject<RedisStreamListener>()

    // 应用启动后初始化事件系统
    monitor.subscribe(ApplicationStarted) {
        logger.info("Application started, initializing event system")
        launch {
            redisStreamListener.start()
        }
        outboxPoller.start()
        logger.info("Event system initialized")
    }

    // 应用停止前清理
    monitor.subscribe(ApplicationStopping) {
        redisStreamListener.stop()
        outboxPoller.stop()
        logger.info("Application stopping, shutting down event system")
    }
}
