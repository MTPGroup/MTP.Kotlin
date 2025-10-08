package tech.hanasaki.momotalk_plus.features.contacts.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact
import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository

class ListContactUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke(): IResult<List<Contact>, AppError> =
        repository.getContacts().mapError { error ->
            AppError("获取联系人列表失败: $error")
        }
}