package tech.hanasaki.azusa.common.platform.event.di

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import tech.hanasaki.azusa.common.kernel.event.IntegrationEvent
import tech.hanasaki.azusa.common.kernel.event.IntegrationEventListener
import tech.hanasaki.azusa.common.platform.event.listener.RedisStreamListener
import tech.hanasaki.azusa.common.platform.util.AppJson

/**
 * 注册集成事件处理器
 *
 * 使用示例:
 * ```kotlin
 * // 定义监听器（依赖通过构造函数注入）
 * class OtpGeneratedIntegrationListener(
 *     private val notificationService: NotificationService
 * ) : IntegrationEventListener<OtpGeneratedIntegrationEvent> {
 *     override suspend fun handle(event: OtpGeneratedIntegrationEvent) {
 *         notificationService.sendEmail(...)
 *     }
 * }
 *
 * // 在模块中注册
 * single<OtpGeneratedIntegrationListener> { OtpGeneratedIntegrationListener(get()) }
 * integrationEventHandler<OtpGeneratedIntegrationEvent, OtpGeneratedIntegrationListener>()
 * ```
 *
 * @param E 集成事件类型，必须实现 IntegrationEvent
 * @param L 监听器类型，必须实现 IntegrationEventListener<E>
 */
@OptIn(InternalSerializationApi::class)
inline fun <reified E : IntegrationEvent, reified L : IntegrationEventListener<E>> Module.integrationEventHandler() {
    single(named("IntegrationEventHandler_${E::class.simpleName}"), createdAtStart = true) {
        val redisStreamListener = get<RedisStreamListener>()
        val listener = get<L>()

        @Suppress("UNCHECKED_CAST")
        val eventSerializer = E::class.serializer()

        val eventType = eventSerializer.descriptor.serialName

        redisStreamListener.registerHandler(eventType) { payload ->
            try {
                val payloadString = payload["payload"]
                    ?: throw IllegalArgumentException("Event payload is missing")

                val event = AppJson.json.decodeFromString(eventSerializer, payloadString)
                listener.handle(event)
            } catch (e: Exception) {
                val logger = org.slf4j.LoggerFactory.getLogger("IntegrationEventHandler")
                logger.error("Failed to handle IntegrationEvent $eventType: ${e.message}", e)
                throw e
            }
        }

        object {}
    }
}
