package tech.hanasaki.azusa.auth.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.OncePerRequestFilter
import tech.hanasaki.azusa.auth.config.JwtConfig
import tech.hanasaki.azusa.shared.ErrorDetail
import tech.hanasaki.azusa.shared.ErrorResponse
import java.util.*
import kotlin.time.Clock

@Configuration
class SecurityConfiguration(
    private val jwtConfig: JwtConfig,
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.GET, "/health").permitAll()
                auth.requestMatchers(
                    "/auth/sign-up/email",
                    "/auth/sign-in/email",
                    "/auth/refresh",
                    "/auth/sign-out",
                    "/auth/email-otp/**",
                ).permitAll()
                auth.anyRequest().authenticated()
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtConfig, objectMapper),
                UsernamePasswordAuthenticationFilter::class.java
            )
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}

private class JwtAuthenticationFilter(
    private val jwtConfig: JwtConfig,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION) ?: ""
        if (!header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = header.removePrefix("Bearer ").trim()
        if (token.isBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val verifier = JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                .withIssuer(jwtConfig.issuer)
                .withAudience(jwtConfig.audience)
                .build()
            val decoded = verifier.verify(token)
            val subject = decoded.subject ?: throw IllegalArgumentException("Missing subject")
            UUID.fromString(subject)
            val authentication = UsernamePasswordAuthenticationToken(subject, null, emptyList())
            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
        } catch (_: JWTVerificationException) {
            writeUnauthorized(response, "Invalid or expired token")
        } catch (_: IllegalArgumentException) {
            writeUnauthorized(response, "Invalid subject")
        }
    }

    private fun writeUnauthorized(response: HttpServletResponse, message: String) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        val payload = ErrorResponse(
            error = ErrorDetail(
                message = message,
                code = "UNAUTHORIZED",
            ),
            timestamp = Clock.System.now().toString(),
        )
        response.writer.write(objectMapper.writeValueAsString(payload))
    }
}