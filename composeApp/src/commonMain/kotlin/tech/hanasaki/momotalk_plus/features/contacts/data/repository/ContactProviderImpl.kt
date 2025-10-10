package tech.hanasaki.momotalk_plus.features.contacts.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.hanasaki.momotalk_plus.core.domain.repository.ContactInfo
import tech.hanasaki.momotalk_plus.core.domain.repository.ContactProvider
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.ListContactUseCase

class ContactProviderImpl(
    private val listContactUseCase: ListContactUseCase,
) : ContactProvider {
    override fun getAvailableContacts(): Flow<List<ContactInfo>> {
        return listContactUseCase()
            .map { contacts ->
                contacts.map {
                    ContactInfo(
                        id = it.id,
                        name = it.name,
                        signature = it.signature,
                        avatarUrl = it.avatarUrl
                    )
                }
            }
    }
}