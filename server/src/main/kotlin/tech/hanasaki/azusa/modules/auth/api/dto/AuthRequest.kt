package tech.hanasaki.azusa.modules.auth.api.dto

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.auth.application.command.LoginCommand
import tech.hanasaki.azusa.modules.auth.application.command.RegisterCommand
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.Username

@Serializable
data class SignUpRequest(
    val email: String,
    val name: String,
    val password: String,
) {
    fun toCommand(): RegisterCommand = RegisterCommand(
        email = Email(email),
        password = password,
        username = Username(name),
    )
}

@Serializable
data class SignInWithPasswordRequest(
    val email: String,
    val password: String,
) {
    fun toCommand(): LoginCommand = LoginCommand(
        email = Email(email),
        password = password,
    )
}

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

@Serializable
data class SignOutRequest(
    val refreshToken: String? = null,
)

@Serializable
data class SendEmailVerificationRequest(
    val email: String,
    val type: String,
)

@Serializable
data class SendPasswordResetEmailRequest(
    val email: String,
)

@Serializable
data class VerifyOTPRequest(
    val email: String,
    val otp: String,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val password: String,
)
