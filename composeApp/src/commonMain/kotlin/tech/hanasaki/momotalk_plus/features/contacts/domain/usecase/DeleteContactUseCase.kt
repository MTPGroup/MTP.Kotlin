package tech.hanasaki.momotalk_plus.features.contacts.domain.usecase

import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository

class DeleteContactUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke(userId: String) =
        repository.deleteContact(userId)
}