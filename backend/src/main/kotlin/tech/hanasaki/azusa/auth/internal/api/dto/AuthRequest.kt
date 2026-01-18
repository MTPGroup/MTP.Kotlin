package tech.hanasaki.azusa.auth.internal.api.dto

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.auth.internal.application.command.LoginCommand
import tech.hanasaki.azusa.auth.internal.application.command.RegisterCommand
import tech.hanasaki.azusa.auth.internal.application.command.ResetPasswordCommand
import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.Username

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
data class SignOutRequest(
    val refreshToken: String,
)

@Serializable
data class SendOtpRequest(
    val email: String,
    val type: String,
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
) {
    fun toCommand(): ResetPasswordCommand = ResetPasswordCommand(
        email = Email(email),
        newPassword = password
    )
}

@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
)
