package tech.hanasaki.momotalk_plus.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.path
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import tech.hanasaki.momotalk_plus.core.data.datasource.local.CharacterLocalDataSource
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.CharacterDetailResponse
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.CharacterListResponse
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.CreateCharacterRequest
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.UpdateCharacterRequest
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository

/**
 * Character Store - 管理角色数据的缓存和网络请求
 * 提供智能的缓存策略和数据流
 */
class CharacterRepositoryImpl(
    private val supabase: SupabaseClient,
    private val localDataSource: CharacterLocalDataSource,
) : CharacterRepository {

    private suspend fun refreshCharacterList() {
        runCatching {
            val response = supabase.functions.invoke("characters") {
                url { path("characters") }
                method = HttpMethod.Get
            }
            val characters = response.body<CharacterListResponse>().data.characters
            localDataSource.clearCharacters()
            localDataSource.saveCharacters(characters)
        }.onFailure { it.printStackTrace() }
    }

    private suspend fun refreshCharacter(id: String) {
        runCatching {
            val response = supabase.functions.invoke("characters") {
                url { path("characters", id) }
                method = HttpMethod.Get
            }
            val character = response.body<CharacterDetailResponse>().data
            localDataSource.saveCharacter(character)
        }.onFailure { it.printStackTrace() }
    }

    /**
     * 获取角色列表数据流
     * @param refresh 是否强制刷新（兼容旧签名，若为 true 则触发刷新）
     */
    fun streamCharacters(refresh: Boolean = false): Flow<List<Character>> {
        return localDataSource.getCharactersFlow()
            .onStart {
                refreshCharacterList()
            }
    }

    /**
     * 获取单个角色数据流
     * @param id 角色ID
     * @param refresh 是否强制刷新（兼容旧签名）
     */
    fun streamCharacter(id: String, refresh: Boolean = false): Flow<Character?> {
        return localDataSource.getCharacterFlow(id)
            .onStart {
                refreshCharacter(id)
            }
    }

    override suspend fun createCharacter(
        name: String,
        creatorId: String,
        persona: String,
        signature: String,
        avatarUrl: String,
        visibility: Visibility,
    ) {
        val request = CreateCharacterRequest(
            name = name,
            signature = signature,
            avatarUrl = avatarUrl,
            persona = persona,
            visibility = visibility
        )

        try {
            supabase.functions.invoke("characters") {
                url { path("characters") }
                method = HttpMethod.Post
                setBody(request)
            }

            refreshCharacterList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteCharacter(id: String) {
        try {
            supabase.functions.invoke("characters") {
                url { path("characters", id) }
                method = HttpMethod.Delete
            }

            localDataSource.clearCharacter(id)
            refreshCharacterList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAvailableCharacters(): Flow<List<Character>> {
        return localDataSource.getCharactersFlow()
            .onStart {
                refreshCharacterList()
            }
            .catch {
                emit(emptyList())
            }
    }

    override fun getCharacterById(id: String): Flow<Character?> {
        return localDataSource.getCharacterFlow(id)
            .onStart {
                refreshCharacter(id)
            }
    }

    override suspend fun updateCharacter(
        id: String,
        name: String,
        persona: String,
        signature: String,
        avatarUrl: String,
        visibility: Visibility,
    ) {
        try {
            val request = UpdateCharacterRequest(
                name = name,
                signature = signature,
                avatarUrl = avatarUrl,
                persona = persona,
                visibility = visibility
            )

            supabase.functions.invoke("characters") {
                url { path("characters", id) }
                method = HttpMethod.Put
                setBody(request)
            }

            refreshCharacterList()
            refreshCharacter(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
