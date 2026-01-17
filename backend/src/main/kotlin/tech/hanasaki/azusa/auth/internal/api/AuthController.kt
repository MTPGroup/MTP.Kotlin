package tech.hanasaki.azusa.auth.internal.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import tech.hanasaki.azusa.auth.internal.api.dto.ChangePasswordRequest
import tech.hanasaki.azusa.auth.internal.api.dto.ChangePasswordResponse
import tech.hanasaki.azusa.auth.internal.api.dto.OtpSendResponse
import tech.hanasaki.azusa.auth.internal.api.dto.RefreshTokenRequest
import tech.hanasaki.azusa.auth.internal.api.dto.ResetPasswordRequest
import tech.hanasaki.azusa.auth.internal.api.dto.ResetPasswordResponse
import tech.hanasaki.azusa.auth.internal.api.dto.SendOtpRequest
import tech.hanasaki.azusa.auth.internal.api.dto.SignInWithPasswordRequest
import tech.hanasaki.azusa.auth.internal.api.dto.SignInWithPasswordResponse
import tech.hanasaki.azusa.auth.internal.api.dto.SignUpRequest
import tech.hanasaki.azusa.auth.internal.api.dto.SignUpResponse
import tech.hanasaki.azusa.auth.internal.api.dto.UserProfile
import tech.hanasaki.azusa.auth.internal.api.dto.VerifyOTPRequest
import tech.hanasaki.azusa.auth.internal.api.dto.VerifyOTPResponse
import tech.hanasaki.azusa.auth.internal.api.mapper.toUserProfile
import tech.hanasaki.azusa.auth.internal.application.service.AuthService
import tech.hanasaki.azusa.auth.internal.application.service.OtpService
import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.OtpType
import tech.hanasaki.azusa.auth.internal.domain.model.UserId
import tech.hanasaki.azusa.shared.ApiException
import java.util.UUID

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val otpService: OtpService,
) {
    @PostMapping("/sign-up/email")
    suspend fun signUp(@RequestBody request: SignUpRequest): SignUpResponse {
        validateSignUp(request)
        authService.register(request.toCommand())
        return SignUpResponse(success = true)
    }

    @PostMapping("/sign-in/email")
    suspend fun signIn(@RequestBody request: SignInWithPasswordRequest): SignInWithPasswordResponse {
        validateSignIn(request)
        val result = authService.login(request.toCommand())
        return SignInWithPasswordResponse(
            user = result.toUserProfile(),
            accessToken = result.tokens.accessToken,
            refreshToken = result.tokens.refreshToken,
            expiresIn = result.tokens.expiresIn,
        )
    }

    @PostMapping("/email-otp/send")
    suspend fun sendOtp(@RequestBody request: SendOtpRequest): OtpSendResponse {
        otpService.sendOtp(Email(request.email), OtpType.valueOf(request.type))
        return OtpSendResponse(success = true)
    }

    @PostMapping("/email-otp/verify-email")
    suspend fun verifyEmail(@RequestBody request: VerifyOTPRequest): VerifyOTPResponse {
        val email = Email(request.email)
        otpService.verifyOtp(email, OtpType.VERIFY_EMAIL, request.otp)
        authService.verifyEmail(email)
        return VerifyOTPResponse(success = true)
    }

    @PostMapping("/email-otp/reset-password")
    suspend fun resetPassword(@RequestBody request: ResetPasswordRequest): ResetPasswordResponse {
        val email = Email(request.email)
        otpService.verifyOtp(email, OtpType.RESET_PASSWORD, request.otp)
        if (request.password.length < 6) {
            throw ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Password too short")
        }
        authService.resetPassword(request.toCommand())
        return ResetPasswordResponse(success = true)
    }

    @PostMapping("/refresh")
    suspend fun refresh(@RequestBody request: RefreshTokenRequest): SignInWithPasswordResponse {
        val result = authService.refreshToken(request.refreshToken)
        return SignInWithPasswordResponse(
            user = result.toUserProfile(),
            accessToken = result.tokens.accessToken,
            refreshToken = result.tokens.refreshToken,
            expiresIn = result.tokens.expiresIn,
        )
    }

    @PostMapping("/sign-out")
    suspend fun signOut(@RequestBody(required = false) request: RefreshTokenRequest?): ResponseEntity<Void> {
        if (request != null) {
            authService.logout(request.refreshToken)
        }
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me")
    suspend fun me(authentication: Authentication): UserProfile {
        val userId = requireUserId(authentication)
        val user = authService.getProfile(userId)
        return user.toUserProfile()
    }

    @PostMapping("/password/change")
    suspend fun changePassword(
        authentication: Authentication,
        @RequestBody request: ChangePasswordRequest,
    ): ChangePasswordResponse {
        val userId = requireUserId(authentication)
        if (request.newPassword.length < 6) {
            throw ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Password too short")
        }
        authService.changePassword(userId, request.oldPassword, request.newPassword)
        return ChangePasswordResponse(success = true)
    }

    @PostMapping("/email-otp/resend-verification")
    suspend fun resendVerification(authentication: Authentication): OtpSendResponse {
        val userId = requireUserId(authentication)
        val user = authService.getProfile(userId)
        val email = user.email ?: throw ApiException(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Email not set",
        )
        if (user.isEmailVerified) {
            throw ApiException(HttpStatus.CONFLICT, "INVALID_STATE", "Email already verified")
        }
        otpService.sendOtp(email, OtpType.VERIFY_EMAIL)
        return OtpSendResponse(success = true)
    }

    private fun requireUserId(authentication: Authentication): UserId {
        val subject = authentication.principal as? String
            ?: throw ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing authentication")
        val userId = runCatching { UUID.fromString(subject) }.getOrNull()
            ?: throw ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid subject")
        return UserId(userId)
    }

    private fun validateSignUp(request: SignUpRequest) {
        if (request.email.isBlank() || !request.email.contains("@")) {
            throw ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid email")
        }
        if (request.password.length < 6) {
            throw ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Password too short")
        }
        if (request.name.isBlank()) {
            throw ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Name is required")
        }
    }

    private fun validateSignIn(request: SignInWithPasswordRequest) {
        if (request.email.isBlank() || !request.email.contains("@")) {
            throw ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid email")
        }
        if (request.password.isBlank()) {
            throw ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Password is required")
        }
    }
}
