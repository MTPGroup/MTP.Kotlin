package tech.hanasaki.azusa.common.platform.event.di

import kotlinx.coroutines.runBlocking
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import tech.hanasaki.azusa.common.kernel.event.DomainEvent
import tech.hanasaki.azusa.common.kernel.event.EventListener
import tech.hanasaki.azusa.common.kernel.event.EventSubscriber
import tech.hanasaki.azusa.common.kernel.event.SubscriptionMode
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


/**
 * 注册并订阅监听器类
 *
 * @param L 监听器类型，必须实现 EventListener<E>
 * @param E 事件类型
 * @param constructor 监听器的构造函数，例如 ::UserRegisteredListener
 * @param mode 订阅模式
 */

inline fun <reified L : EventListener<E>, reified E : DomainEvent> Module.subscriber(
    crossinline constructor: Scope.() -> L,
    mode: SubscriptionMode = SubscriptionMode.ASYNCHRONOUS,
) {
    single<L> {
        constructor()
    }

    single(named("Subscriber_${L::class.simpleName}_${Uuid.random()}"), createdAtStart = true) {
        val listener = get<L>()
        val subscriber = get<EventSubscriber>()
        runBlocking {
            subscriber.subscribe(E::class.java, listener, mode)
        }
        object {}
    }
}
