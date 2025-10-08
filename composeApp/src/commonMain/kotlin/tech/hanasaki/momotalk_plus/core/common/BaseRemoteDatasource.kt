package tech.hanasaki.momotalk_plus.core.common

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * 通用API请求处理器
 * 处理统一的API响应格式
 */
open class BaseRemoteDatasource(val client: HttpClient) {
    protected val baseUrl = "http://localhost:3001/api"

    /**
     * 执行POST请求
     */
    suspend inline fun <reified T : Any> post(
        url: String,
        requestBody: Any? = null,
        headers: Headers? = null,
    ): IResult<T, AppError> {
        return try {
            val response: T = client.post(url) {
                contentType(ContentType.Application.Json)
                if (requestBody != null) {
                    setBody(requestBody)
                }
                headers {
                    if (headers != null) {
                        appendAll(headers)
                    }
                }
            }.body()

            IResult.Success(response)
        } catch (e: ClientRequestException) {
            try {
                val errorBody = e.response.body<Any>()
                IResult.Error(
                    AppError(errorBody.toString())
                )
            } catch (_: Exception) {
                IResult.Error(
                    AppError(
                        "客户端请求失败，无法解析错误信息。"
                    )
                )
            }
        } catch (e: ServerResponseException) {
            IResult.Error(
                AppError(
                    "服务器错误: ${e.response.status}"
                )
            )
        } catch (e: RedirectResponseException) {
            IResult.Error(
                AppError(
                    "重定向错误: ${e.response.status}"
                )
            )
        } catch (e: Exception) {
            IResult.Error(AppError(e.message ?: "未知错误"))
        }
    }

    /**
     * 执行GET请求
     */
    suspend inline fun <reified T : Any> get(
        url: String,
        params: Map<String, Any> = emptyMap(),
        headers: Headers? = null,
    ): IResult<T, AppError> {
        return try {
            val response: T = client.get(url) {
                parameters {
                    params.forEach { (key, value) ->
                        append(key, value.toString())
                    }
                }
                headers {
                    if (headers != null) {
                        appendAll(headers)
                    }
                }
            }.body()
            IResult.Success(response)
        } catch (e: ClientRequestException) {
            try {
                val errorBody = e.response.body<Any>()
                IResult.Error(
                    AppError(errorBody.toString())
                )
            } catch (_: Exception) {
                IResult.Error(
                    AppError(
                        "客户端请求失败，无法解析错误信息。"
                    )
                )
            }
        } catch (e: ServerResponseException) {
            IResult.Error(
                AppError(
                    "服务器错误: ${e.response.status}"
                )
            )
        } catch (e: RedirectResponseException) {
            IResult.Error(
                AppError(
                    "重定向错误: ${e.response.status}"
                )
            )
        } catch (e: Exception) {
            IResult.Error(AppError(e.message ?: "未知错误"))
        }
    }

    /**
     * 执行PUT请求
     */
    suspend inline fun <reified T : Any> put(
        url: String,
        requestBody: Any? = null,
        headers: Headers? = null,
    ): IResult<T, AppError> {
        return try {
            val response = client.put(url) {
                contentType(ContentType.Application.Json)
                if (requestBody != null) {
                    setBody(requestBody)
                }
                headers {
                    if (headers != null) {
                        appendAll(headers)
                    }
                }
            }.body<T>()
            IResult.Success(response)
        } catch (e: ClientRequestException) {
            try {
                val errorBody = e.response.body<Any>()
                IResult.Error(
                    AppError(errorBody.toString())
                )
            } catch (_: Exception) {
                IResult.Error(
                    AppError(
                        "客户端请求失败，无法解析错误信息。"
                    )
                )
            }
        } catch (e: ServerResponseException) {
            IResult.Error(
                AppError(
                    "服务器错误: ${e.response.status}"
                )
            )
        } catch (e: RedirectResponseException) {
            IResult.Error(
                AppError(
                    "重定向错误: ${e.response.status}"
                )
            )
        } catch (e: Exception) {
            IResult.Error(AppError(e.message ?: "未知错误"))
        }
    }

    /**
     * 执行DELETE请求
     */
    suspend inline fun <reified T : Any> delete(
        url: String,
        headers: Headers? = null,
    ): IResult<T, AppError> {
        return try {
            val response = client.delete(url) {
                headers {
                    if (headers != null) {
                        appendAll(headers)
                    }
                }
            }.body<T>()
            IResult.Success(response)
        } catch (e: ClientRequestException) {
            try {
                val errorBody = e.response.body<Any>()
                IResult.Error(
                    AppError(errorBody.toString())
                )
            } catch (_: Exception) {
                IResult.Error(
                    AppError(
                        "客户端请求失败，无法解析错误信息。"
                    )
                )
            }
        } catch (e: ServerResponseException) {
            IResult.Error(
                AppError(
                    "服务器错误: ${e.response.status}"
                )
            )
        } catch (e: RedirectResponseException) {
            IResult.Error(
                AppError(
                    "重定向错误: ${e.response.status}"
                )
            )
        } catch (e: Exception) {
            IResult.Error(AppError(e.message ?: "未知错误"))
        }
    }

}