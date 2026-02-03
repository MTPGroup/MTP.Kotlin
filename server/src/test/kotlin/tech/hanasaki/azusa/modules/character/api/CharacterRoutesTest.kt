package tech.hanasaki.azusa.modules.character.api

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.koin.ktor.plugin.Koin
import tech.hanasaki.azusa.BaseIntegrationTest
import tech.hanasaki.azusa.common.platform.api.ApiResponse
import tech.hanasaki.azusa.modules.auth.api.authRoutes
import tech.hanasaki.azusa.modules.auth.authModule
import tech.hanasaki.azusa.modules.character.api.dto.CharacterResponse
import tech.hanasaki.azusa.modules.character.characterModule
import kotlin.test.assertEquals

class CharacterRoutesTest : BaseIntegrationTest() {
    private fun testCharacterApplication(
        block: suspend ApplicationTestBuilder.() -> Unit,
    ) = testApplication {
        environment {
            config = ApplicationConfig("application.yaml")
        }
        application {
            install(Koin) {
                modules(
                    testSharedModule(),
                    authModule(environment.config),
                    characterModule(environment.config),
                )
            }
            testModule()
            routing {
                authRoutes()
                characterRoutes()
            }
        }
        client = createClient {
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    },
                )
            }
        }
        startApplication()
        createTestUser()
        block()
    }

    @Test
    fun `GET character should return success`() = testCharacterApplication {
        val accessToken = getSignInInfo()?.accessToken
        val response = client.get("/characters") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET get public characters should return success`() = testCharacterApplication {
        val accessToken = getSignInInfo()?.accessToken
        val response = client.get("/characters/public") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET search public characters should return success`() = testCharacterApplication {
        val accessToken = getSignInInfo()?.accessToken
        val response = client.get("/characters/search?q=test") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }

        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET character detail should return success`() = testCharacterApplication {
        val accessToken = getSignInInfo()?.accessToken
        val characterId = getCreatedCharacter()?.id
        val response = client.get("/characters/$characterId") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST character should return created`() = testCharacterApplication {
        val accessToken = getSignInInfo()?.accessToken
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
    fun `PUT character should return ok`() = testCharacterApplication {
        val accessToken = getSignInInfo()?.accessToken
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
    fun `DELETE character should return no content`() = testCharacterApplication {
        val accessToken = getSignInInfo()?.accessToken
        val characterId = getCreatedCharacter()?.id

        val response = client.delete("/characters/$characterId") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `GET character knowledge bases should return success`() = testCharacterApplication {
        val accessToken = getSignInInfo()?.accessToken
        val characterId = getCreatedCharacter()?.id
        val response = client.get("/characters/$characterId/knowledge-bases") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    private suspend fun ClientProvider.getCreatedCharacter(): CharacterResponse? {
        val accessToken = getSignInInfo()?.accessToken
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