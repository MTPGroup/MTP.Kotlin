package tech.hanasaki.azusa.shared.infrastructure.event.redis

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import tech.hanasaki.azusa.shared.port.out.EventPublisherPort

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisStreamEventPublisher(
    private val streamConfig: StreamConfig,
    private val redisCommands: RedisCoroutinesCommands<String, String>,
) : EventPublisherPort {
    override suspend fun publish(eventType: String, payload: String) {
        val message = mapOf(
            "eventType" to eventType,
            "payload" to payload,
        )
        redisCommands.xadd(streamConfig.streamKey, message)
    }
}
