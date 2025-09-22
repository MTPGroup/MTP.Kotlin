package tech.hanasaki.momotalk_plus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val name: String,
    val image: String?,
    val emailVerified: Boolean,
    val createdAt: String,
    val updatedAt: String,
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

