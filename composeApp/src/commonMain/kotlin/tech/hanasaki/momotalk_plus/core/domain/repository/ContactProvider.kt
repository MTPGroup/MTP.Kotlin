package tech.hanasaki.momotalk_plus.core.domain.repository

import kotlinx.coroutines.flow.Flow

data class ContactInfo(
    val id: String,
    val name: String,
    val signature: String,
    val avatarUrl: String,
)

interface ContactProvider {
    fun getAvailableContacts(): Flow<List<ContactInfo>>
}