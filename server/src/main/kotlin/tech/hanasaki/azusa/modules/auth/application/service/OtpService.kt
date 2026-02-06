package tech.hanasaki.azusa.modules.auth.application.service

import tech.hanasaki.azusa.modules.auth.application.port.`in`.OtpUseCasePort
import tech.hanasaki.azusa.modules.auth.config.OtpConfig
import tech.hanasaki.azusa.shared.domain.model.base.publishAndClear
import tech.hanasaki.azusa.modules.auth.domain.model.Otp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.modules.auth.domain.port.OtpRepositoryPort
import tech.hanasaki.azusa.shared.domain.event.OtpGeneratedIntegrationEvent
import tech.hanasaki.azusa.shared.domain.exception.AuthenticationException
import tech.hanasaki.azusa.shared.domain.exception.HitLimitException
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.port.out.DomainEventBusPort
import tech.hanasaki.azusa.shared.port.out.OutboxSchedulerPort
import tech.hanasaki.azusa.shared.port.out.StringEncoderPort
import tech.hanasaki.azusa.shared.port.out.TransactionalPort
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class OtpService(
    private val otpRepository: OtpRepositoryPort,
    private val otpConfig: OtpConfig,
    private val encoder: StringEncoderPort,
    private val domainEventBus: DomainEventBusPort,
    private val outboxScheduler: OutboxSchedulerPort,
    private val tx: TransactionalPort,
) : OtpUseCasePort {
    override suspend fun generate(email: Email, type: OtpType) {
        tx.execute {
            val oneHourAgo = Clock.System.now().minus(1.hours)
            val sentCount = otpRepository.countSentAfter(email, type, oneHourAgo)
            if (sentCount >= otpConfig.maxPerHour) {
                throw HitLimitException(
                    message = "OTP请求已达限制。请稍后再试。",
                    retryAfter = 1.hours.inWholeMilliseconds
                )
            }

            val code = if (otpConfig.testMode) otpConfig.testCode else generateCode()
            val otp = Otp.create(
                email = email,
                codeHash = encoder.encode(code),
                type = type,
                expiresAt = Clock.System.now().plus(otpConfig.expiresMinutes.minutes)
            )
            otpRepository.save(otp)
            otp.publishAndClear(domainEventBus)
            outboxScheduler.schedule(
                OtpGeneratedIntegrationEvent(
                    email = email.value,
                    code = code,
                    type = type.name,
                )
            )
        }
    }

    override suspend fun verify(email: Email, type: OtpType, code: String) {
        tx.execute {
            val otp = otpRepository.findValidLatest(email, type)
                ?: throw AuthenticationException("非法或已过期的验证码")

            if (!encoder.verify(code, otp.codeHash)) {
                throw AuthenticationException("非法的验证码")
            }

            if (!otp.isValid()) {
                throw AuthenticationException("验证码已过期")
            }

            otpRepository.markAsUsed(otp)
        }
    }

    private fun generateCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }
}