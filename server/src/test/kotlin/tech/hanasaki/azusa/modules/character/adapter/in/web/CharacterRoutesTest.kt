package tech.hanasaki.azusa.modules.character.adapter.`in`.web

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.koin.core.module.Module
import tech.hanasaki.azusa.BaseIntegrationTest
import tech.hanasaki.azusa.modules.character.adapter.`in`.web.dto.CharacterResponse
import tech.hanasaki.azusa.modules.character.adapter.`in`.web.dto.KnowledgeSubscriptionResponse
import tech.hanasaki.azusa.modules.character.characterModule
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharacterRoutesTest : BaseIntegrationTest() {

    override fun additionalModules(config: ApplicationConfig): List<Module> = listOf(
        characterModule()
    )

    override fun Route.additionalRoutes() {
        characterRoutes()
    }

    @Test
    fun `GET character should return success`() = integrationTest {
        val accessToken = getSignInInfo()?.tokens?.accessToken
        val response = client.get("/characters") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET get public characters should return success`() = integrationTest {
        val accessToken = getSignInInfo()?.tokens?.accessToken
        val response = client.get("/characters/public") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET search public characters should return success`() = integrationTest {
        val accessToken = getSignInInfo()?.tokens?.accessToken
        val response = client.get("/characters/search?q=test") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }

        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET character detail should return success`() = integrationTest {
        val accessToken = getSignInInfo()?.tokens?.accessToken
        val characterId = getCreatedCharacter()?.id
        val response = client.get("/characters/$characterId") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST character should return created`() = integrationTest {
        val accessToken = getSignInInfo()?.tokens?.accessToken
        val response = client.post("/characters") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "name": "Test Character",
                    "avatar": null,
                    "bio": "This is a test character",
                    "originPrompt": null,
                    "isPublic": true
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `PUT character should return ok`() = integrationTest {
        val accessToken = getSignInInfo()?.tokens?.accessToken
        val characterId = getCreatedCharacter()?.id

        val response = client.put("/characters/$characterId") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(
                """
                    {
                        "name": "Test Character",
                        "avatar": null,
                        "bio": "This is a test character",
                        "originPrompt": null,
                        "isPublic": false
                    }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `DELETE character should return no content`() = integrationTest {
        val accessToken = getSignInInfo()?.tokens?.accessToken
        val characterId = getCreatedCharacter()?.id

        val response = client.delete("/characters/$characterId") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `GET character knowledge bases should return success and empty`() = integrationTest {
        val accessToken = getSignInInfo()?.tokens?.accessToken
        val characterId = getCreatedCharacter()?.id
        val response = client.get("/characters/$characterId/knowledge-bases") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }
        val responseBody = response.body<ApiResponse<List<KnowledgeSubscriptionResponse>>>()

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue { responseBody.data?.isEmpty() ?: false }
    }

    private suspend fun ClientProvider.getCreatedCharacter(): CharacterResponse? {
        val accessToken = getSignInInfo()?.tokens?.accessToken
        return client.post("/characters") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(
                """
                    {
                        "name": "Test Character",
                        "avatar": null,
                        "bio": "This is a test character",
                        "originPrompt": null,
                        "isPublic": true
                    }
                """.trimIndent()
            )
        }.body<ApiResponse<CharacterResponse>>().data
    }
}