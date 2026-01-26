package tech.hanasaki.azusa.modules.auth.infrastructure.config

import io.ktor.server.config.*
import tech.hanasaki.azusa.modules.auth.domain.model.OtpConfig


fun ApplicationConfig.readOtpConfig(): OtpConfig {
    return OtpConfig(
        testMode = propertyOrNull("otp.testMode")?.getString()?.toBoolean() ?: false,
        testCode = propertyOrNull("otp.testCode")?.getString() ?: "123456",
    )
}
