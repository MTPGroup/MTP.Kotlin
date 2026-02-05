package tech.hanasaki.azusa.modules.auth.adapter.out.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import tech.hanasaki.azusa.common.domain.exception.AuthenticationException
import tech.hanasaki.azusa.common.domain.model.Email
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.auth.config.JwtConfig
import tech.hanasaki.azusa.modules.auth.application.port.`in`.TokenVerifier
import tech.hanasaki.azusa.modules.auth.application.port.out.TokenGenerator
import tech.hanasaki.azusa.modules.auth.application.result.TokenPair
import java.util.*
import kotlin.time.toKotlinInstant
import kotlin.uuid.Uuid


class JwtTokenService(
    private val config: JwtConfig,
) : TokenGenerator, TokenVerifier {
    override fun generate(
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

    override fun verify(refreshToken: String): UserId {
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
