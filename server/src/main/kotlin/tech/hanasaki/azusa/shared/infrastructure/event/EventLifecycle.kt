package tech.hanasaki.azusa.shared.infrastructure.event

import io.github.oshai.kotlinlogging.KotlinLogging
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.shared.infrastructure.event.outbox.OutboxPoller
import tech.hanasaki.azusa.shared.infrastructure.event.redis.RedisStreamListener

private val logger = KotlinLogging.logger { }

fun Application.configureEvents() {
    val outboxPoller by inject<OutboxPoller>()
    val redisStreamListener by inject<RedisStreamListener>()
    val redisConnection by inject<StatefulRedisConnection<String, String>>()
    val redisClient by inject<RedisClient>()

    // 应用启动后初始化事件系统
    monitor.subscribe(ApplicationStarted) {
        logger.info { "应用启动，开始初始化事件系统……" }
        launch {
            redisStreamListener.start()
        }
        outboxPoller.start()
        logger.info { "事件系统初始化完成" }
    }

    // 应用停止前清理（顺序：停协程 → 关连接 → 关客户端）
    monitor.subscribe(ApplicationStopping) {
        logger.info { "应用停止，正在清理事件系统……" }
        runBlocking {
            redisStreamListener.stop()
            outboxPoller.stop()
        }
        redisConnection.close()
        redisClient.shutdown()
        logger.info { "事件系统已停止" }
    }
}
