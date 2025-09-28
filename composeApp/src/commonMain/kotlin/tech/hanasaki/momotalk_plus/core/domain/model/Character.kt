package tech.hanasaki.momotalk_plus.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Visibility {
    @SerialName("public")
    PUBLIC,

    @SerialName("private")
    PRIVATE,
}

@Serializable
data class Creator(
    val id: String,
    val name: String,
    val image: String?,
    val username: String?,
)

@Serializable
data class Character(
    val id: String,
    val creatorId: String,
    val name: String,
    val signature: String,
    val persona: String,
    val avatarUrl: String,
    val visibility: Visibility,
    val createdAt: String,
    val updatedAt: String,
    val creator: Creator,
) {
    companion object {
        fun default(): Character = Character(
            id = "",
            creatorId = "",
            name = "",
            signature = "",
            persona = "",
            avatarUrl = "",
            visibility = Visibility.PUBLIC,
            createdAt = "",
            updatedAt = "",
            creator = Creator(
                id = "",
                name = "",
                image = "",
                username = "",
            ),
        )
    }
}