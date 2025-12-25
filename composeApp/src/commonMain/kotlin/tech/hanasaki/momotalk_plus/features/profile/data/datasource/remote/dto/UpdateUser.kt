package tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val username: String? = null,
    val avatar: String? = null,
)

@Serializable
data class UpdateUserResponse(
    val status: Boolean,
)