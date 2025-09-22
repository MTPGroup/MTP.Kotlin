package tech.hanasaki.momotalk_plus.features.auth.data.model

import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType


@Serializable
data class SignInWithPasswordRequest(
    val email: String,
    val password: String,
)

@Serializable
data class SignInWithPasswordResponse(
    val redirect: Boolean,
    val token: String,
    val user: UserProfile,
)

@Serializable
data class SignUpRequest(
    val email: String,
    val name: String,
    val password: String,
    val callbackURL: String,
)

@Serializable
data class SignUpResponse(
    val token: String,
    val user: UserProfile,
)

@Serializable
data object SignOutRequest

@Serializable
data class SignOutResponse(
    val success: Boolean,
)

@Serializable
data class SendEmailVerificationRequest(
    val email: String,
    val type: OTPType
)

@Serializable
data class SendEmailVerificationResponse(
    val success: Boolean,
)

@Serializable
data class SendPasswordResetEmailRequest(
    val email: String,
)

@Serializable
data class SendPasswordResetEmailResponse(
    val success: Boolean,
)

@Serializable
data class VerifyOTPRequest(
    val email: String,
    val otp: String,
)

@Serializable
data class VerifyOTPResponse(
    val token: String,
    val user: UserProfile,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val password: String,
)

@Serializable
data class ResetPasswordResponse(
    val success: Boolean,
)