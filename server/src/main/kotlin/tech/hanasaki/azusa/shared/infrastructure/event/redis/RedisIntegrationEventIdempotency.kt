package tech.hanasaki.azusa.shared.infrastructure.event.redis

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import tech.hanasaki.azusa.shared.domain.event.IntegrationEvent
import tech.hanasaki.azusa.shared.port.out.IntegrationEventIdempotencyPort

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisIntegrationEventIdempotency(
    private val redisCommands: RedisCoroutinesCommands<String, String>,
    private val config: StreamConfig,
) : IntegrationEventIdempotencyPort {

    override suspend fun tryAcquire(event: IntegrationEvent, handlerKey: String): Boolean {
        val processedKey = processedKey(event, handlerKey)
        if ((redisCommands.exists(processedKey) ?: 0L) > 0L) {
            return false
        }

        val lockKey = lockKey(event, handlerKey)
        val lockTtlSeconds = config.idempotencyLockTtl.inWholeSeconds.coerceAtLeast(1)
        val result = redisCommands.set(
            lockKey,
            "1",
            SetArgs().nx().ex(lockTtlSeconds)
        )
        return result == "OK"
    }

    override suspend fun markProcessed(event: IntegrationEvent, handlerKey: String) {
        val processedKey = processedKey(event, handlerKey)
        val processedTtlSeconds = config.idempotencyProcessedTtl.inWholeSeconds.coerceAtLeast(1)
        redisCommands.set(
            processedKey,
            "1",
            SetArgs().ex(processedTtlSeconds)
        )
        redisCommands.del(lockKey(event, handlerKey))
    }

    override suspend fun release(event: IntegrationEvent, handlerKey: String) {
        redisCommands.del(lockKey(event, handlerKey))
    }

    private fun lockKey(event: IntegrationEvent, handlerKey: String): String =
        "azusa:event:lock:${event.eventId}:$handlerKey"

    private fun processedKey(event: IntegrationEvent, handlerKey: String): String =
        "azusa:event:processed:${event.eventId}:$handlerKey"
}
