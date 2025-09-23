package tech.hanasaki.momotalk_plus.core.data.model

import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility

@Serializable
data class CreateCharacterRequest(
    val name: String,
    val creatorId: String,
    val persona: String,
    val signature: String,
    val avatarUrl: String,
    val visibility: Visibility,
)

@Serializable
data class CreateCharacterResponse(
    val success: Boolean,
)

@Serializable
data class CharacterData(
    val characters: List<Character>,
)

@Serializable
data class ListCharacterResponse(
    val success: Boolean,
    val data: CharacterData,
)

@Serializable
data class CD(
    val characterData: Character,
)

@Serializable
data class CharacterDetailResponse(
    val success: Boolean,
    val data: CD,
)

@Serializable
data class UpdateCharacterRequest(
    val name: String,
    val persona: String,
    val signature: String,
    val avatarUrl: String,
    val visibility: Visibility,
)

@Serializable
data class UpdateCharacterResponse(
    val success: Boolean,
)
