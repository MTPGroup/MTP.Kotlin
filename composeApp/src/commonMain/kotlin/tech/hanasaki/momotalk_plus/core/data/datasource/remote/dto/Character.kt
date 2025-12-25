package tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto

import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility

@Serializable
data class CreateCharacterRequest(
    val name: String,
    val signature: String,
    val avatarUrl: String,
    val persona: String,
    val visibility: Visibility,
)

@Serializable
data class CreateCharacterResponse(
    val success: Boolean,
    val message: String,
)

@Serializable
data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean,
)

@Serializable
data class CharacterListData(
    val characters: List<Character>,
    val pagination: Pagination,
)

@Serializable
data class CharacterListResponse(
    val success: Boolean,
    val data: CharacterListData,
)

@Serializable
data class CharacterDetailResponse(
    val success: Boolean,
    val data: Character,
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
    val message: String,
)
