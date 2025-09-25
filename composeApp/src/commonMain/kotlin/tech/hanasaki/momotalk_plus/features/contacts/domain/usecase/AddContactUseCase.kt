package tech.hanasaki.momotalk_plus.features.contacts.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository

class AddContactUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke(userId: String): IResult<Unit, AppError> =
        repository.addContact(userId).mapError { error ->
            AppError("添加联系人失败: $error")
        }
}