package tech.hanasaki.momotalk_plus.features.contacts.data.datasource.local.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.local.entity.ContactEntity

class ContactDao {
    private val contacts = MutableStateFlow<List<ContactEntity>>(emptyList())

    fun getContacts(): Flow<List<ContactEntity>> = contacts

    suspend fun addContact(contact: ContactEntity) {
        contacts.value = contacts.value + contact
    }

    suspend fun upsert(contact: ContactEntity) {
        contacts.value = contacts.value.filterNot { it.id == contact.id } + contact
    }

    suspend fun upsertAll(contactList: List<ContactEntity>) {
        val merged = contacts.value.associateBy { it.id }.toMutableMap()
        contactList.forEach { merged[it.id] = it }
        contacts.value = merged.values.toList()
    }

    suspend fun deleteContact(characterId: String) {
        contacts.value = contacts.value.filterNot { it.id == characterId }
    }

    suspend fun deleteAll() {
        contacts.value = emptyList()
    }
}
