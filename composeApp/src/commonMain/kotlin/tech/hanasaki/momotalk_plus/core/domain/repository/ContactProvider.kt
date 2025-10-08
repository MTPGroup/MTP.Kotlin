package tech.hanasaki.momotalk_plus.core.domain.repository

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult

data class ContactInfo(
    val id: String,
    val name: String,
    val signature: String,
    val avatarUrl: String,
)

interface ContactProvider {
    suspend fun getAvailableContacts(): IResult<List<ContactInfo>, AppError>
}