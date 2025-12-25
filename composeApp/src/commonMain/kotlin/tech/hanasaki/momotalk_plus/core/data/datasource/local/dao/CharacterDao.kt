package tech.hanasaki.momotalk_plus.core.data.datasource.local.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.CharacterEntity

class CharacterDao {
    private val characters = MutableStateFlow<List<CharacterEntity>>(emptyList())

    fun getAllCharacter(): Flow<List<CharacterEntity>> = characters

    fun getCharacterById(id: String): Flow<CharacterEntity?> =
        characters.map { list -> list.firstOrNull { it.id == id } }

    suspend fun insertOrReplace(character: CharacterEntity) {
        characters.value = characters.value.filterNot { it.id == character.id } + character
    }

    suspend fun deleteCharacter(id: String) {
        characters.value = characters.value.filterNot { it.id == id }
    }

    suspend fun deleteAllCharacter() {
        characters.value = emptyList()
    }
}
