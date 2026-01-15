package tech.hanasaki.azusa.modules.auth.domain.model

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.auth.domain.events.EmailVerifiedEvent
import tech.hanasaki.azusa.modules.auth.domain.events.UserRegisteredEvent
import tech.hanasaki.azusa.shared.domain.base.AggregateRoot
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

@JvmInline
value class UserId(val value: UUID)

@JvmInline
value class PasswordHash(val value: String)

@JvmInline
value class Email(val value: String) {
    init {
        require(value.contains('@')) { "Invalid email format" }
    }
}

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
            val id = UserId(UUID.randomUUID())
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

    /*fun updateProfile(username: Username, avatarUrl: AvatarUrl?) {
        _profile = UserProfile(
            userId = id,
            username = username,
            avatar = avatarUrl,
        )
    }*/

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

    fun attachEmail(newEmail: Email) {
        _email = newEmail
        _emailVerified = false
        _status = UserStatus.PENDING
    }

    fun activate() {
        _status = UserStatus.ACTIVE
    }

    fun suspend() {
        require(_status == UserStatus.ACTIVE) { "Only active accounts can be suspended" }
        _status = UserStatus.SUSPENDED
    }

    fun restore() {
        require(_status == UserStatus.SUSPENDED) { "Only suspended accounts can be restored" }
        _status = UserStatus.ACTIVE
    }

    fun disable() {
        _status = UserStatus.DISABLED
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
