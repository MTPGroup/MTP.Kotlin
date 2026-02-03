package tech.hanasaki.azusa.modules.auth.infrastructure.adapter

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import tech.hanasaki.azusa.common.kernel.exception.AuthenticationException
import tech.hanasaki.azusa.common.kernel.model.Email
import tech.hanasaki.azusa.common.kernel.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.model.JwtConfig
import tech.hanasaki.azusa.modules.auth.domain.port.TokenPair
import tech.hanasaki.azusa.modules.auth.domain.port.TokenService
import java.util.Date
import kotlin.time.toKotlinInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class JwtTokenService(
    private val config: JwtConfig,
) : TokenService {
    override fun generateTokens(
        userId: UserId,
        email: Email,
    ): TokenPair {
        val algorithm = Algorithm.HMAC256(config.secret)

        val now = System.currentTimeMillis()
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
            .withJWTId(Uuid.random().toString())
            .withIssuedAt(Date(now))
            .withExpiresAt(refreshExpiresAt)
            .sign(algorithm)

        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = config.accessTokenMinutes * 60L,
            refreshTokenExpiresAt = refreshExpiresAt.toInstant().toKotlinInstant()
        )
    }

    override fun verifyRefreshToken(refreshToken: String): UserId {
        val algorithm = Algorithm.HMAC256(config.secret)
        val verifier = JWT.require(algorithm)
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .build()

        try {
            val decodedJWT = verifier.verify(refreshToken)
            val userIdString = decodedJWT.subject
                ?: throw AuthenticationException("Refresh token is missing subject (userId)")
            return UserId(Uuid.parse(userIdString))
        } catch (_: JWTVerificationException) {
            throw AuthenticationException("Invalid or expired refresh token")
        } catch (_: IllegalArgumentException) {
            throw AuthenticationException("Invalid userId format in refresh token")
        }
    }
}