package tech.hanasaki.momotalk_plus.features.contacts.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility
import tech.hanasaki.momotalk_plus.core.network.*
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.ContactItemDto
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.ContactRemoteDatasource
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact
import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository

class ContactRepositoryImpl(
    private val remote: ContactRemoteDatasource,
    private val errorMapper: NetworkErrorMapper,
) : ContactRepository {

    override suspend fun addContact(characterId: String) {
        throw UnsupportedOperationException("当前后端未提供添加联系人接口")
    }

    override suspend fun deleteContact(characterId: String) {
        when (val result = callRawApi(errorMapper) { remote.deleteMyCharacter(characterId) }) {
            is AppResult.Success -> Unit
            is AppResult.Failure -> throw AppErrorException(result.error)
        }
    }

    override fun getContacts(): Flow<List<Contact>> = flow {
        when (val result = callApi(errorMapper) { remote.getPublicCharacters(page = 1, limit = 100) }) {
            is AppResult.Success -> emit(result.data.items.map { it.toDomain() })
            is AppResult.Failure -> throw AppErrorException(result.error)
        }
    }
}

private fun ContactItemDto.toDomain(): Contact = Contact(
    id = id,
    creatorId = author.id,
    name = name,
    signature = bio.orEmpty(),
    persona = originPrompt.orEmpty(),
    avatarUrl = avatar.orEmpty(),
    visibility = if (isPublic) Visibility.PUBLIC else Visibility.PRIVATE,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)
