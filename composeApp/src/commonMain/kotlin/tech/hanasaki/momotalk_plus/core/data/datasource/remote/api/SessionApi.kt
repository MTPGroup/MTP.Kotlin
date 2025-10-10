package tech.hanasaki.momotalk_plus.core.data.datasource.remote.api

import de.jensklingenberg.ktorfit.http.GET
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.GetSessionResponse

interface SessionApi {
    /**
     * 获取会话信息
     */
    @GET("auth/get-session")
    suspend fun getSessionInfo(): GetSessionResponse

    /**
     * 用户登出
     */
    @GET("auth/logout")
    suspend fun logout()
}