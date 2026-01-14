package tech.hanasaki.azusa.modules.auth.api

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tech.hanasaki.azusa.modules.auth.api.dto.SignInWithPasswordRequest
import tech.hanasaki.azusa.modules.auth.api.dto.SignInWithPasswordResponse
import tech.hanasaki.azusa.modules.auth.api.dto.SignUpRequest
import tech.hanasaki.azusa.modules.auth.api.dto.RefreshTokenRequest
import tech.hanasaki.azusa.modules.auth.api.mapper.toUserProfile
import tech.hanasaki.azusa.modules.auth.application.service.AuthService
import tech.hanasaki.azusa.shared.infrastructure.utils.ApiException

fun Route.authRoutes(
    authService: AuthService,
) {
    route("/auth") {
        post("/sign-up/email") {
            val request = call.receive<SignUpRequest>()
            validateSignUp(request)

            authService.register(request.toCommand())

            val loginCommand = SignInWithPasswordRequest(email = request.email, password = request.password).toCommand()
            val result = authService.login(loginCommand)

            call.respond(
                SignInWithPasswordResponse(
                    user = result.toUserProfile(),
                    accessToken = result.tokens.accessToken,
                    refreshToken = result.tokens.refreshToken,
                    expiresIn = result.tokens.expiresIn,
                ),
            )
        }

        post("/sign-in/email") {
            val request = call.receive<SignInWithPasswordRequest>()
            validateSignIn(request)
            val result = authService.login(request.toCommand())
            call.respond(
                SignInWithPasswordResponse(
                    user = result.toUserProfile(),
                    accessToken = result.tokens.accessToken,
                    refreshToken = result.tokens.refreshToken,
                    expiresIn = result.tokens.expiresIn,
                ),
            )
        }

        /*post("/email-otp/send-verification-otp") {
            val request = call.receive<SendEmailVerificationRequest>()
            val type = OtpType.fromValue(request.type)
                ?: throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid OTP type")
            val code = emailOtpUseCase.createOtp(request.email, type)
            val subject = otpSubject(type)
            val html = otpTemplate(code, otpConfig.expiresMinutes)
            if (smtpConfig.enabled) {
                mailer.sendHtml(request.email, subject, html)
            }
            if (otpDebug.returnCode) {
                call.response.headers.append("X-OTP-Code", code)
            }
            call.respond(SendEmailVerificationResponse(success = true))
        }

        post("/email-otp/forget-password") {
            val request = call.receive<SendPasswordResetEmailRequest>()
            val code = emailOtpUseCase.createOtp(request.email, OtpType.RESET_PASSWORD)
            val subject = otpSubject(OtpType.RESET_PASSWORD)
            val html = otpTemplate(code, otpConfig.expiresMinutes)
            if (smtpConfig.enabled) {
                mailer.sendHtml(request.email, subject, html)
            }
            if (otpDebug.returnCode) {
                call.response.headers.append("X-OTP-Code", code)
            }
            call.respond(SendPasswordResetEmailResponse(success = true))
        }

        post("/email-otp/verify-email") {
            val request = call.receive<VerifyOTPRequest>()
            emailOtpUseCase.verifyOtp(request.email, OtpType.VERIFY_EMAIL, request.otp)
            emailOtpUseCase.markEmailVerified(request.email)
            val authUser = emailOtpUseCase.getUserProfile(request.email)
            call.respond(
                VerifyOTPResponse(
                    token = null,
                    user = authUser.toUserProfile(),
                ),
            )
        }

        post("/email-otp/reset-password") {
            val request = call.receive<ResetPasswordRequest>()
            emailOtpUseCase.verifyOtp(request.email, OtpType.RESET_PASSWORD, request.otp)
            if (request.password.length < 6) {
                throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Password too short")
            }
            val newHash = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())
            emailOtpUseCase.resetPassword(request.email, newHash)
            call.respond(ResetPasswordResponse(success = true))
        }*/

        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val result = authService.refreshToken(request.refreshToken)
            call.respond(
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

//private fun otpSubject(type: OtpType): String {
//    return when (type) {
//        OtpType.SIGN_IN -> "Your sign-in code"
//        OtpType.RESET_PASSWORD -> "Your password reset code"
//        OtpType.VERIFY_EMAIL -> "Verify your email"
//    }
//}
//
//private fun otpTemplate(code: String, expiresMinutes: Int): String {
//    return """
//        <div style="font-family: Arial, sans-serif; line-height: 1.6;">
//          <h2>Your verification code</h2>
//          <p>Use the following code to continue:</p>
//          <div style="font-size: 24px; font-weight: bold; letter-spacing: 4px;">$code</div>
//          <p>This code expires in $expiresMinutes minutes.</p>
//        </div>
//    """.trimIndent()
//}
