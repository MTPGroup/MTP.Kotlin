package tech.hanasaki.momotalk_plus.features.contacts.data.repository

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.ContactRemoteDatasource
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact
import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository

class ContactRepositoryImpl(private val datasource: ContactRemoteDatasource) : ContactRepository {
    override suspend fun addContact(characterId: String): IResult<Unit, AppError> =
        datasource.addContact(characterId)

    override suspend fun deleteContact(characterId: String): IResult<Unit, AppError> =
        datasource.deleteContact(characterId)

    override suspend fun getContacts(): IResult<List<Contact>, AppError> =
        datasource.getContacts().map { response ->
            response.data.contacts
        }
}