package tech.hanasaki.momotalk_plus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String? = null,
    val picture: String? = null,
    val username: String? = null,
    val email: String? = null,
    @SerialName("email_verified")
    val emailVerified: Boolean? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    val providers: List<UserProfileProvider> = emptyList(),
    val gender: String? = null,
    val birthdate: String? = null,
    val zoneinfo: String? = null,
    val locale: String? = null,
    @SerialName("created_from")
    val createdFrom: String? = null,
    val sub: String? = null,
    val uid: String? = null,
    val address: UserAddress? = null,
    @SerialName("last_login_at")
    val nickName: String? = null,
    val province: String? = null,
    val country: String? = null,
    val city: String? = null,
)

@Serializable
data class UserProfileProvider(
    @SerialName("provider_id")
    val providerId: String,
    @SerialName("federated_id")
    val federatedId: String,
    @SerialName("raw_id")
    val displayName: String? = null,
    @SerialName("photo_url")
    val photoUrl: String? = null,
    val email: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
)

@Serializable
data class UserAddress(
    val formatted: String? = null,
    @SerialName("street_address")
    val streetAddress: String? = null,
    val locality: String? = null,
    val region: String? = null,
    @SerialName("postal_code")
    val postalCode: String? = null,
    val country: String? = null,
)

