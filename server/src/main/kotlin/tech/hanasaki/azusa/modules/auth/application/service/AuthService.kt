package tech.hanasaki.azusa.modules.auth.application.service

import tech.hanasaki.azusa.modules.auth.application.dto.AuthenticatedUser
import tech.hanasaki.azusa.modules.auth.application.dto.UserProfileDto
import tech.hanasaki.azusa.modules.auth.application.dto.toUserProfileDto
import tech.hanasaki.azusa.modules.auth.application.port.`in`.AuthUseCasePort
import tech.hanasaki.azusa.modules.auth.application.port.out.PasswordEncoderPort
import tech.hanasaki.azusa.modules.auth.application.port.out.TokenServicePort
import tech.hanasaki.azusa.modules.auth.domain.model.*
import tech.hanasaki.azusa.modules.auth.domain.port.RefreshTokenRepositoryPort
import tech.hanasaki.azusa.modules.auth.domain.port.UserRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.AuthenticationException
import tech.hanasaki.azusa.shared.domain.exception.ConflictException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.base.publishAndClear
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.DomainEventBusPort
import tech.hanasaki.azusa.shared.port.out.StringEncoderPort
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class AuthService(
    private val userRepository: UserRepositoryPort,
    private val refreshTokenRepository: RefreshTokenRepositoryPort,
    private val passwordEncoder: PasswordEncoderPort,
    private val tokenService: TokenServicePort,
    private val encoder: StringEncoderPort,
    private val domainEventBus: DomainEventBusPort,
    private val tx: TransactionalPort,
) : AuthUseCasePort {

    override suspend fun register(
        email: Email,
        password: PlainPassword,
        username: Username,
    ) = tx.execute {
        val email = email

        if (userRepository.findByEmail(email) != null) {
            throw ConflictException("邮箱已被注册")
        }

        val user = User.create(
            email = email,
            hashedPassword = passwordEncoder.encode(password),
            username = username,
        )

        userRepository.save(user)
        user.publishAndClear(domainEventBus)
    }

    override suspend fun login(
        email: Email,
        password: PlainPassword,
    ): AuthenticatedUser = tx.execute {
        val user = userRepository.findByEmail(email)

        if (user == null || !passwordEncoder.matches(password, user.hashedPassword)) {
            throw NotFoundException("账号或密码错误")
        }

        if (!user.canSignIn()) {
            when (user.status) {
                UserStatus.BANNED -> throw AuthenticationException("账号已被封禁，封禁结束时间： ${user.bannedUntil}")
                UserStatus.PENDING -> throw AuthenticationException("邮箱未验证")
                else -> throw AuthenticationException("账号不可用")
            }
        }

        val tokens = tokenService.generate(user.id, user.email!!)

        val refreshToken = RefreshToken(
            userId = user.id,
            tokenHash = encoder.encode(tokens.refreshToken),
            expiresAt = tokens.refreshTokenExpiresAt,
        )
        refreshTokenRepository.save(refreshToken)

        return@execute AuthenticatedUser(user.toUserProfileDto(), tokens)
    }

    override suspend fun logout(refreshToken: String) = tx.execute {
        val tokenHash = encoder.encode(refreshToken)
        val storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
        if (storedToken != null) {
            refreshTokenRepository.revoke(storedToken)
        }
    }

    override suspend fun deleteAccount(userId: UserId) = tx.execute {
        val user = userRepository.findById(userId)
            ?: throw NotFoundException("用户不存在")
        userRepository.deleteById(user.id)
    }

    override suspend fun verifyEmail(email: Email) = tx.execute {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundException("用户不存在")

        user.verifyEmail()

        userRepository.save(user)
        user.publishAndClear(domainEventBus)
    }

    override suspend fun resetPassword(
        email: Email,
        newPassword: PlainPassword,
    ) = tx.execute {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundException("用户不存在")

        user.changePassword(passwordEncoder.encode(newPassword))

        userRepository.save(user)
        user.publishAndClear(domainEventBus)
    }

    override suspend fun changePassword(
        userId: UserId,
        oldPassword: PlainPassword,
        newPassword: PlainPassword,
    ) = tx.execute {
        val user = userRepository.findById(userId)
            ?: throw NotFoundException("用户不存在")

        if (!passwordEncoder.matches(oldPassword, user.hashedPassword)) {
            throw AuthenticationException("旧密码错误")
        }

        user.changePassword(passwordEncoder.encode(newPassword))

        userRepository.save(user)
        user.publishAndClear(domainEventBus)
    }

    override suspend fun getProfile(userId: UserId): UserProfileDto = tx.readOnly {
        val user = userRepository.findById(userId)
            ?: throw NotFoundException("用户不存在")

        return@readOnly user.toUserProfileDto()
    }

    override suspend fun refreshToken(refreshToken: String): AuthenticatedUser = tx.execute {
        tokenService.verify(refreshToken)

        val tokenHash = encoder.encode(refreshToken)
        val storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
            ?: throw AuthenticationException("Refresh token不存在")

        if (!storedToken.isValid()) {
            throw AuthenticationException("Refresh token 已过期")
        }
        refreshTokenRepository.revoke(storedToken)

        val user = userRepository.findById(storedToken.userId)
            ?: throw NotFoundException("用户不存在")

        if (!user.canSignIn()) {
            throw AuthenticationException("账号不可用")
        }

        val tokens = tokenService.generate(user.id, user.email!!)

        val newRefreshToken = RefreshToken(
            userId = user.id,
            tokenHash = encoder.encode(tokens.refreshToken),
            expiresAt = tokens.refreshTokenExpiresAt,
        )
        refreshTokenRepository.save(newRefreshToken)

        userRepository.save(user)

        return@execute AuthenticatedUser(user.toUserProfileDto(), tokens)
    }
}