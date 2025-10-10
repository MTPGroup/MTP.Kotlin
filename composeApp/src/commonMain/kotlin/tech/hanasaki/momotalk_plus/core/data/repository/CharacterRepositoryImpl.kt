package tech.hanasaki.momotalk_plus.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.*
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import tech.hanasaki.momotalk_plus.core.data.datasource.local.CharacterLocalDataSource
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.api.CharacterApi
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.CreateCharacterRequest
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.UpdateCharacterRequest
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository
import kotlin.time.Duration.Companion.minutes

/**
 * Character Store - 管理角色数据的缓存和网络请求
 * 提供智能的缓存策略和数据流
 */
class CharacterRepositoryImpl(
    private val characterApi: CharacterApi,
    private val localDataSource: CharacterLocalDataSource,
) : CharacterRepository {
    /**
     * 角色列表 Store
     * 缓存策略: 30分钟后过期
     */
    val listStore: Store<Unit, List<Character>> = StoreBuilder
        .from(
            fetcher = Fetcher.of { _: Unit ->
                val response = characterApi.getCharacters()
                response.data.characters
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { _: Unit ->
                    localDataSource.getCharactersFlow()
                },
                writer = { _: Unit, characters: List<Character> ->
                    localDataSource.saveCharacters(characters)
                },
                delete = { _: Unit ->
                    localDataSource.clearCharacters()
                },
                deleteAll = {
                    localDataSource.clearCharacters()
                }
            )
        )
        .cachePolicy(
            MemoryPolicy.builder<Unit, List<Character>>()
                .setExpireAfterWrite(30.minutes)
                .build()
        )
        .build()

    /**
     * 单个角色详情 Store
     * 缓存策略: 15分钟后过期
     */
    val detailStore: Store<String, Character> = StoreBuilder
        .from(
            fetcher = Fetcher.of { id: String ->
                val response = characterApi.getCharacterById(id)
                response.data
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { id: String ->
                    localDataSource.getCharacterFlow(id)
                },
                writer = { id: String, character: Character ->
                    localDataSource.saveCharacter(character)
                },
                delete = { id: String ->
                    localDataSource.clearCharacter(id)
                },
                deleteAll = {
                    localDataSource.clearCharacters()
                }
            )
        )
        .cachePolicy(
            MemoryPolicy.builder<String, Character>()
                .setExpireAfterWrite(15.minutes)
                .build()
        )
        .build()

    /**
     * 获取角色列表数据流
     * @param refresh 是否强制刷新
     */
    fun streamCharacters(refresh: Boolean = false): Flow<StoreReadResponse<List<Character>>> {
        return listStore.stream(
            StoreReadRequest.cached(Unit, refresh = refresh)
        )
    }

    /**
     * 获取单个角色数据流
     * @param id 角色ID
     * @param refresh 是否强制刷新
     */
    fun streamCharacter(id: String, refresh: Boolean = false): Flow<StoreReadResponse<Character>> {
        return detailStore.stream(
            StoreReadRequest.Companion.cached(id, refresh = refresh)
        )
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
            characterApi.createCharacter(request)

            // 刷新列表缓存
            listStore.fresh(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteCharacter(id: String) {
        try {
            characterApi.deleteCharacter(id)

            // 刷新缓存
            listStore.fresh(Unit)
            detailStore.fresh(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAvailableCharacters(): Flow<List<Character>> {
        return listStore.stream(
            StoreReadRequest.cached(Unit, false)
        )
            .map { response ->
                when (response) {
                    is StoreReadResponse.Data -> response.value
                    is StoreReadResponse.Loading -> response.dataOrNull() ?: emptyList()
                    else -> emptyList()
                }
            }
            .catch {
                emit(emptyList())
            }
    }

    override fun getCharacterById(id: String): Flow<Character?> {
        return detailStore.stream(
            StoreReadRequest.cached(id, true)
        ).map { response ->
            when (response) {
                is StoreReadResponse.Data -> response.value
                is StoreReadResponse.Loading -> response.dataOrNull()
                else -> null
            }
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

            characterApi.updateCharacter(id, request)

            // 刷新缓存
            listStore.fresh(Unit)
            detailStore.fresh(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}