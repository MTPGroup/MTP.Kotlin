package tech.hanasaki.azusa.modules.setting.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import tech.hanasaki.azusa.BaseIntegrationTest
import tech.hanasaki.azusa.common.kernel.model.Email
import tech.hanasaki.azusa.common.platform.api.ApiResponse
import tech.hanasaki.azusa.modules.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.modules.setting.api.dto.LLMConfigResponse
import tech.hanasaki.azusa.modules.setting.api.dto.SettingResponse
import tech.hanasaki.azusa.modules.setting.domain.model.Setting
import tech.hanasaki.azusa.modules.setting.domain.repository.SettingRepository
import tech.hanasaki.azusa.modules.setting.settingModule
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class SettingRoutesTest : BaseIntegrationTest() {

    override fun additionalModules(config: ApplicationConfig): List<Module> = listOf(
        settingModule(config)
    )

    override fun Route.additionalRoutes() {
        settingRoutes()
    }

    /**
     * 为测试用户创建 Setting
     */
    private suspend fun createTestSetting() {
        val koin = GlobalContext.get()
        val userRepository = koin.get<UserRepository>()
        val settingRepository = koin.get<SettingRepository>()
        val user = userRepository.findByEmail(Email("test-user@example.com"))!!
        val setting = Setting.create(user.id)
        settingRepository.save(setting)
    }

    /**
     * 带 Setting 初始化的集成测试
     */
    private fun settingTest(block: suspend ApplicationTestBuilder.() -> Unit) = integrationTest {
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
        val accessToken = getSignInInfo()?.accessToken
        val response = client.get("/settings") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ApiResponse<SettingResponse>>()
        assertTrue(body.success)
        assertNotNull(body.data)
    }

    @Test
    fun `GET llm-configs without token should return Unauthorized`() = integrationTest {
        val response = client.get("/settings/llm-configs")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET llm-configs should return empty list initially`() = settingTest {
        val accessToken = getSignInInfo()?.accessToken
        val response = client.get("/settings/llm-configs") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ApiResponse<Set<LLMConfigResponse>>>()
        assertTrue(body.success)
        assertTrue(body.data?.isEmpty() ?: false)
    }

    @Test
    fun `POST llm-configs without token should return Unauthorized`() = integrationTest {
        val response = client.post("/settings/llm-configs") {
            contentType(ContentType.Application.Json)
            setBody(createLlmConfigJson())
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST llm-configs should create new config`() = settingTest {
        val accessToken = getSignInInfo()?.accessToken
        val response = client.post("/settings/llm-configs") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(createLlmConfigJson())
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ApiResponse<SettingResponse>>()
        assertTrue(body.success)
        assertNotNull(body.data)
        assertTrue(body.data.llmConfigs.isNotEmpty())
    }

    @Test
    fun `GET llm-config by id should return the config`() = settingTest {
        val accessToken = getSignInInfo()?.accessToken

        val createResponse = client.post("/settings/llm-configs") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(createLlmConfigJson())
        }
        val created = createResponse.body<ApiResponse<SettingResponse>>()
        val configId = created.data?.llmConfigs?.first()?.id

        val response = client.get("/settings/llm-configs/$configId") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ApiResponse<LLMConfigResponse>>()
        assertTrue(body.success)
        assertNotNull(body.data)
        assertEquals(configId, body.data.id)
    }

    @Test
    fun `GET llm-config by non-existent id should return NotFound`() = settingTest {
        val accessToken = getSignInInfo()?.accessToken
        val randomId = Uuid.random()

        val response = client.get("/settings/llm-configs/$randomId") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT llm-config should update the config`() = settingTest {
        val accessToken = getSignInInfo()?.accessToken

        val createResponse = client.post("/settings/llm-configs") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(createLlmConfigJson())
        }
        val created = createResponse.body<ApiResponse<SettingResponse>>()
        val configId = created.data?.llmConfigs?.first()?.id

        val response = client.put("/settings/llm-configs/$configId") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "provider": "OPENAI",
                    "baseUrl": "https://api.openai.com/v1",
                    "apiKey": "updated-api-key",
                    "model": "gpt-4",
                    "temperature": 0.8,
                    "maxTokens": 2000,
                    "runOnClient": false
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ApiResponse<SettingResponse>>()
        assertTrue(body.success)
        val updatedConfig = body.data?.llmConfigs?.find { it.id == configId }
        assertEquals("gpt-4", updatedConfig?.model)
    }

    @Test
    fun `DELETE llm-config should remove the config`() = settingTest {
        val accessToken = getSignInInfo()?.accessToken

        val createResponse = client.post("/settings/llm-configs") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(createLlmConfigJson())
        }
        val created = createResponse.body<ApiResponse<SettingResponse>>()
        val configId = created.data?.llmConfigs?.first()?.id

        val response = client.delete("/settings/llm-configs/$configId") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ApiResponse<SettingResponse>>()
        assertTrue(body.success)
        assertTrue(body.data?.llmConfigs?.isEmpty() ?: false)
    }

    @Test
    fun `DELETE active llm-config should return Conflict`() = settingTest {
        val accessToken = getSignInInfo()?.accessToken

        val createResponse = client.post("/settings/llm-configs") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(createLlmConfigJson())
        }
        val created = createResponse.body<ApiResponse<SettingResponse>>()
        val configId = created.data?.llmConfigs?.first()?.id

        client.post("/settings/llm-configs/$configId/select") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        val response = client.delete("/settings/llm-configs/$configId") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `POST select llm-config should set it as active`() = settingTest {
        val accessToken = getSignInInfo()?.accessToken

        val createResponse = client.post("/settings/llm-configs") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(createLlmConfigJson())
        }
        val created = createResponse.body<ApiResponse<SettingResponse>>()
        val configId = created.data?.llmConfigs?.first()?.id

        val response = client.post("/settings/llm-configs/$configId/select") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ApiResponse<SettingResponse>>()
        assertTrue(body.success)
        assertEquals(configId, body.data?.activeLlmConfigId)
    }

    private fun createLlmConfigJson(model: String = "gpt-3.5-turbo"): String = """
        {
            "provider": "OPENAI",
            "baseUrl": "https://api.openai.com/v1",
            "apiKey": "test-api-key",
            "model": "$model",
            "temperature": 0.7,
            "maxTokens": 1000,
            "runOnClient": false
        }
    """.trimIndent()
}
