package tech.hanasaki.momotalk_plus.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import tech.hanasaki.momotalk_plus.core.auth.TokenStore

object ApiConfig {
    const val BASE_URL = "http://localhost:8002/v1/"
}

fun createHttpClient(
    tokenStore: TokenStore,
    json: Json,
    baseUrl: String = ApiConfig.BASE_URL,
): HttpClient = HttpClient {
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                println(message)
            }
        }
        level = LogLevel.INFO
    }

    install(ContentNegotiation) {
        json(json)
    }

    install(SSE)

    install(Auth) {
        bearer {
            loadTokens {
                tokenStore.get()?.let { BearerTokens(it.accessToken, it.refreshToken) }
            }

            refreshTokens {
                val tokens = client.refreshAuthTokens(tokenStore) ?: return@refreshTokens null
                BearerTokens(tokens.accessToken, tokens.refreshToken)
            }
        }
    }

    install(DefaultRequest) {
        url.takeFrom(baseUrl)
        contentType(ContentType.Application.Json)
    }
}
