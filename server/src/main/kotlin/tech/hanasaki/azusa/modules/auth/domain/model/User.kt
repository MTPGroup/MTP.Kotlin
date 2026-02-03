package tech.hanasaki.azusa.modules.auth.domain.model

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.common.kernel.base.AggregateRoot
import tech.hanasaki.azusa.common.kernel.model.AvatarUrl
import tech.hanasaki.azusa.common.kernel.model.Email
import tech.hanasaki.azusa.common.kernel.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.event.EmailVerifiedEvent
import tech.hanasaki.azusa.modules.auth.domain.event.UserRegisteredEvent
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@JvmInline
value class PasswordHash(val value: String)

@Serializable
enum class UserStatus(val value: String) {
    PENDING("pending"),
    ACTIVE("active"),
    SUSPENDED("suspended"),
    DISABLED("disabled"),
    BANNED("banned"),
}

class User(
    val id: UserId,
    private var _passwordHash: PasswordHash,
    private var _profile: UserProfile,
    private var _status: UserStatus,
    private var _email: Email?,
    private var _emailVerified: Boolean,
    private var _bannedUntil: Instant?,
) : AggregateRoot() {
    companion object {
        fun register(
            email: Email,
            hashedPassword: PasswordHash,
            username: Username,
            avatar: AvatarUrl? = null,
        ): User {

            val id = UserId(Uuid.random())
            val now = Clock.System.now()
            val user = User(
                id = id,
                _passwordHash = hashedPassword,
                _status = UserStatus.PENDING,
                _email = email,
                _emailVerified = false,
                _bannedUntil = null,
                _profile = UserProfile(id, username, avatar, now, now)
            )
            user.addDomainEvent(UserRegisteredEvent(user.id, user.email!!))
            return user
        }
    }

    val passwordHash: PasswordHash
        get() = _passwordHash

    val profile: UserProfile
        get() = _profile

    val status: UserStatus
        get() = _status

    val email: Email?
        get() = _email

    val isEmailVerified: Boolean
        get() = _emailVerified

    val bannedUntilAt: Instant?
        get() = _bannedUntil

    fun canSignIn(now: Instant = Clock.System.now()): Boolean =
        _status == UserStatus.ACTIVE &&
                !isBanned(now) &&
                (_email == null || _emailVerified)

    fun verifyEmail() {
        require(_email != null) { "Email not set" }
        _emailVerified = true
        if (_status == UserStatus.PENDING) {
            _status = UserStatus.ACTIVE
        }
        addDomainEvent(EmailVerifiedEvent(id, _email!!))
    }

    fun changePassword(newPasswordHash: PasswordHash) {
        _passwordHash = newPasswordHash
    }

    fun suspend() {
        require(_status == UserStatus.ACTIVE) { "Only active accounts can be suspended" }
        _status = UserStatus.SUSPENDED
    }

    fun ban(until: Instant?) {
        _status = UserStatus.BANNED
        _bannedUntil = until
    }

    fun liftBan() {
        _status = UserStatus.ACTIVE
        _bannedUntil = null
    }

    fun isBanned(now: Instant = Clock.System.now()): Boolean =
        _status == UserStatus.BANNED || (_bannedUntil?.let { now < it } ?: false)
}
