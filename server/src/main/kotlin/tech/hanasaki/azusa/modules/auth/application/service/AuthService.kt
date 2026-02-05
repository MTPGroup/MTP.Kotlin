package tech.hanasaki.azusa.modules.auth.application.service

import tech.hanasaki.azusa.common.domain.exception.AuthenticationException
import tech.hanasaki.azusa.common.domain.exception.ConflictException
import tech.hanasaki.azusa.common.domain.exception.NotFoundException
import tech.hanasaki.azusa.common.domain.model.Email
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.common.port.out.OutboxScheduler
import tech.hanasaki.azusa.common.port.out.TransactionalPort
import tech.hanasaki.azusa.modules.auth.application.command.LoginCommand
import tech.hanasaki.azusa.modules.auth.application.command.RegisterCommand
import tech.hanasaki.azusa.modules.auth.application.command.ResetPasswordCommand
import tech.hanasaki.azusa.modules.auth.application.port.`in`.AuthUseCase
import tech.hanasaki.azusa.modules.auth.application.port.`in`.TokenVerifier
import tech.hanasaki.azusa.modules.auth.application.port.out.PasswordEncoder
import tech.hanasaki.azusa.modules.auth.application.port.out.RefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.application.port.out.TokenGenerator
import tech.hanasaki.azusa.modules.auth.application.port.out.UserRepository
import tech.hanasaki.azusa.modules.auth.application.result.LoginResult
import tech.hanasaki.azusa.modules.auth.application.result.TokenPair
import tech.hanasaki.azusa.modules.auth.domain.model.PasswordHash
import tech.hanasaki.azusa.modules.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.modules.auth.domain.model.User
import tech.hanasaki.azusa.modules.auth.domain.model.UserStatus
import java.security.MessageDigest

class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenGenerator: TokenGenerator,
    private val tokenVerifier: TokenVerifier,
    private val outboxScheduler: OutboxScheduler,
    private val tx: TransactionalPort,
) : AuthUseCase {

    override suspend fun register(cmd: RegisterCommand) {
        tx.execute {
            val email = cmd.email

            if (userRepository.findByEmail(email) != null) {
                throw ConflictException("该邮箱已被注册")
            }

            val hashedPassword = passwordEncoder.encode(cmd.password)

            val user = User.create(
                email = cmd.email,
                hashedPassword = PasswordHash(hashedPassword),
                username = cmd.username,
            )

            userRepository.save(user)

            user.domainEvents.forEach { event ->
                outboxScheduler.schedule(event)
            }
//            eventBus.publishAll(user.domainEvents)
            user.clearDomainEvents()
        }
    }

    override suspend fun login(cmd: LoginCommand): LoginResult {
        val user = userRepository.findByEmail(cmd.email)
            ?: throw NotFoundException("User not found")

        if (!passwordEncoder.matches(cmd.password, user.passwordHash.value)) {
            throw AuthenticationException("Invalid credentials")
        }

        if (!user.canSignIn()) {
            when (user.status) {
                UserStatus.BANNED -> throw AuthenticationException("Account banned until ${user.bannedUntil}")
                UserStatus.PENDING -> throw AuthenticationException("Email not verified")
                else -> throw AuthenticationException("Account is disabled or suspended")
            }
        }

        val tokens = tokenGenerator.generate(user.id, user.email!!)

        val refreshToken = RefreshToken(
            userId = user.id,
            tokenHash = hashToken(tokens.refreshToken),
            expiresAt = tokens.refreshTokenExpiresAt,
        )
        refreshTokenRepository.save(refreshToken)

        userRepository.save(user)

        return createLoginResult(user, tokens)
    }

    override suspend fun logout(refreshToken: String) {
        val tokenHash = hashToken(refreshToken)
        val storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
        if (storedToken != null) {
            refreshTokenRepository.revoke(storedToken)
        }
    }

    override suspend fun deleteAccount(userId: UserId) {
        val user = userRepository.findById(userId)
            ?: throw NotFoundException("User not found")
        userRepository.deleteById(user.id)
    }

    override suspend fun verifyEmail(email: Email) {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundException("User not found")

        user.verifyEmail()

        userRepository.save(user)
    }

    override suspend fun resetPassword(cmd: ResetPasswordCommand) {
        val user = userRepository.findByEmail(cmd.email)
            ?: throw NotFoundException("User not found")

        val hashedPassword = passwordEncoder.encode(cmd.newPassword)
        user.changePassword(PasswordHash(hashedPassword))

        userRepository.save(user)
    }

    override suspend fun changePassword(userId: UserId, oldPassword: String, newPassword: String) {
        val user = userRepository.findById(userId) ?: throw NotFoundException("User not found")

        if (!passwordEncoder.matches(oldPassword, user.passwordHash.value)) {
            throw AuthenticationException("Invalid credentials")
        }

        val hashedPassword = passwordEncoder.encode(newPassword)
        user.changePassword(PasswordHash(hashedPassword))

        userRepository.save(user)
    }

    override suspend fun getProfile(userId: UserId): User {
        return userRepository.findById(userId) ?: throw NotFoundException("User not found")
    }

    override suspend fun refreshToken(refreshToken: String): LoginResult {
        tokenVerifier.verify(refreshToken)

        val tokenHash = hashToken(refreshToken)
        val storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
            ?: throw AuthenticationException("Refresh token not found")

        if (!storedToken.isValid()) {
            throw AuthenticationException("Refresh token is expired or has been revoked")
        }
        refreshTokenRepository.revoke(storedToken)

        val user = userRepository.findById(storedToken.userId)
            ?: throw NotFoundException("User not found")

        if (!user.canSignIn()) {
            throw AuthenticationException("Account is disabled or suspended")
        }

        val tokens = tokenGenerator.generate(user.id, user.email!!)

        val newRefreshToken = RefreshToken(
            userId = user.id,
            tokenHash = hashToken(tokens.refreshToken),
            expiresAt = tokens.refreshTokenExpiresAt,
        )
        refreshTokenRepository.save(newRefreshToken)

        userRepository.save(user)

        return createLoginResult(user, tokens)
    }

    private fun createLoginResult(user: User, tokens: TokenPair) = LoginResult(
        userId = user.id,
        username = user.profile.username,
        email = user.email!!,
        isEmailVerified = user.emailVerified,
        avatar = user.profile.avatar,
        createdAt = user.profile.createdAt,
        updatedAt = user.profile.updatedAt,
        tokens = tokens,
    )

    private fun hashToken(token: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}