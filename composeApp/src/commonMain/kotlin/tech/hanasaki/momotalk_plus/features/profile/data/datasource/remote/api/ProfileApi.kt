package tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.dto.UpdateUserRequest
import tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.dto.UpdateUserResponse

interface ProfileApi {
    @Headers("Content-Type: application/json")
    @POST("auth/update-user")
    suspend fun updateUser(@Body request: UpdateUserRequest): UpdateUserResponse
}