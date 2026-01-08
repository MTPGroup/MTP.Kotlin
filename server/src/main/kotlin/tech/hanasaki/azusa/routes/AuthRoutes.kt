package tech.hanasaki.azusa.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tech.hanasaki.azusa.auth.*
import tech.hanasaki.azusa.common.ApiException
import tech.hanasaki.azusa.mail.SmtpConfig
import tech.hanasaki.azusa.mail.SmtpMailer
import java.util.*

fun Route.authRoutes(config: ApplicationConfig): Unit {
    val smtpConfig = config.readSmtpConfig()
    val otpConfig = config.readOtpConfig()
    val otpDebug = config.readOtpDebugConfig()
    val authConfig = config.readAuthConfig()
    val mailer = SmtpMailer(smtpConfig)
    val otpService = EmailOtpService(otpConfig)
    val refreshTokenService = RefreshTokenService(authConfig.refreshTokenDays)

    route("/auth") {
        post("/sign-up/email") {
            val request = call.receive<SignUpRequest>()
            validateSignUp(request)
            val authUser = AuthService.register(request.email, request.password, request.name)
            val token = issueToken(config, authUser.userId, authConfig.accessTokenMinutes)
            val refreshToken = refreshTokenService.issue(authUser.userId)
            call.respond(
                SignUpResponse(
                    token = token,
                    refreshToken = refreshToken,
                    user = authUser.toUserProfile(),
                ),
            )
        }

        post("/sign-in/email") {
            val request = call.receive<SignInWithPasswordRequest>()
            validateSignIn(request)
            val authUser = AuthService.login(request.email, request.password)
            val token = issueToken(config, authUser.userId, authConfig.accessTokenMinutes)
            val refreshToken = refreshTokenService.issue(authUser.userId)
            call.respond(
                SignInWithPasswordResponse(
                    redirect = false,
                    token = token,
                    refreshToken = refreshToken,
                    user = authUser.toUserProfile(),
                ),
            )
        }

        post("/email-otp/send-verification-otp") {
            val request = call.receive<SendEmailVerificationRequest>()
            val type = OtpType.fromValue(request.type)
                ?: throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid OTP type")
            val code = otpService.createOtp(request.email, type)
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
            val code = otpService.createOtp(request.email, OtpType.RESET_PASSWORD)
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
            otpService.verifyOtp(request.email, OtpType.VERIFY_EMAIL, request.otp)
            otpService.markEmailVerified(request.email)
            call.respond(HttpStatusCode.NoContent)
        }

        post("/email-otp/reset-password") {
            val request = call.receive<ResetPasswordRequest>()
            otpService.verifyOtp(request.email, OtpType.RESET_PASSWORD, request.otp)
            if (request.password.length < 6) {
                throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Password too short")
            }
            val newHash = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())
            otpService.resetPassword(request.email, newHash)
            call.respond(ResetPasswordResponse(success = true))
        }

        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val userId = refreshTokenService.validate(request.refreshToken)
            val token = issueToken(config, userId, authConfig.accessTokenMinutes)
            val newRefresh = refreshTokenService.rotate(request.refreshToken)
            call.respond(RefreshTokenResponse(token = token, refreshToken = newRefresh))
        }

        post("/sign-out") {
            val body = runCatching { call.receive<SignOutRequest>() }.getOrNull()
            val refreshToken = body?.refreshToken
            if (!refreshToken.isNullOrBlank()) {
                refreshTokenService.revoke(refreshToken)
            }
            call.respond(HttpStatusCode.OK, SignOutResponse(success = true))
        }
    }
}

private fun validateSignUp(request: SignUpRequest): Unit {
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

private fun validateSignIn(request: SignInWithPasswordRequest): Unit {
    if (request.email.isBlank() || !request.email.contains("@")) {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid email")
    }
    if (request.password.isBlank()) {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Password is required")
    }
}

private fun issueToken(config: ApplicationConfig, userId: UUID, accessTokenMinutes: Int): String {
    val issuer = config.property("jwt.issuer").getString()
    val audience = config.property("jwt.audience").getString()
    val secret = config.property("jwt.secret").getString()
    val nowMillis = System.currentTimeMillis()
    val expiresAt = Date(nowMillis + accessTokenMinutes.toLong() * 60 * 1000)
    return JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withSubject(userId.toString())
        .withIssuedAt(Date(nowMillis))
        .withExpiresAt(expiresAt)
        .sign(Algorithm.HMAC256(secret))
}

private fun AuthUser.toUserProfile(): UserProfile {
    return UserProfile(
        id = profileId.toString(),
        email = email,
        name = username,
        image = avatar,
        emailVerified = emailVerified,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

private fun otpSubject(type: OtpType): String {
    return when (type) {
        OtpType.SIGN_IN -> "Your sign-in code"
        OtpType.RESET_PASSWORD -> "Your password reset code"
        OtpType.VERIFY_EMAIL -> "Verify your email"
    }
}

private fun otpTemplate(code: String, expiresMinutes: Int): String {
    return """
        <div style="font-family: Arial, sans-serif; line-height: 1.6;">
          <h2>Your verification code</h2>
          <p>Use the following code to continue:</p>
          <div style="font-size: 24px; font-weight: bold; letter-spacing: 4px;">$code</div>
          <p>This code expires in $expiresMinutes minutes.</p>
        </div>
    """.trimIndent()
}

private fun ApplicationConfig.readSmtpConfig(): SmtpConfig {
    return SmtpConfig(
        host = property("smtp.host").getString(),
        port = property("smtp.port").getString().toInt(),
        username = property("smtp.username").getString(),
        password = property("smtp.password").getString(),
        from = property("smtp.from").getString(),
        tls = property("smtp.tls").getString().toBoolean(),
        enabled = property("smtp.enabled").getString().toBoolean(),
    )
}

private fun ApplicationConfig.readOtpConfig(): OtpConfig {
    return OtpConfig(
        length = property("otp.length").getString().toInt(),
        expiresMinutes = property("otp.expiresMinutes").getString().toInt(),
        minIntervalSeconds = property("otp.minIntervalSeconds").getString().toInt(),
        maxPerHour = property("otp.maxPerHour").getString().toInt(),
    )
}

private data class OtpDebugConfig(
    val returnCode: Boolean,
)

private fun ApplicationConfig.readOtpDebugConfig(): OtpDebugConfig {
    return OtpDebugConfig(
        returnCode = property("otp.debugReturn").getString().toBoolean(),
    )
}

private data class AuthConfig(
    val accessTokenMinutes: Int,
    val refreshTokenDays: Int,
)

private fun ApplicationConfig.readAuthConfig(): AuthConfig {
    return AuthConfig(
        accessTokenMinutes = property("auth.accessTokenMinutes").getString().toInt(),
        refreshTokenDays = property("auth.refreshTokenDays").getString().toInt(),
    )
}
