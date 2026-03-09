package tech.hanasaki.momotalk_plus.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import tech.hanasaki.momotalk_plus.core.data.datasource.local.CharacterLocalDataSource
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.CharacterRemoteDataSource
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.CharacterDto
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.CreateCharacterRequest
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.UpdateCharacterRequest
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.core.domain.model.Creator
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository
import tech.hanasaki.momotalk_plus.core.network.AppErrorException
import tech.hanasaki.momotalk_plus.core.network.AppResult
import tech.hanasaki.momotalk_plus.core.network.NetworkErrorMapper
import tech.hanasaki.momotalk_plus.core.network.callApi
import tech.hanasaki.momotalk_plus.core.network.callRawApi

/**
 * Character Store - 管理角色数据的缓存和网络请求
 * 提供智能的缓存策略和数据流
 */
class CharacterRepositoryImpl(
    private val remote: CharacterRemoteDataSource,
    private val errorMapper: NetworkErrorMapper,
    private val localDataSource: CharacterLocalDataSource,
) : CharacterRepository {

    private suspend fun refreshCharacterList() {
        when (val result = callApi(errorMapper) { remote.getMyCharacters(page = 1, limit = 100) }) {
            is AppResult.Success -> {
                localDataSource.clearCharacters()
                localDataSource.saveCharacters(result.data.items.map { it.toDomain() })
            }

            is AppResult.Failure -> Unit
        }
    }

    private suspend fun refreshCharacter(id: String) {
        when (val result = callApi(errorMapper) { remote.getCharacter(id) }) {
            is AppResult.Success -> {
                localDataSource.saveCharacter(result.data.toDomain())
            }

            is AppResult.Failure -> Unit
        }
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
            avatar = avatarUrl.ifBlank { null },
            bio = signature.ifBlank { null },
            originPrompt = persona.ifBlank { null },
            isPublic = visibility == Visibility.PUBLIC,
        )

        when (val result = callApi(errorMapper) { remote.createCharacter(request) }) {
            is AppResult.Success -> {
                localDataSource.saveCharacter(result.data.toDomain())
                refreshCharacterList()
            }

            is AppResult.Failure -> throw AppErrorException(result.error)
        }
    }

    override suspend fun deleteCharacter(id: String) {
        when (val result = callRawApi(errorMapper) { remote.deleteCharacter(id) }) {
            is AppResult.Success -> {
                localDataSource.clearCharacter(id)
                refreshCharacterList()
            }

            is AppResult.Failure -> throw AppErrorException(result.error)
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
        val request = UpdateCharacterRequest(
            name = name,
            avatar = avatarUrl.ifBlank { null },
            bio = signature.ifBlank { null },
            originPrompt = persona.ifBlank { null },
            isPublic = visibility == Visibility.PUBLIC,
        )

        when (val result = callApi(errorMapper) { remote.updateCharacter(id, request) }) {
            is AppResult.Success -> {
                localDataSource.saveCharacter(result.data.toDomain())
                refreshCharacterList()
            }

            is AppResult.Failure -> throw AppErrorException(result.error)
        }
    }
}

private fun CharacterDto.toDomain(): Character = Character(
    id = id,
    creatorId = author?.id.orEmpty(),
    name = name,
    signature = bio.orEmpty(),
    persona = originPrompt.orEmpty(),
    avatarUrl = avatar.orEmpty(),
    visibility = if (isPublic) Visibility.PUBLIC else Visibility.PRIVATE,
    createdAt = createdAt,
    updatedAt = updatedAt,
    creator = Creator(
        id = author?.id.orEmpty(),
        name = author?.name.orEmpty(),
        image = author?.avatar,
        username = null,
    ),
)
