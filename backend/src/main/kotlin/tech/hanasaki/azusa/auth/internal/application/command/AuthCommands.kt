package tech.hanasaki.azusa.auth.application.command

import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.Username

data class RegisterCommand(
    val email: Email,
    val password: String,
    val username: Username,
)

data class LoginCommand(
    val email: Email,
    val password: String,
)

data class ResetPasswordCommand(
    val email: Email,
    val newPassword: String,
)
