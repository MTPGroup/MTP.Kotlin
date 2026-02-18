package tech.hanasaki.azusa.modules.auth.config

import io.ktor.server.config.*

data class OtpConfig(
    val testMode: Boolean = false,
    val testCode: String = "123456",
    val expiresMinutes: Int = 10,
    val maxPerHour: Int = 5,
)

fun ApplicationConfig.readOtpConfig(): OtpConfig {
    return OtpConfig(
        testMode = propertyOrNull("otp.testMode")?.getString()?.toBoolean() ?: false,
        testCode = propertyOrNull("otp.testCode")?.getString() ?: "123456",
        expiresMinutes = propertyOrNull("otp.expiresMinutes")?.getString()?.toInt() ?: 10,
        maxPerHour = propertyOrNull("otp.maxPerHour")?.getString()?.toInt() ?: 5,
    )
}
