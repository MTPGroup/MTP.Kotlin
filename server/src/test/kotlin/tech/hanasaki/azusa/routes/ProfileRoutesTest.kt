package tech.hanasaki.azusa.routes

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.testcontainers.containers.PostgreSQLContainer
import tech.hanasaki.azusa.app.module
import tech.hanasaki.azusa.auth.RefreshTokenRequest
import tech.hanasaki.azusa.auth.SignInWithPasswordRequest
import tech.hanasaki.azusa.auth.SignInWithPasswordResponse
import tech.hanasaki.azusa.auth.SignUpRequest
import tech.hanasaki.azusa.auth.SignUpResponse
import tech.hanasaki.azusa.auth.VerifyOTPRequest
import tech.hanasaki.azusa.profile.ProfileResponse
import tech.hanasaki.azusa.profile.UpdateProfileRequest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileRoutesTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val postgres by lazy {
        PostgreSQLContainer<Nothing>("pgvector/pgvector:pg18").apply {
            withDatabaseName("azusa_test")
            withUsername("postgres")
            withPassword("postgres")
            start()
        }
    }

    @Test
    fun getProfileReturnsCurrentProfile(): Unit = testApplication {
        environment {
            config = testConfig()
        }
        application { module() }

        val token = signUpAndVerify()
        val response = client.get("/profiles") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val payload = json.decodeFromString<ProfileResponse>(response.bodyAsText())
        assertTrue(payload.success)
        assertTrue(payload.data.id.isNotBlank())
    }

    @Test
    fun updateProfileUpdatesUsername(): Unit = testApplication {
        environment {
            config = testConfig()
        }
        application { module() }

        val token = signUpAndVerify()
        val newName = "Updated User"
        val response = client.put("/profiles") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(UpdateProfileRequest(username = newName)))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val payload = json.decodeFromString<ProfileResponse>(response.bodyAsText())
        assertEquals(newName, payload.data.username)
    }

    private suspend fun ApplicationTestBuilder.signUpAndVerify(): String {
        val email = "profile-${UUID.randomUUID()}@example.com"
        val password = "password123"

        val signUpResponse = client.post("/auth/sign-up/email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SignUpRequest(
                        email = email,
                        name = "Profile User",
                        password = password,
                        callbackURL = "http://localhost",
                    ),
                ),
            )
        }
        json.decodeFromString<SignUpResponse>(signUpResponse.bodyAsText())

        val sendOtp = client.post("/auth/email-otp/send-verification-otp") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    tech.hanasaki.azusa.auth.SendEmailVerificationRequest(
                        email = email,
                        type = "email-verification",
                    ),
                ),
            )
        }
        val otp = sendOtp.headers["X-OTP-Code"] ?: error("Missing X-OTP-Code header")

        client.post("/auth/email-otp/verify-email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    VerifyOTPRequest(
                        email = email,
                        otp = otp,
                    ),
                ),
            )
        }

        val signInResponse = client.post("/auth/sign-in/email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SignInWithPasswordRequest(
                        email = email,
                        password = password,
                    ),
                ),
            )
        }
        val signIn = json.decodeFromString<SignInWithPasswordResponse>(signInResponse.bodyAsText())
        assertTrue(signIn.token.isNotBlank())
        assertTrue(signIn.refreshToken.isNotBlank())

        val refreshResponse = client.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    RefreshTokenRequest(
                        refreshToken = signIn.refreshToken,
                    ),
                ),
            )
        }
        assertEquals(HttpStatusCode.OK, refreshResponse.status)

        return signIn.token
    }

    private fun testConfig(): MapApplicationConfig {
        val container = postgres
        return MapApplicationConfig(
            "database.driver" to "org.postgresql.Driver",
            "database.url" to container.jdbcUrl,
            "database.user" to container.username,
            "database.password" to container.password,
            "database.maxPoolSize" to "5",
            "jwt.issuer" to "azusa",
            "jwt.audience" to "azusa",
            "jwt.realm" to "azusa",
            "jwt.secret" to "test_secret",
            "auth.accessTokenMinutes" to "15",
            "auth.refreshTokenDays" to "30",
            "smtp.host" to "localhost",
            "smtp.port" to "1025",
            "smtp.username" to "test",
            "smtp.password" to "test",
            "smtp.from" to "no-reply@example.com",
            "smtp.tls" to "true",
            "smtp.enabled" to "false",
            "otp.length" to "6",
            "otp.expiresMinutes" to "10",
            "otp.minIntervalSeconds" to "60",
            "otp.maxPerHour" to "5",
            "otp.debugReturn" to "true",
            "s3.endpoint" to "http://localhost:9000",
            "s3.region" to "us-east-1",
            "s3.bucket" to "avatars",
            "s3.accessKey" to "test",
            "s3.secretKey" to "test",
            "s3.publicBaseUrl" to "http://localhost:9000",
            "s3.forcePathStyle" to "true",
        )
    }
}
