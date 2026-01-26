package tech.hanasaki.azusa.modules.auth.domain.model

data class OtpConfig(
    val testMode: Boolean = false,
    val testCode: String = "123456",
)
