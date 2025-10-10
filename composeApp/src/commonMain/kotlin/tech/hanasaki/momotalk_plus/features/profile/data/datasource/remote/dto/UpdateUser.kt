package tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.dto

data class UpdateUserRequest(
    val name: String,
    val image: String? = null,
)

data class UpdateUserResponse(
    val status: Boolean,
)