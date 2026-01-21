package tech.hanasaki.azusa.modules.auth.application.service

import tech.hanasaki.azusa.modules.auth.application.command.LoginCommand
import tech.hanasaki.azusa.modules.auth.application.command.RegisterCommand
import tech.hanasaki.azusa.modules.auth.application.command.ResetPasswordCommand
import tech.hanasaki.azusa.modules.auth.application.port.PasswordEncoder
import tech.hanasaki.azusa.modules.auth.application.port.TokenPair
import tech.hanasaki.azusa.modules.auth.application.port.TokenService
import tech.hanasaki.azusa.modules.auth.application.result.LoginResult
import tech.hanasaki.azusa.modules.auth.domain.model.*
import tech.hanasaki.azusa.modules.auth.domain.repository.RefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.shared.domain.event.EventPublisher
import tech.hanasaki.azusa.shared.domain.exception.AuthenticationException
import tech.hanasaki.azusa.shared.domain.exception.ConflictException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.UserId
import java.security.MessageDigest

class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService,
    private val eventPublisher: EventPublisher,
) {

    /**
     * 用户注册
     */
    suspend fun register(cmd: RegisterCommand) {
        val email = cmd.email

        if (userRepository.findByEmail(email) != null) {
            throw ConflictException("Email already in use")
        }

        val hashedPassword = passwordEncoder.encode(cmd.password)

        val user = User.register(
            email = cmd.email,
            hashedPassword = PasswordHash(hashedPassword),
            username = cmd.username,
        )

        userRepository.save(user)

        eventPublisher.publishAll(user.domainEvents)
        user.clearDomainEvents()
    }

    /**
     * 用户登录
     */
    suspend fun login(cmd: LoginCommand): LoginResult {

        val user = userRepository.findByEmail(cmd.email)
            ?: throw NotFoundException("User not found")

        if (!passwordEncoder.matches(cmd.password, user.passwordHash.value)) {
            throw AuthenticationException("Invalid credentials")
        }

        if (!user.canSignIn()) {
            when (user.status) {
                UserStatus.BANNED -> throw AuthenticationException("Account banned until ${user.bannedUntilAt}")
                UserStatus.PENDING -> throw AuthenticationException("Email not verified")
                else -> throw AuthenticationException("Account is disabled or suspended")
            }
        }

        val tokens = tokenService.generateTokens(user.id, user.email!!)

        val refreshToken = RefreshToken(
            userId = user.id,
            tokenHash = hashToken(tokens.refreshToken),
            expiresAt = tokens.refreshTokenExpiresAt,
        )
        refreshTokenRepository.save(refreshToken)

        userRepository.save(user)

        return createLoginResult(user, tokens)
    }

    /**
     * 用户登出
     */
    suspend fun logout(refreshToken: String) {
        val tokenHash = hashToken(refreshToken)
        val storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
        if (storedToken != null) {
            refreshTokenRepository.revoke(storedToken)
        }
    }

    /**
     * 邮箱验证
     */
    suspend fun verifyEmail(email: Email) {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundException("User not found")

        user.verifyEmail()

        userRepository.save(user)
        eventPublisher.publishAll(user.domainEvents)
        user.clearDomainEvents()
    }

    /**
     * 重置密码
     */
    suspend fun resetPassword(cmd: ResetPasswordCommand) {
        val user = userRepository.findByEmail(cmd.email)
            ?: throw NotFoundException("User not found")

        val hashedPassword = passwordEncoder.encode(cmd.newPassword)
        user.changePassword(PasswordHash(hashedPassword))

        userRepository.save(user)
    }

    /**
     * 修改密码
     */
    suspend fun changePassword(userId: UserId, oldPassword: String, newPassword: String) {
        val user = userRepository.findById(userId) ?: throw NotFoundException("User not found")

        if (!passwordEncoder.matches(oldPassword, user.passwordHash.value)) {
            throw AuthenticationException("Invalid credentials")
        }

        val hashedPassword = passwordEncoder.encode(newPassword)
        user.changePassword(PasswordHash(hashedPassword))

        userRepository.save(user)
    }

    /**
     * 获取用户信息
     */
    suspend fun getProfile(userId: UserId): User {
        return userRepository.findById(userId) ?: throw NotFoundException("User not found")
    }


    /**
     * 使用 RefreshToken 刷新 AccessToken
     */
    suspend fun refreshToken(refreshToken: String): LoginResult {
        tokenService.verifyRefreshToken(refreshToken)

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

        val tokens = tokenService.generateTokens(user.id, user.email!!)

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
        isEmailVerified = user.isEmailVerified,
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