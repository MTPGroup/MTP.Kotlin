package tech.hanasaki.azusa.auth.internal.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.stereotype.Service
import tech.hanasaki.azusa.auth.internal.application.port.TokenPair
import tech.hanasaki.azusa.auth.internal.application.port.TokenService
import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.UserId
import tech.hanasaki.azusa.auth.internal.config.JwtConfig
import tech.hanasaki.azusa.shared.AuthenticationException
import java.util.*
import kotlin.time.toKotlinInstant

@Service
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
            .withJWTId(UUID.randomUUID().toString())
            .withIssuedAt(Date(now))
            .withExpiresAt(refreshExpiresAt)
            .sign(algorithm)

        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = config.accessTokenMinutes * 60L,
            refreshTokenExpiresAt = refreshExpiresAt.toInstant().toKotlinInstant(),
            createdAt = issuedAt.toInstant().toKotlinInstant()
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
            return UserId(UUID.fromString(userIdString))
        } catch (_: JWTVerificationException) {
            throw AuthenticationException("Invalid or expired refresh token")
        } catch (_: IllegalArgumentException) {
            throw AuthenticationException("Invalid userId format in refresh token")
        }
    }
}
