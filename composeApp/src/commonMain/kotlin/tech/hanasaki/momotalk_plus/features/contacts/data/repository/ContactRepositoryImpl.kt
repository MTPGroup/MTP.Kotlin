package tech.hanasaki.momotalk_plus.features.contacts.data.repository

import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.ContactRemoteDatasource
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.ContactError
import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository

class ContactRepositoryImpl(private val datasource: ContactRemoteDatasource) : ContactRepository {
    override suspend fun addContact(userId: String): Result<Unit, ContactError> =
        datasource.addContact(userId)

    override suspend fun deleteContact(userId: String): Result<Unit, ContactError> =
        datasource.deleteContact(userId)

    override suspend fun getContacts(): Result<List<Contact>, ContactError> =
        datasource.getContacts()
}