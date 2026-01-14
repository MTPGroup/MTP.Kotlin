package tech.hanasaki.azusa.modules.auth.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.datetime.Clock
import tech.hanasaki.azusa.config.JwtConfig
import tech.hanasaki.azusa.modules.auth.application.service.TokenPair
import tech.hanasaki.azusa.modules.auth.application.service.TokenService
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.UserId
import java.util.*

class JwtTokenService(
    private val config: JwtConfig,
) : TokenService {
    override fun generateTokens(
        userId: UserId,
        email: Email,
    ): TokenPair {
        val algorithm = Algorithm.HMAC256(config.secret)

        val now = Clock.System.now().toEpochMilliseconds()
        val accessDurationMillis = config.accessTokenMinutes * 60 * 1000L
        val refreshDurationMillis = config.refreshTokenDays * 24 * 60 * 60 * 1000L

        val issuedAt = Date(now)
        val accessExpiresAt = Date(now + accessDurationMillis)
        val refreshExpiresAt = Date(now + refreshDurationMillis)

        val accessToken = JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(userId.value.toString())
            .withClaim("email", email.value)
            .withIssuedAt(issuedAt)
            .withExpiresAt(accessExpiresAt)
            .sign(algorithm)

        val refreshToken = JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(userId.value.toString())
            .withJWTId(UUID.randomUUID().toString())
            .withIssuedAt(Date(now))
            .withExpiresAt(refreshExpiresAt)
            .sign(algorithm)

        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = accessExpiresAt.time
        )
    }
}