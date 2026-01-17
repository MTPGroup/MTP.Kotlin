package tech.hanasaki.azusa.auth.domain.model

import kotlinx.datetime.Instant


@JvmInline
value class Username(
    val value: String,
) {
    init {
        require(value.length in 2..20) { "用户名长度非法(2 ~ 20)" }
    }
}

@JvmInline
value class AvatarUrl(
    val value: String,
) {
    init {
        require(value.startsWith("http://") || value.startsWith("https://")) { "非法URL" }
    }
}


class UserProfile(
    val userId: UserId,
    val username: Username,
    val avatar: AvatarUrl?,
    val createdAt: Instant,
    val updatedAt: Instant,
)