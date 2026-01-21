package tech.hanasaki.azusa.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import org.koin.ktor.ext.getKoin
import org.testcontainers.containers.PostgreSQLContainer
import tech.hanasaki.azusa.module
import tech.hanasaki.azusa.modules.auth.api.dto.*
import tech.hanasaki.azusa.modules.auth.application.port.EmailService
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.modules.auth.domain.repository.OtpRepository
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.koin.dsl.module as koinModule

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
        application {
            module()
            getKoin().loadModules(listOf(testOverrides()))
        }

        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("ok", response.bodyAsText())
    }

    @Test
    fun signUpSignInAndMe(): Unit = testApplication {
        environment { config = testConfig() }
        application {
            module()
            getKoin().loadModules(listOf(testOverrides()), allowOverride = true)
        }

        val email = "me-${UUID.randomUUID()}@example.com"
        val password = "password123"

        val signIn = signUpVerifyAndSignIn(email, password)
        assertTrue(signIn.accessToken.isNotBlank())

        val meResponse = client.get("/auth/me") {
            header(HttpHeaders.Authorization, "Bearer ${signIn.accessToken}")
        }
        assertEquals(HttpStatusCode.OK, meResponse.status)
        val me = json.decodeFromString(UserProfile.serializer(), meResponse.bodyAsText())
        assertEquals(email, me.email)
        assertEquals(signIn.user.id, me.id)
    }

    @Test
    fun changePasswordAndLoginWithNewPassword(): Unit = testApplication {
        environment { config = testConfig() }
        application {
            module()
            getKoin().loadModules(listOf(testOverrides()), allowOverride = true)
        }

        val email = "pw-${UUID.randomUUID()}@example.com"
        val oldPassword = "password123"
        val newPassword = "newpass456"

        val signIn = signUpVerifyAndSignIn(email, oldPassword)

        val changeResponse = client.post("/auth/password/change") {
            header(HttpHeaders.Authorization, "Bearer ${signIn.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    ChangePasswordRequest.serializer(),
                    ChangePasswordRequest(oldPassword, newPassword)
                )
            )
        }
        assertEquals(HttpStatusCode.OK, changeResponse.status)
        val changePayload = json.decodeFromString(ChangePasswordResponse.serializer(), changeResponse.bodyAsText())
        assertTrue(changePayload.success)

        val oldLogin = client.post("/auth/sign-in/email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SignInWithPasswordRequest.serializer(),
                    SignInWithPasswordRequest(email = email, password = oldPassword)
                )
            )
        }
        val oldLoginBody = oldLogin.bodyAsText()
        assertEquals(HttpStatusCode.Unauthorized, oldLogin.status, "old login body: $oldLoginBody")

        val newLogin = client.post("/auth/sign-in/email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SignInWithPasswordRequest.serializer(),
                    SignInWithPasswordRequest(email = email, password = newPassword)
                )
            )
        }
        assertEquals(HttpStatusCode.OK, newLogin.status)
        val newLoginPayload = json.decodeFromString(SignInWithPasswordResponse.serializer(), newLogin.bodyAsText())
        assertTrue(newLoginPayload.accessToken.isNotBlank())
    }

    @Test
    fun resendVerificationCreatesOtp(): Unit = testApplication {
        environment { config = testConfig() }
        application {
            module()
            getKoin().loadModules(listOf(testOverrides()), allowOverride = true)
        }

        val email = "otp-${UUID.randomUUID()}@example.com"
        val signIn = signUpVerifyAndSignIn(email, "password123")

        val resendResponse = client.post("/auth/email-otp/resend-verification") {
            header(HttpHeaders.Authorization, "Bearer ${signIn.accessToken}")
        }
        val resendBody = resendResponse.bodyAsText()
        assertEquals(HttpStatusCode.Conflict, resendResponse.status, "resend body: $resendBody")
    }

    private suspend fun ApplicationTestBuilder.signUpVerifyAndSignIn(
        email: String,
        password: String,
    ): SignInWithPasswordResponse {
        val signUpResponse = client.post("/auth/sign-up/email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SignUpRequest.serializer(),
                    SignUpRequest(
                        email = email,
                        name = "Test User",
                        password = password,
                    )
                )
            )
        }
        val signUpBody = signUpResponse.bodyAsText()
        assertEquals(HttpStatusCode.OK, signUpResponse.status, signUpBody)
        val signUpPayload = json.decodeFromString(SignUpResponse.serializer(), signUpBody)
        assertTrue(signUpPayload.success)

        val sendOtp = client.post("/auth/email-otp/send") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SendOtpRequest.serializer(),
                    SendOtpRequest(email = email, type = OtpType.VERIFY_EMAIL.name)
                )
            )
        }
        val sendOtpBody = sendOtp.bodyAsText()
        assertEquals(HttpStatusCode.OK, sendOtp.status, "send-otp body: $sendOtpBody")

        val otpRepo: OtpRepository = GlobalContext.get().get()
        val otp = otpRepo.findValidLatest(Email(email), OtpType.VERIFY_EMAIL)
        assertNotNull(otp)

        val verifyResponse = client.post("/auth/email-otp/verify-email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    VerifyOTPRequest.serializer(),
                    VerifyOTPRequest(email = email, otp = otp.code)
                )
            )
        }
        val verifyBody = verifyResponse.bodyAsText()
        assertEquals(HttpStatusCode.OK, verifyResponse.status, "verify body: $verifyBody")

        val signInResponse = client.post("/auth/sign-in/email") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    SignInWithPasswordRequest.serializer(),
                    SignInWithPasswordRequest(email = email, password = password)
                )
            )
        }
        val signInBody = signInResponse.bodyAsText()
        assertEquals(HttpStatusCode.OK, signInResponse.status, "sign-in body: $signInBody")
        return json.decodeFromString(SignInWithPasswordResponse.serializer(), signInBody)
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
            "jwt.accessTokenMinutes" to "15",
            "jwt.refreshTokenDays" to "30",
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

    private fun testOverrides() = koinModule {
        single<EmailService> { FakeEmailService() }
    }

    private class FakeEmailService : EmailService {
        override suspend fun sendHtml(to: Email, subject: String, html: String) {
        }
    }
}
