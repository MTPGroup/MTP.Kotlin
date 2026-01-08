package tech.hanasaki.azusa.routes

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
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
import kotlinx.serialization.json.JsonArray
import org.testcontainers.containers.PostgreSQLContainer
import tech.hanasaki.azusa.app.module
import tech.hanasaki.azusa.auth.SignInWithPasswordRequest
import tech.hanasaki.azusa.auth.SignInWithPasswordResponse
import tech.hanasaki.azusa.auth.SignUpRequest
import tech.hanasaki.azusa.auth.VerifyOTPRequest
import tech.hanasaki.azusa.settings.SettingsResponse
import tech.hanasaki.azusa.settings.UpdateSettingsRequest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsRoutesTest {
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
    fun getSettingsReturnsSettings(): Unit = testApplication {
        environment {
            config = testConfig()
        }
        application { module() }

        val token = signUpAndVerify()
        val response = client.get("/settings") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val payload = json.decodeFromString<SettingsResponse>(response.bodyAsText())
        assertTrue(payload.success)
        assertTrue(payload.data.ownerId.isNotBlank())
    }

    @Test
    fun updateSettingsUpdatesTheme(): Unit = testApplication {
        environment {
            config = testConfig()
        }
        application { module() }

        val token = signUpAndVerify()
        val response = client.patch("/settings") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(UpdateSettingsRequest(theme = "dark", chatModels = JsonArray(emptyList()))))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val payload = json.decodeFromString<SettingsResponse>(response.bodyAsText())
        assertEquals("dark", payload.data.theme)
    }

    private suspend fun ApplicationTestBuilder.signUpAndVerify(): String {
        val email = "settings-${UUID.randomUUID()}@example.com"
        val password = "password123"

        client.post("/auth/sign-up/email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SignUpRequest(
                        email = email,
                        name = "Settings User",
                        password = password,
                        callbackURL = "http://localhost",
                    ),
                ),
            )
        }

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
