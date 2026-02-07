package tech.hanasaki.azusa.modules.setting.adapter.`in`.web

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import tech.hanasaki.azusa.BaseIntegrationTest
import tech.hanasaki.azusa.modules.auth.domain.port.UserRepositoryPort
import tech.hanasaki.azusa.modules.setting.adapter.`in`.web.dto.SettingResponse
import tech.hanasaki.azusa.modules.setting.application.port.`in`.SettingUseCasePort
import tech.hanasaki.azusa.modules.setting.settingModule
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse
import tech.hanasaki.azusa.shared.port.out.TransactionalPort
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SettingRoutesTest : BaseIntegrationTest() {

    override fun additionalModules(config: ApplicationConfig): List<Module> = listOf(
        settingModule()
    )

    override fun Route.additionalRoutes() {
        settingRoutes()
    }

    private suspend fun createTestSetting() {
        val koin = GlobalContext.get()
        val userRepository = koin.get<UserRepositoryPort>()
        val settingUseCase = koin.get<SettingUseCasePort>()
        val tx = koin.get<TransactionalPort>()
        val user = tx.readOnly {
            userRepository.findByEmail(Email("test-user@example.com"))!!
        }
        settingUseCase.createSetting(user.id)
    }

    private fun settingTest(block: suspend io.ktor.server.testing.ApplicationTestBuilder.() -> Unit) =
        integrationTest {
            createTestSetting()
            block()
        }


    @Test
    fun `GET settings without token should return Unauthorized`() = integrationTest {
        val response = client.get("/settings")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET settings should return user setting`() = settingTest {
        val accessToken = getSignInInfo()?.tokens?.accessToken
        val response = client.get("/settings") {
            headers { append(HttpHeaders.Authorization, "Bearer $accessToken") }
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ApiResponse<SettingResponse>>()
        assertTrue(body.success)
        assertNotNull(body.data)
        assertEquals("SYSTEM", body.data.theme.name)
        assertTrue(body.data.llmConfigs.isEmpty())
    }


    @Test
    fun `PUT settings without token should return Unauthorized`() = integrationTest {
        val response = client.put("/settings") {
            contentType(ContentType.Application.Json)
            setBody(updateSettingJson())
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT settings should update theme and llmConfigs`() = settingTest {
        val accessToken = getSignInInfo()?.tokens?.accessToken
        val response = client.put("/settings") {
            headers { append(HttpHeaders.Authorization, "Bearer $accessToken") }
            contentType(ContentType.Application.Json)
            setBody(updateSettingJson())
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ApiResponse<SettingResponse>>()
        assertTrue(body.success)
        assertNotNull(body.data)
        assertEquals("DARK", body.data.theme.name)
        assertEquals(1, body.data.llmConfigs.size)
        assertEquals("gpt-4", body.data.llmConfigs.first().model)
    }

    @Test
    fun `PUT settings should replace llmConfigs entirely`() = settingTest {
        val accessToken = getSignInInfo()?.tokens?.accessToken

        client.put("/settings") {
            headers { append(HttpHeaders.Authorization, "Bearer $accessToken") }
            contentType(ContentType.Application.Json)
            setBody(updateSettingJson())
        }

        val response = client.put("/settings") {
            headers { append(HttpHeaders.Authorization, "Bearer $accessToken") }
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "theme": "LIGHT",
                    "llmConfigs": [
                        {
                            "provider": "AZURE",
                            "baseUrl": "https://azure.openai.com/v1",
                            "apiKey": "azure-key",
                            "model": "gpt-4o",
                            "temperature": 0.5,
                            "maxTokens": 2000,
                            "runOnClient": false
                        }
                    ],
                    "activeThemeId": null,
                    "activeLlmConfigId": null
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ApiResponse<SettingResponse>>()
        assertTrue(body.success)
        assertEquals(1, body.data?.llmConfigs?.size)
        assertEquals("gpt-4o", body.data?.llmConfigs?.first()?.model)
        assertEquals("LIGHT", body.data?.theme?.name)
    }

    @Test
    fun `PUT settings with activeLlmConfigId should set active config`() = settingTest {
        val accessToken = getSignInInfo()?.tokens?.accessToken

        val createResponse = client.put("/settings") {
            headers { append(HttpHeaders.Authorization, "Bearer $accessToken") }
            contentType(ContentType.Application.Json)
            setBody(updateSettingJson())
        }
        val configId = createResponse.body<ApiResponse<SettingResponse>>()
            .data?.llmConfigs?.first()?.id

        val response = client.put("/settings") {
            headers { append(HttpHeaders.Authorization, "Bearer $accessToken") }
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "theme": "DARK",
                    "llmConfigs": [
                        {
                            "id": "$configId",
                            "provider": "OPENAI",
                            "baseUrl": "https://api.openai.com/v1",
                            "apiKey": "test-api-key",
                            "model": "gpt-4",
                            "temperature": 0.7,
                            "maxTokens": 1000,
                            "runOnClient": false
                        }
                    ],
                    "activeThemeId": null,
                    "activeLlmConfigId": "$configId"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ApiResponse<SettingResponse>>()
        assertTrue(body.success)
        assertEquals(configId, body.data?.activeLlmConfigId)
    }

    private fun updateSettingJson(): String = """
        {
            "theme": "DARK",
            "llmConfigs": [
                {
                    "provider": "OPENAI",
                    "baseUrl": "https://api.openai.com/v1",
                    "apiKey": "test-api-key",
                    "model": "gpt-4",
                    "temperature": 0.7,
                    "maxTokens": 1000,
                    "runOnClient": false
                }
            ],
            "activeThemeId": null,
            "activeLlmConfigId": null
        }
    """.trimIndent()
}
