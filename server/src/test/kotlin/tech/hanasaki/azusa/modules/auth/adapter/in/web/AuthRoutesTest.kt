package tech.hanasaki.azusa.modules.auth.adapter.`in`.web

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import tech.hanasaki.azusa.BaseIntegrationTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest : BaseIntegrationTest() {

    @Test
    fun `POST sign-up with valid data should succeed`() = integrationTest {
        val response = client.post("/auth/sign-up/email") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "test@example.com",
                    "password": "password123",
                    "name": "Test User"
                }
            """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("success"))
    }

    @Test
    fun `POST sign-up with invalid email should return BadRequest`() = integrationTest {
        val response = client.post("/auth/sign-up/email") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "invalid-email",
                    "password": "password123",
                    "name": "Test User"
                }
            """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST sign-up with short password should return BadRequest`() = integrationTest {
        val response = client.post("/auth/sign-up/email") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "test2@example.com",
                    "password": "123",
                    "name": "Test User"
                }
            """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST sign-in with invalid credentials should return error`() = integrationTest {
        val response = client.post("/auth/sign-in/email") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "nonexistent@example.com",
                    "password": "password123"
                }
            """.trimIndent()
            )
        }

        // 用户不存在应返回 404
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `POST sign-in with unverify email should return unauthorized`() = integrationTest {
        client.post("/auth/sign-up/email") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "test@example.com",
                    "password": "password123",
                    "name": "Test User"
                }
                """.trimIndent()
            )
        }

        val response = client.post("/auth/sign-in/email") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
            """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST sign-in with verify email should return success`() = integrationTest {
        val response = client.post("/auth/sign-in/email") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "test-user@example.com",
                    "password": "password123"
                }
            """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST send otp should success`() = integrationTest {
        val response = client.post("/auth/email-otp/send") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                    {
                        "email": "test@example.com",
                        "type": "VERIFY_EMAIL"
                    }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST refresh should success`() = integrationTest {
        val refreshToken = getSignInInfo()?.refreshToken
        val response = client.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                    {
                        "refreshToken": "$refreshToken"
                    }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST sign-out`() = integrationTest {
        val refreshToken = getSignInInfo()?.refreshToken
        val response = client.post("/auth/sign-out") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                    {
                        "refreshToken": "$refreshToken"
                    }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `GET auth me without token should return Unauthorized`() = integrationTest {
        val response = client.get("/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET auth me should return success`() = integrationTest {
        val accessToken = getSignInInfo()?.accessToken
        val response = client.get("/auth/me") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST change password should success`() = integrationTest {
        val accessToken = getSignInInfo()?.accessToken
        val response = client.post("/auth/password/change") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(
                """
                    {
                        "oldPassword": "password123",
                        "newPassword": "newpassword123"
                    }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `DELETE account should success`() = integrationTest {
        val accessToken = getSignInInfo()?.accessToken
        val response = client.delete("/auth/account") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }
}
