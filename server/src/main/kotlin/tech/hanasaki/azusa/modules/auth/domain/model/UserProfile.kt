package tech.hanasaki.azusa.modules.auth.domain.model

import tech.hanasaki.azusa.common.kernel.model.AvatarUrl
import tech.hanasaki.azusa.common.kernel.model.UserId
import kotlin.time.Instant


@JvmInline
value class Username(
    val value: String,
) {
    init {
        require(value.length in 2..20) { "用户名长度非法(2 ~ 20)" }
    }
}


class UserProfile(
    val userId: UserId,
    val username: Username,
    val avatar: AvatarUrl?,
    val createdAt: Instant,
    val updatedAt: Instant,
)