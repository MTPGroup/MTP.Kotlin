package tech.hanasaki.momotalk_plus.features.login.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SignInWithPasswordRequest(
    val username: String,
    val password: String,
    @SerialName("verification_token")
    val verificationToken: String? = null,
)

@Serializable
data class SignInWithPasswordResponse(
    val sub: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
)

@Serializable
data class SignUpRequest(
    val email: String?,
    val phoneNumber: String?,
    val username: String,
    val password: String,
    @SerialName("verification_token")
    val verificationToken: String,
)

@Serializable
data class SignUpResponse(
    val sub: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
)

@Serializable
data class SendVerificationCodeRequest(
    val email: String?,
    @SerialName("phone_number")
    val phoneNumber: String?,
    val target: String = "ANY",
)

@Serializable
data class SendVerificationCodeResponse(
    @SerialName("verification_id")
    val verificationId: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("is_user")
    val isUser: Boolean? = null,
)

@Serializable
data class VerifyCodeRequest(
    @SerialName("verification_id")
    val verificationId: String,
    @SerialName("verification_code")
    val verificationCode: String,
)

@Serializable
data class VerifyCodeResponse(
    @SerialName("verification_token")
    val verificationToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
)

@Serializable
data class CaptchaResponse(
    val token: String,
    val data: String,
    @SerialName("expires_in")
    val expiresIn: Long
)

@Serializable
data class CaptchaIdRequest(
    val token: String,
    val key: String,
)

@Serializable
data class CaptchaIdResponse(
    @SerialName("captcha_token")
    val captchaToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
)

@Serializable
data class ResetPasswordRequest(
    val email: String?,
    @SerialName("phone_number")
    val phoneNumber: String?,
    @SerialName("new_password")
    val newPassword: String?,
    @SerialName("verification_token")
    val verificationToken: String,
)
