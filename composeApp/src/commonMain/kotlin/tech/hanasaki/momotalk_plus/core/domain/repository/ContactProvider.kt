package tech.hanasaki.momotalk_plus.core.domain.repository

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult

data class ContactInfo(
    val id: String,
    val name: String,
    val signature: String,
    val avatarUrl: String,
)

interface ContactProvider {
    suspend fun getAvailableContacts(): IResult<List<ContactInfo>, AppError>
}