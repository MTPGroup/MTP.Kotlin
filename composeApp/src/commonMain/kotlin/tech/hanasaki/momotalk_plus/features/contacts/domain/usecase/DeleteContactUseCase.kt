package tech.hanasaki.momotalk_plus.features.contacts.domain.usecase

import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository

class DeleteContactUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke(userId: String): Result<Unit> = try {
        repository.deleteContact(userId)
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}