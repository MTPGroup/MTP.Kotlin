package tech.hanasaki.azusa.modules.auth.domain.model

import tech.hanasaki.azusa.common.domain.model.AggregateRoot
import tech.hanasaki.azusa.common.domain.model.AvatarUrl
import tech.hanasaki.azusa.common.domain.model.Email
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.event.EmailVerified
import tech.hanasaki.azusa.modules.auth.domain.event.UserRegistered
import kotlin.time.Clock
import kotlin.time.Instant


@JvmInline
value class PasswordHash(val value: String)

@JvmInline
value class Username(
    val value: String,
) {
    init {
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

class User(
    val id: UserId,
    passwordHash: PasswordHash,
    profile: UserProfile,
    status: UserStatus,
    email: Email?,
    emailVerified: Boolean,
    bannedUntil: Instant?,
) : AggregateRoot() {
    companion object {
        fun create(
            email: Email,
            hashedPassword: PasswordHash,
            username: Username,
            avatar: AvatarUrl? = null,
            now: Instant = Clock.System.now(),
        ): User {
            val id = UserId.generate()
            val user = User(
                id = id,
                passwordHash = hashedPassword,
                status = UserStatus.PENDING,
                email = email,
                emailVerified = false,
                bannedUntil = null,
                profile = UserProfile(id, username, avatar, now, now)
            )
            user.addDomainEvent(UserRegistered(userId = user.id, email = user.email!!))
            return user
        }
    }

    var passwordHash: PasswordHash = passwordHash
        private set

    var profile: UserProfile = profile
        private set

    var status: UserStatus = status
        private set

    var email: Email? = email
        private set

    var emailVerified: Boolean = emailVerified
        private set

    var bannedUntil: Instant? = bannedUntil
        private set

    fun canSignIn(now: Instant = Clock.System.now()): Boolean =
        status == UserStatus.ACTIVE &&
                !isBanned(now) &&
                (email == null || emailVerified)

    fun verifyEmail() {
        checkNotNull(email) { "无法验证邮箱： 邮箱未设置" }
        if (emailVerified) return

        emailVerified = true
        if (status == UserStatus.PENDING) {
            status = UserStatus.ACTIVE
        }
        addDomainEvent(EmailVerified(id, email = email!!))
    }

    fun changePassword(newPasswordHash: PasswordHash) {
        passwordHash = newPasswordHash
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

    fun isBanned(now: Instant = Clock.System.now()): Boolean =
        status == UserStatus.BANNED || (bannedUntil?.let { now < it } ?: false)
}
