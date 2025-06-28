package tech.hanasaki.momotalk_plus.core.domain.model


data class RefreshInfo(
    val uid: String,
    val refreshToken: String,
    val idToken: String,
    val expiresIn: Long,
)
