package tech.hanasaki.azusa.modules.auth.domain.model

import tech.hanasaki.azusa.modules.auth.domain.event.EmailVerified
import tech.hanasaki.azusa.modules.auth.domain.event.PasswordChanged
import tech.hanasaki.azusa.modules.auth.domain.event.UserRegistered
import tech.hanasaki.azusa.shared.domain.model.base.AggregateRoot
import tech.hanasaki.azusa.shared.domain.model.vo.AvatarUrl
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Clock
import kotlin.time.Instant


@JvmInline
value class HashedPassword(val value: String)

@JvmInline
value class PlainPassword(val value: String) {
    init {
        require(value.length >= 8) { "密码长度不能小于8位" }
        require(value.isNotBlank()) { "密码不能为空" }
    }
}

@JvmInline
value class Username(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "用户名不能为空" }
        require(value.length in 2..20) { "用户名长度非法(2 ~ 20)" }
    }
}


data class UserProfile(
    val userId: UserId,
    val username: Username,
    val avatar: AvatarUrl?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class UserStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    DISABLED,
    BANNED,
}

class User private constructor(
    val id: UserId,
    hashedPassword: HashedPassword,
    profile: UserProfile,
    status: UserStatus,
    role: UserRole,
    email: Email?,
    emailVerified: Boolean,
    bannedUntil: Instant?,
) : AggregateRoot() {
    companion object {
        fun create(
            email: Email,
            hashedPassword: HashedPassword,
            username: Username,
            avatar: AvatarUrl? = null,
            now: Instant = Clock.System.now(),
        ): User {
            val id = UserId.generate()
            val user = User(
                id = id,
                hashedPassword = hashedPassword,
                status = UserStatus.PENDING,
                role = UserRole.USER,
                email = email,
                emailVerified = false,
                bannedUntil = null,
                profile = UserProfile(id, username, avatar, now, now)
            )
            user.addDomainEvent(UserRegistered(userId = user.id, email = user.email!!))
            return user
        }

        fun reconstitute(
            id: UserId,
            hashedPassword: HashedPassword,
            profile: UserProfile,
            status: UserStatus,
            role: UserRole,
            email: Email?,
            emailVerified: Boolean,
            bannedUntil: Instant?,
        ): User = User(
            id = id,
            hashedPassword = hashedPassword,
            profile = profile,
            status = status,
            role = role,
            email = email,
            emailVerified = emailVerified,
            bannedUntil = bannedUntil
        )
    }

    var hashedPassword: HashedPassword = hashedPassword
        private set

    var profile: UserProfile = profile
        private set

    var status: UserStatus = status
        private set

    var role: UserRole = role
        private set

    var email: Email? = email
        private set

    var isEmailVerified: Boolean = emailVerified
        private set

    var bannedUntil: Instant? = bannedUntil
        private set

    fun canSignIn(now: Instant = Clock.System.now()): Boolean =
        status == UserStatus.ACTIVE &&
                !isBanned(now) &&
                (email == null || isEmailVerified)

    fun verifyEmail() {
        checkNotNull(email) { "无法验证邮箱： 邮箱未设置" }
        if (isEmailVerified) return

        isEmailVerified = true
        if (status == UserStatus.PENDING) {
            status = UserStatus.ACTIVE
        }
        addDomainEvent(EmailVerified(id, email = email!!))
    }

    fun changePassword(newPasswordHash: HashedPassword) {
        hashedPassword = newPasswordHash
        addDomainEvent(PasswordChanged(id, email = email))
    }

    fun suspend() {
        require(status == UserStatus.ACTIVE) { "Only active accounts can be suspended" }
        status = UserStatus.SUSPENDED
    }

    fun ban(until: Instant?) {
        status = UserStatus.BANNED
        bannedUntil = until
    }

    fun liftBan() {
        status = UserStatus.ACTIVE
        bannedUntil = null
    }

    fun updateAvatar(avatar: AvatarUrl?) {
        profile = profile.copy(avatar = avatar, updatedAt = Clock.System.now())
    }

    fun isBanned(now: Instant = Clock.System.now()): Boolean =
        status == UserStatus.BANNED || (bannedUntil?.let { now < it } ?: false)
}
