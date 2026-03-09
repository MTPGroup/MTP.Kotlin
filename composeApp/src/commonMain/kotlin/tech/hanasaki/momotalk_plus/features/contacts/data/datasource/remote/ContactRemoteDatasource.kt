package tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import tech.hanasaki.momotalk_plus.core.network.ApiEnvelope
import tech.hanasaki.momotalk_plus.core.network.PageDto

class ContactRemoteDatasource(
    private val client: HttpClient,
) {
    suspend fun getPublicCharacters(
        page: Int? = null,
        limit: Int? = null,
    ): ApiEnvelope<PageDto<ContactItemDto>> =
        client.get("characters/public") {
            parameters {
                page?.let { append("page", it.toString()) }
                limit?.let { append("limit", it.toString()) }
            }
        }.body()

    suspend fun searchCharacter(
        page: Int? = null,
        limit: Int? = null,
        query: String = "",
    ): ApiEnvelope<PageDto<ContactItemDto>> =
        client.get("characters/search") {
            parameters {
                append("q", query)
                page?.let { append("page", it.toString()) }
                limit?.let { append("limit", it.toString()) }
            }
        }.body()

    suspend fun getMyContacts(
        page: Int? = null,
        limit: Int? = null,
    ): ApiEnvelope<PageDto<ContactItemDto>> =
        client.get("characters") {
            parameters {
                page?.let { append("page", it.toString()) }
                limit?.let { append("limit", it.toString()) }
            }
        }.body()

    suspend fun getContactInfo(characterId: String): ApiEnvelope<ContactItemDto> =
        client.get("characters/${characterId}").body()

    suspend fun deleteMyCharacter(characterId: String) {
        client.delete("characters/${characterId}")
    }
}
