package tech.hanasaki.momotalk_plus.core.data.datasource.remote.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.PATCH
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.SettingsResponse
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.UpdateSettingsRequest

interface SettingApi {
    /**
     * 获取用户设置
     */
    @GET("settings")
    suspend fun fetchSettings(): SettingsResponse

    /**
     * 更新用户设置
     */
    @Headers("Content-Type: application/json")
    @PATCH("settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest)
}