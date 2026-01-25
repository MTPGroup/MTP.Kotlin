package tech.hanasaki.azusa.modules.auth.application.handler

import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.modules.auth.application.service.OtpService
import tech.hanasaki.azusa.modules.auth.domain.events.UserRegisteredEvent
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.shared.infrastructure.event.bus.InMemoryEventBus

/**
 * Auth 模块事件处理器
 *
 * 负责处理认证相关的领域事件：
 * - UserRegisteredEvent: 用户注册后自动发送验证码邮件
 */
class AuthEventHandler(
    private val eventBus: InMemoryEventBus,
    private val otpService: OtpService,
) {
    private val logger = LoggerFactory.getLogger(AuthEventHandler::class.java)

    /**
     * 启动事件订阅
     * @param scope 协程作用域，用于管理订阅生命周期
     */
    fun startListening(scope: CoroutineScope) {
        // 订阅用户注册事件
        eventBus.subscribe<UserRegisteredEvent>(scope) { event ->
            handleUserRegistered(event)
        }

        logger.info("AuthEventHandler started listening for events")
    }

    /**
     * 处理用户注册事件 - 自动发送邮箱验证码
     */
    private suspend fun handleUserRegistered(event: UserRegisteredEvent) {
        logger.info("Handling UserRegisteredEvent for user: ${event.userId}")

        try {
            otpService.sendOtp(event.email, OtpType.VERIFY_EMAIL)
            logger.info("Verification email sent to: ${event.email.value}")
        } catch (e: Exception) {
            // 邮件发送失败不应阻断注册流程，记录日志即可
            logger.error("Failed to send verification email to ${event.email.value}: ${e.message}", e)
        }
    }
}
