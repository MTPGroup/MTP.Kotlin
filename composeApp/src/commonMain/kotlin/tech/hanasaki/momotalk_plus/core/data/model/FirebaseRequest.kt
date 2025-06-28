package tech.hanasaki.momotalk_plus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.features.login.data.model.UserInfo

@Serializable
data class RefreshIdTokenResponse(
    @SerialName("id_token")
    val idToken: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("project_id")
    val projectId: String,
)

@Serializable
data class FirebaseErrorResponse(
    val error: FirebaseErrorDetail
)

@Serializable
data class FirebaseErrorDetail(
    val code: Int,
    val message: String,
    val errors: List<FirebaseErrorObject>? = null,
)

@Serializable
data class FirebaseErrorObject(
    val message: String,
    val domain: String,
    val reason: String,
)

@Serializable
data class SetAccountInfoRequest(
    val idToken: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val deleteAttribute: List<String>? = null,
    val returnSecureToken: Boolean = true,
)

@Serializable
data class SetAccountInfoResponse(
    val localId: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
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
data class GetAccountInfoRequest(
    val idToken: String,
)

@Serializable
data class GetAccountInfoResponse(
    val users: List<UserInfo>,
)
