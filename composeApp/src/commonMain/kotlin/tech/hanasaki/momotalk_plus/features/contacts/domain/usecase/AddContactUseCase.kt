package tech.hanasaki.momotalk_plus.features.contacts.domain.usecase

import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository

class AddContactUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke(userId: String): Result<Unit> = try {
        repository.addContact(userId)
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}