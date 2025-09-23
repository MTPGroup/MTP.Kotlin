package tech.hanasaki.momotalk_plus.features.contacts.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository

class DeleteContactUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke(userId: String): Result<Unit, AppError> =
        repository.deleteContact(userId).mapError { error ->
            AppError("删除联系人失败: $error")
        }
}