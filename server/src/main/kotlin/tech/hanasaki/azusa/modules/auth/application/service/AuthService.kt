package tech.hanasaki.azusa.modules.auth.application.service

import tech.hanasaki.azusa.modules.auth.application.command.LoginCommand
import tech.hanasaki.azusa.modules.auth.application.command.RegisterCommand
import tech.hanasaki.azusa.modules.auth.application.result.LoginResult
import tech.hanasaki.azusa.modules.auth.domain.model.PasswordHash
import tech.hanasaki.azusa.modules.auth.domain.model.User
import tech.hanasaki.azusa.modules.auth.domain.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.model.UserStatus
import tech.hanasaki.azusa.modules.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.shared.domain.event.EventPublisher
import tech.hanasaki.azusa.shared.domain.exception.AuthenticationException
import tech.hanasaki.azusa.shared.domain.exception.ConflictException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException

class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService,
    private val eventPublisher: EventPublisher,
) {

    /**
     * 用户注册
     */
    suspend fun register(cmd: RegisterCommand): UserId {
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

        return user.id
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

        userRepository.save(user)

        return LoginResult(
            userId = UserId(user.id.value),
            username = user.profile.username,
            email = user.email!!,
            avatar = user.profile.avatar,
            createdAt = user.profile.createdAt,
            updatedAt = user.profile.updatedAt,
            tokens = tokens,
        )
    }

    /**
     * 邮箱验证
     */
    suspend fun verifyEmail(userId: UserId) {
        val user = userRepository.findById(userId)
            ?: throw NotFoundException("User not found")

        user.verifyEmail()

        userRepository.save(user)
        eventPublisher.publishAll(user.domainEvents)
        user.clearDomainEvents()
    }
}