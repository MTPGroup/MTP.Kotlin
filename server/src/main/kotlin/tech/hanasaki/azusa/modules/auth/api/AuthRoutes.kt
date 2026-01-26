package tech.hanasaki.azusa.modules.auth.api

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.auth.api.dto.*
import tech.hanasaki.azusa.modules.auth.api.mapper.toUserProfile
import tech.hanasaki.azusa.modules.auth.application.service.AuthService
import tech.hanasaki.azusa.modules.auth.application.service.OtpService
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.common.platform.api.requireUserId
import tech.hanasaki.azusa.common.platform.api.ApiException
import tech.hanasaki.azusa.common.platform.api.respondOk

fun Route.authRoutes() {
    val authService: AuthService by inject()
    val otpService: OtpService by inject()

    route("/auth") {
        post("/sign-up/email") {
            val request = call.receive<SignUpRequest>()
            validateSignUp(request)

            authService.register(request.toCommand())

            call.respondOk(SignUpResponse(success = true))
        }

        post("/sign-in/email") {
            val request = call.receive<SignInWithPasswordRequest>()
            validateSignIn(request)
            val result = authService.login(request.toCommand())
            call.respondOk(
                SignInWithPasswordResponse(
                    user = result.toUserProfile(),
                    accessToken = result.tokens.accessToken,
                    refreshToken = result.tokens.refreshToken,
                    expiresIn = result.tokens.expiresIn,
                ),
            )
        }

        post("/email-otp/send") {
            val request = call.receive<SendOtpRequest>()
            otpService.sendOtp(Email(request.email), OtpType.fromValue(request.type))
            call.respondOk(OtpSendResponse(true))
        }

        post("/email-otp/verify-email") {
            val request = call.receive<VerifyOTPRequest>()
            val email = Email(request.email)
            otpService.verifyOtp(
                email,
                OtpType.VERIFY_EMAIL,
                request.otp
            )
            authService.verifyEmail(email)
            call.respondOk(VerifyOTPResponse(true))
        }

        post("/email-otp/reset-password") {
            val request = call.receive<ResetPasswordRequest>()
            val email = Email(request.email)
            otpService.verifyOtp(
                email,
                OtpType.RESET_PASSWORD,
                request.otp
            )
            if (request.password.length < 6) {
                throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Password too short")
            }
            authService.resetPassword(request.toCommand())
            call.respondOk(ResetPasswordResponse(success = true))
        }

        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val result = authService.refreshToken(request.refreshToken)
            call.respondOk(
                SignInWithPasswordResponse(
                    user = result.toUserProfile(),
                    accessToken = result.tokens.accessToken,
                    refreshToken = result.tokens.refreshToken,
                    expiresIn = result.tokens.expiresIn,
                )
            )
        }

        post("/sign-out") {
            val request = runCatching { call.receive<RefreshTokenRequest>() }.getOrNull()
            if (request != null) {
                authService.logout(request.refreshToken)
            }
            call.respond(HttpStatusCode.NoContent)
        }

        authenticate("auth-jwt") {
            get("/me") {
                val userId = call.requireUserId()
                val user = authService.getProfile(userId)
                call.respondOk(user.toUserProfile())
            }

            post("/password/change") {
                val userId = call.requireUserId()
                val request = call.receive<ChangePasswordRequest>()
                if (request.newPassword.length < 6) {
                    throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Password too short")
                }
                authService.changePassword(userId, request.oldPassword, request.newPassword)
                call.respondOk(ChangePasswordResponse(success = true))
            }

            delete("/account") {
                val userId = call.requireUserId()
                authService.deleteAccount(userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun validateSignUp(request: SignUpRequest) {
    if (request.email.isBlank() || !request.email.contains("@")) {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid email")
    }
    if (request.password.length < 6) {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Password too short")
    }
    if (request.name.isBlank()) {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Name is required")
    }
}

private fun validateSignIn(request: SignInWithPasswordRequest) {
    if (request.email.isBlank() || !request.email.contains("@")) {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid email")
    }
    if (request.password.isBlank()) {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Password is required")
    }
}
