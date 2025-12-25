package tech.hanasaki.momotalk_plus.features.profile.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.path
import tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.dto.UpdateUserRequest
import tech.hanasaki.momotalk_plus.features.profile.domain.repository.ProfileRepository

/**
 * ProfileRepositoryImpl - 个人资料仓库实现
 */
class ProfileRepositoryImpl(
    private val supabase: SupabaseClient,
) : ProfileRepository {
    override suspend fun updateUserProfile(
        id: String,
        name: String,
        avatar: String?,
    ) {
        supabase.functions.invoke("profiles") {
            url { path("profiles") }
            method = HttpMethod.Put
            setBody(UpdateUserRequest(username = name, avatar = avatar))
        }
    }
}
