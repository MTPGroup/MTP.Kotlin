package tech.hanasaki.momotalk_plus.features.contacts.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact
import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository

class ListContactUseCase(private val repository: ContactRepository) {
    operator fun invoke(): Flow<List<Contact>> =
        repository.getContacts()
}