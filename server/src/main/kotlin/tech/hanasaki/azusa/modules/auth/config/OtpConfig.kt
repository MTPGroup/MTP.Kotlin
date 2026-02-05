package tech.hanasaki.azusa.modules.auth.config

import io.ktor.server.config.*

data class OtpConfig(
    val testMode: Boolean = false,
    val testCode: String = "123456",
)

fun ApplicationConfig.readOtpConfig(): OtpConfig {
    return OtpConfig(
        testMode = propertyOrNull("otp.testMode")?.getString()?.toBoolean() ?: false,
        testCode = propertyOrNull("otp.testCode")?.getString() ?: "123456",
    )
}
