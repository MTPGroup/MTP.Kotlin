package tech.hanasaki.momotalk_plus.features.login.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SignInWithPasswordRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true,
)

@Serializable
data class SignInWithPasswordResponse(
    val idToken: String,
    val email: String,
    val refreshToken: String,
    val expiresIn: String,
    val localId: String,
    val displayName: String = "",
    val registered: Boolean,
)

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true,
)

@Serializable
data class SignUpResponse(
    val idToken: String,
    val email: String,
    val refreshToken: String,
    val expiresIn: String,
    val localId: String,
)

@Serializable
data class SendResetPasswordEmailRequest(
    val email: String,
    val requestType: String = "PASSWORD_RESET",
)

@Serializable
data class SendResetPasswordEmailResponse(
    val email: String,
)

@Serializable
data class VerifyResetPasswordCodeRequest(
    val oobCode: String,
)

@Serializable
data class VerifyResetPasswordCodeResponse(
    val email: String,
    val requestType: String = "PASSWORD_RESET",
)

@Serializable
data class ResetPasswordRequest(
    val oobCode: String,
    val newPassword: String,
)

@Serializable
data class ResetPasswordResponse(
    val email: String,
    val requestType: String = "PASSWORD_RESET",
)

@Serializable
data class ChangeEmailRequest(
    val idToken: String,
    val email: String,
    val returnSecureToken: Boolean = true,
)

@Serializable
data class ChangeEmailResponse(
    val localId: String,
    val email: String,
    val passwordHash: String,
    val providerUserInfo: List<ProviderUserInfo>,
    val idToken: String,
    val refreshToken: String,
    val expiresIn: String,
)

@Serializable
data class ProviderUserInfo(
    val providerId: String,
    val federatedId: String,
)

@Serializable
data class ChangePasswordRequest(
    val idToken: String,
    val password: String,
    val returnSecureToken: Boolean = true,
)

@Serializable
data class ChangePasswordResponse(
    val localId: String,
    val email: String,
    val passwordHash: String,
    val providerUserInfo: List<ProviderUserInfo>,
    val idToken: String,
    val refreshToken: String,
    val expiresIn: String,
)


@Serializable
data class UserInfo(
    val localId: String,
    val email: String,
    val emailVerified: Boolean,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val passwordHash: String? = null,
    val passwordUpdatedAt: Double? = null,
    val validSince: String? = null,
    val disabled: Boolean = false,
    val lastLoginAt: String? = null,
    val createdAt: String? = null,
    val customAuth: Boolean = false,
)

@Serializable
data class LinkEmailAndPasswordRequest(
    val idToken: String,
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true,
)

@Serializable
data class LinkEmailAndPasswordResponse(
    val localId: String,
    val email: String,
    val displayName: String,
    val photoUrl: String,
    val passwordHash: String,
    val providerUserInfo: List<ProviderUserInfo>,
    val emailVerified: Boolean,
    val idToken: String,
    val refreshToken: String,
    val expiresIn: String,
)

@Serializable
data class SendEmailVerificationRequest(
    val idToken: String,
    val requestType: String = "VERIFY_EMAIL",
)

@Serializable
data class SendEmailVerificationResponse(
    val email: String,
)

@Serializable
data class ConfirmEmailVerificationRequest(
    val oobCode: String,
)

@Serializable
data class ConfirmEmailVerificationResponse(
    val email: String,
    val displayName: String,
    val photoUrl: String,
    val passwordHash: String,
    val providerUserInfo: List<ProviderUserInfo>,
    val emailVerified: Boolean
)

@Serializable
data class DeleteAccountRequest(
    val idToken: String,
)
