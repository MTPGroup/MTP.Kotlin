package tech.hanasaki.momotalk_plus.core.data.repository

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.CharacterRemoteDatasource
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository

class CharacterRepositoryImpl(private val characterRemoteDatasource: CharacterRemoteDatasource) : CharacterRepository {
    override suspend fun createCharacter(
        name: String,
        creatorId: String,
        persona: String,
        signature: String,
        avatarUrl: String,
        visibility: Visibility,
    ): IResult<Unit, AppError> =
        characterRemoteDatasource.createCharacter(
            name,
            persona,
            signature,
            avatarUrl,
            visibility,
        ).map { }

    override suspend fun deleteCharacter(id: String): IResult<Unit, AppError> =
        characterRemoteDatasource.deleteCharacter(id)

    override suspend fun getAvailableCharacters(): IResult<List<Character>, AppError> =
        characterRemoteDatasource.listCharacters().map { listCharacterResponse ->
            listCharacterResponse.data.characters
        }

    override suspend fun getCharacterById(id: String): IResult<Character, AppError> =
        characterRemoteDatasource.searchCharacterById(id).map { characterDetailResponse ->
            characterDetailResponse.data
        }

    override suspend fun updateCharacter(
        id: String,
        name: String,
        persona: String,
        signature: String,
        avatarUrl: String,
        visibility: Visibility,
    ): IResult<Unit, AppError> =
        characterRemoteDatasource.updateCharacter(
            id,
            name,
            persona,
            signature,
            avatarUrl,
            visibility,
        ).map { }
}