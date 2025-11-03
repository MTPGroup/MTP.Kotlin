package tech.hanasaki.momotalk_plus.features.profile.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import tech.hanasaki.momotalk_plus.core.domain.model.User
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
        supabase.from("profiles")
            .update({
                User::name setTo name
                User::avatar setTo avatar
            }) {
                filter {
                    User::id eq id
                }
            }
    }
}
