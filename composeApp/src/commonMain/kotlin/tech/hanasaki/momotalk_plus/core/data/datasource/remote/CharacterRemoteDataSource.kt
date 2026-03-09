package tech.hanasaki.momotalk_plus.core.data.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.CharacterDto
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.CreateCharacterRequest
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.UpdateCharacterRequest
import tech.hanasaki.momotalk_plus.core.network.ApiEnvelope
import tech.hanasaki.momotalk_plus.core.network.PageDto

class CharacterRemoteDataSource(
    private val client: HttpClient,
) {
    suspend fun getMyCharacters(
        page: Int? = null,
        limit: Int? = null,
    ): ApiEnvelope<PageDto<CharacterDto>> =
        client.get("characters") {
            url {
                page?.let { parameters.append("page", it.toString()) }
                limit?.let { parameters.append("limit", it.toString()) }
            }
        }.body()

    suspend fun getCharacter(characterId: String): ApiEnvelope<CharacterDto> =
        client.get("characters/$characterId").body()

    suspend fun createCharacter(request: CreateCharacterRequest): ApiEnvelope<CharacterDto> =
        client.post("characters") { setBody(request) }.body()

    suspend fun updateCharacter(
        characterId: String,
        request: UpdateCharacterRequest,
    ): ApiEnvelope<CharacterDto> =
        client.put("characters/$characterId") { setBody(request) }.body()

    suspend fun deleteCharacter(characterId: String) {
        client.delete("characters/$characterId")
    }
}
