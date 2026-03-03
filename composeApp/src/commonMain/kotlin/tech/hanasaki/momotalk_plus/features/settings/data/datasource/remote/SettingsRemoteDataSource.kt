package tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import tech.hanasaki.momotalk_plus.core.network.ApiEnvelope
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote.dto.SettingResponseData
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote.dto.UpdateSettingRequest

class SettingsRemoteDataSource(
    private val client: HttpClient,
) {
    suspend fun getSettings(): ApiEnvelope<SettingResponseData> =
        client.get("settings").body()

    suspend fun updateSettings(request: UpdateSettingRequest): ApiEnvelope<SettingResponseData> =
        client.put("settings") {
            setBody(request)
        }.body()
}
