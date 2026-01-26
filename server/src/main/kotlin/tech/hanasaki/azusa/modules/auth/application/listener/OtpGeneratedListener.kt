package tech.hanasaki.azusa.modules.auth.application.listener

import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.kernel.event.EventListener
import tech.hanasaki.azusa.modules.auth.domain.event.OtpGeneratedEvent

class OtpGeneratedListener() : EventListener<OtpGeneratedEvent> {
    private val logger = LoggerFactory.getLogger(OtpGeneratedListener::class.java)

    override suspend fun handle(event: OtpGeneratedEvent) {
        // TODO: 转换成集成事件
    }
}