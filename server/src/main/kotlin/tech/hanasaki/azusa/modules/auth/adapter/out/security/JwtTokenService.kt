package tech.hanasaki.azusa.modules.auth.adapter.out.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import tech.hanasaki.azusa.modules.auth.application.dto.TokenPair
import tech.hanasaki.azusa.modules.auth.application.port.out.TokenServicePort
import tech.hanasaki.azusa.modules.auth.config.JwtConfig
import tech.hanasaki.azusa.shared.domain.exception.AuthenticationException
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.time.toJavaInstant


class JwtTokenService(
    private val config: JwtConfig,
) : TokenServicePort {
    private val algorithm by lazy { Algorithm.HMAC256(config.secret.toByteArray()) }

    override fun generate(
        userId: UserId,
        email: Email,
    ): TokenPair {
        val now = Clock.System.now()

        val accessExpiresAt = now.plus(config.accessTokenMinutes.minutes)
        val refreshExpiresAt = now.plus(config.refreshTokenDays.days)

        val accessToken = createAccessToken(
            userId = userId,
            email = email,
            issuedAt = now,
            expiresAt = accessExpiresAt,
        )

        val refreshToken = createRefreshToken(
            userId = userId,
            issuedAt = now,
            expiresAt = refreshExpiresAt,
        )

        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresAt = accessExpiresAt,
            refreshTokenExpiresAt = refreshExpiresAt,
        )
    }

    override fun verify(refreshToken: String): UserId {
        try {
            val verifier = JWT.require(algorithm)
                .withIssuer(config.issuer)
                .withAudience(config.audience)
                .build()

            val decodedJWT = verifier.verify(refreshToken)
            if (decodedJWT.id == null)
                throw AuthenticationException("Refresh token 缺失 JWT ID")

            val userIdString = decodedJWT.subject
                ?: throw AuthenticationException("Refresh token 缺失 Subject(userId)")
            return try {
                UserId.fromString(userIdString)
            } catch (e: IllegalArgumentException) {
                throw AuthenticationException("Refresh token 中的 userId 非法: ${e.message}")
            }
        } catch (e: JWTVerificationException) {
            throw AuthenticationException("Refresh token 验证失败: ${e.message}")
        }
    }

    private fun createAccessToken(
        userId: UserId,
        email: Email,
        issuedAt: Instant,
        expiresAt: Instant,
    ): String {
        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(userId.value.toString())
            .withClaim("email", email.value)
            .withIssuedAt(Date.from(issuedAt.toJavaInstant()))
            .withExpiresAt(Date.from(expiresAt.toJavaInstant()))
            .sign(algorithm)
    }

    private fun createRefreshToken(
        userId: UserId,
        issuedAt: Instant,
        expiresAt: Instant,
    ): String {
        val jwtId = UUID.randomUUID().toString()

        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(userId.value.toString())
            .withJWTId(jwtId)
            .withIssuedAt(Date.from(issuedAt.toJavaInstant()))
            .withExpiresAt(Date.from(expiresAt.toJavaInstant()))
            .sign(algorithm)
    }
}
