package tech.hanasaki.azusa.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.testcontainers.containers.PostgreSQLContainer
import tech.hanasaki.azusa.app.module
import tech.hanasaki.azusa.auth.*
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRoutesTest {
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
    fun healthRouteReturnsOk(): Unit = testApplication {
        environment {
            config = testConfig()
        }
        application { module() }

        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("ok", response.bodyAsText())
    }

    @Test
    fun signUpAndSignInWork(): Unit = testApplication {
        environment {
            config = testConfig()
        }
        application { module() }

        val email = "test-${UUID.randomUUID()}@example.com"
        val signUpResponse = client.post("/auth/sign-up/email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SignUpRequest(
                        email = email,
                        name = "Test User",
                        password = "password123",
                        callbackURL = "http://localhost",
                    ),
                ),
            )
        }
        assertStatus(HttpStatusCode.OK, signUpResponse.status, signUpResponse.bodyAsText())
        val signUp = json.decodeFromString<SignUpResponse>(signUpResponse.bodyAsText())
        assertNotNull(signUp.token)
        assertTrue(signUp.refreshToken.isNotBlank())
        assertEquals(email, signUp.user.email)
        assertEquals("Test User", signUp.user.name)
        assertTrue(signUp.user.id.isNotBlank())

        val signInResponse = client.post("/auth/sign-in/email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SignInWithPasswordRequest(
                        email = email,
                        password = "password123",
                    ),
                ),
            )
        }
        assertStatus(HttpStatusCode.OK, signInResponse.status, signInResponse.bodyAsText())
        val signIn = json.decodeFromString<SignInWithPasswordResponse>(signInResponse.bodyAsText())
        assertTrue(signIn.token.isNotBlank())
        assertTrue(signIn.refreshToken.isNotBlank())
        assertEquals(email, signIn.user.email)

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
        assertStatus(HttpStatusCode.OK, refreshResponse.status, refreshResponse.bodyAsText())
        val refreshPayload = json.decodeFromString<RefreshTokenResponse>(refreshResponse.bodyAsText())
        assertTrue(refreshPayload.token.isNotBlank())
        assertTrue(refreshPayload.refreshToken.isNotBlank())

        val signOutResponse = client.post("/auth/sign-out") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SignOutRequest(
                        refreshToken = refreshPayload.refreshToken,
                    ),
                ),
            )
        }
        assertStatus(HttpStatusCode.OK, signOutResponse.status, signOutResponse.bodyAsText())

        val refreshAfterSignOut = client.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    RefreshTokenRequest(
                        refreshToken = refreshPayload.refreshToken,
                    ),
                ),
            )
        }
        assertEquals(HttpStatusCode.Unauthorized, refreshAfterSignOut.status)
    }

    @Test
    fun emailOtpFlowWorks(): Unit = testApplication {
        environment {
            config = testConfig()
        }
        application { module() }

        val email = "otp-${UUID.randomUUID()}@example.com"
        client.post("/auth/sign-up/email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SignUpRequest(
                        email = email,
                        name = "Otp User",
                        password = "password123",
                        callbackURL = "http://localhost",
                    ),
                ),
            )
        }

        val verifySend = client.post("/auth/email-otp/send-verification-otp") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SendEmailVerificationRequest(
                        email = email,
                        type = "email-verification",
                    ),
                ),
            )
        }
        assertStatus(HttpStatusCode.OK, verifySend.status, verifySend.bodyAsText())
        val verifyOtp = verifySend.headers["X-OTP-Code"] ?: error("Missing X-OTP-Code header")

        val verifyResponse = client.post("/auth/email-otp/verify-email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    VerifyOTPRequest(
                        email = email,
                        otp = verifyOtp,
                    ),
                ),
            )
        }
        assertStatus(HttpStatusCode.NoContent, verifyResponse.status, verifyResponse.bodyAsText())

        val resetSend = client.post("/auth/email-otp/forget-password") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SendPasswordResetEmailRequest(
                        email = email,
                    ),
                ),
            )
        }
        assertStatus(HttpStatusCode.OK, resetSend.status, resetSend.bodyAsText())
        val resetOtp = resetSend.headers["X-OTP-Code"] ?: error("Missing X-OTP-Code header")

        val resetResponse = client.post("/auth/email-otp/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    ResetPasswordRequest(
                        email = email,
                        otp = resetOtp,
                        password = "newpassword123",
                    ),
                ),
            )
        }
        assertStatus(HttpStatusCode.OK, resetResponse.status, resetResponse.bodyAsText())

        val signInResponse = client.post("/auth/sign-in/email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SignInWithPasswordRequest(
                        email = email,
                        password = "newpassword123",
                    ),
                ),
            )
        }
        assertStatus(HttpStatusCode.OK, signInResponse.status, signInResponse.bodyAsText())
        val signIn = json.decodeFromString<SignInWithPasswordResponse>(signInResponse.bodyAsText())
        assertTrue(signIn.token.isNotBlank())
        assertTrue(signIn.refreshToken.isNotBlank())
    }

    @Test
    fun signOutReturnsSuccess(): Unit = testApplication {
        environment {
            config = testConfig()
        }
        application { module() }

        val response = client.post("/auth/sign-out")

        assertStatus(HttpStatusCode.OK, response.status, response.bodyAsText())
        val payload = json.decodeFromString<SignOutResponse>(response.bodyAsText())
        assertTrue(payload.success)
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
            "auth.accessTokenMinutes" to "15",
            "auth.refreshTokenDays" to "30",
        )
    }

    private fun assertStatus(expected: HttpStatusCode, actual: HttpStatusCode, body: String): Unit {
        if (expected != actual) {
            throw AssertionError("expected:<$expected> but was:<$actual>\nbody:\n$body")
        }
    }
}
