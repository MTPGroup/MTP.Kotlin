package tech.hanasaki.azusa.shared.infrastructure.event

import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.shared.infrastructure.event.outbox.OutboxPoller
import tech.hanasaki.azusa.shared.infrastructure.event.redis.RedisStreamListener

private val logger = LoggerFactory.getLogger("Events")

fun Application.configureEvents() {
    val outboxPoller by inject<OutboxPoller>()
    val redisStreamListener by inject<RedisStreamListener>()

    // 应用启动后初始化事件系统
    monitor.subscribe(ApplicationStarted) {
        logger.info("应用启动，开始初始化事件系统……")
        launch {
            redisStreamListener.start()
        }
        outboxPoller.start()
        logger.info("事件系统初始化完成")
    }

    // 应用停止前清理
    monitor.subscribe(ApplicationStopping) {
        redisStreamListener.stop()
        outboxPoller.stop()
        logger.info("应用停止，清理事件系统……")
    }
}
