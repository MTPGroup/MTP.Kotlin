package tech.hanasaki.azusa.auth.internal.api

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tech.hanasaki.azusa.auth.api.AuthController
import tech.hanasaki.azusa.auth.api.dto.SendOtpRequest
import tech.hanasaki.azusa.auth.api.dto.SignInWithPasswordRequest
import tech.hanasaki.azusa.auth.api.dto.SignUpRequest
import tech.hanasaki.azusa.auth.application.result.LoginResult
import tech.hanasaki.azusa.auth.application.service.AuthService
import tech.hanasaki.azusa.auth.application.service.OtpService
import tech.hanasaki.azusa.auth.internal.application.service.TokenPair
import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.OtpType
import tech.hanasaki.azusa.auth.domain.model.UserId
import tech.hanasaki.azusa.auth.domain.model.Username
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@WebMvcTest(
    controllers = [AuthController::class],
    excludeAutoConfiguration = [
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration::class,
    ],
)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthControllerTest.TestConfig::class)
class AuthControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
) {

    @Autowired
    lateinit var authService: AuthService

    @Autowired
    lateinit var otpService: OtpService

    @Test
    fun `sign up returns success`() = runBlocking {
        val request = SignUpRequest(email = "user@example.com", name = "Alice", password = "password")

        mockMvc.perform(
            post("/auth/sign-up/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        verify(authService).register(request.toCommand())
    }

    @Test
    fun `sign in returns tokens`() = runBlocking {
        val now = Clock.System.now()
        val userId = UserId(UUID.randomUUID())
        val loginResult = LoginResult(
            userId = userId,
            email = Email("user@example.com"),
            username = Username("Alice"),
            avatar = null,
            isEmailVerified = true,
            tokens = TokenPair(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                createdAt = now,
                expiresIn = 3600,
                refreshTokenExpiresAt = now.plus(30.minutes),
            ),
            createdAt = now,
            updatedAt = now.plus(10.seconds),
        )
        given(authService.login(any())).willReturn(loginResult)

        val request = SignInWithPasswordRequest(email = "user@example.com", password = "password")

        mockMvc.perform(
            post("/auth/sign-in/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
            .andExpect(jsonPath("$.user.id").value(userId.value.toString()))
            .andExpect(jsonPath("$.user.email").value("user@example.com"))
    }

    @Test
    fun `send otp triggers service`() = runBlocking {
        val request = SendOtpRequest(email = "user@example.com", type = OtpType.VERIFY_EMAIL.name)

        mockMvc.perform(
            post("/auth/email-otp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        verify(otpService).sendOtp(Email("user@example.com"), OtpType.VERIFY_EMAIL)
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun authService(): AuthService = mock(AuthService::class.java)

        @Bean
        fun otpService(): OtpService = mock(OtpService::class.java)
    }
}
