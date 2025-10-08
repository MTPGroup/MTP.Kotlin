package tech.hanasaki.momotalk_plus.features.contacts.data.repository

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.core.domain.repository.ContactInfo
import tech.hanasaki.momotalk_plus.core.domain.repository.ContactProvider
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.ListContactUseCase

class ContactProviderImpl(
    private val listContactUseCase: ListContactUseCase,
) : ContactProvider {
    override suspend fun getAvailableContacts(): IResult<List<ContactInfo>, AppError> {
        return when (val result = listContactUseCase()) {
            is IResult.Success -> {
                IResult.Success(
                    result.data.map { contact ->
                        ContactInfo(
                            id = contact.id,
                            name = contact.name,
                            signature = contact.signature,
                            avatarUrl = contact.avatarUrl
                        )
                    }
                )
            }

            is IResult.Error -> result
        }
    }
}