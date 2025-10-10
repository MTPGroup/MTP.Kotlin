package tech.hanasaki.momotalk_plus.core.data.datasource.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.hanasaki.momotalk_plus.core.data.datasource.mapper.CharacterMapper.toCharacter
import tech.hanasaki.momotalk_plus.core.data.datasource.mapper.CharacterMapper.toCharacterEntity
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.db.AppDatabase

/**
 * Character 本地数据源
 * 使用 SQLDelight 进行持久化缓存
 */
class CharacterLocalDataSource(
    db: AppDatabase,
) {
    private val characterDao = db.characterDao()

    /**
     * 获取角色列表的 Flow
     */
    fun getCharactersFlow(): Flow<List<Character>> {
        return characterDao.getAllCharacter().map { characters ->
            characters.map { it.toCharacter() }
        }
    }

    /**
     * 保存角色列表到缓存
     */
    suspend fun saveCharacters(characters: List<Character>) {
        characters.forEach { character ->
            characterDao.insertOrReplace(character.toCharacterEntity())
        }
    }

    /**
     * 获取单个角色的 Flow
     */
    fun getCharacterFlow(id: String): Flow<Character?> {
        return characterDao.getCharacterById(id).map { it?.toCharacter() }
    }

    /**
     * 保存单个角色到缓存
     */
    suspend fun saveCharacter(character: Character) {
        characterDao.insertOrReplace(character.toCharacterEntity())
    }

    /**
     * 清除所有角色缓存
     */
    suspend fun clearCharacters() {
        characterDao.deleteAllCharacter()
    }

    /**
     * 清除单个角色缓存
     */
    suspend fun clearCharacter(id: String) {
        characterDao.deleteCharacter(id)
    }
}
