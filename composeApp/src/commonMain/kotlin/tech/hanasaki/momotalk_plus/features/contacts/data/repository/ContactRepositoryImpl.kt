package tech.hanasaki.momotalk_plus.features.contacts.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.http.path
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import tech.hanasaki.momotalk_plus.db.AppDatabase
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.mapper.ContactMapper.toContact
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.mapper.ContactMapper.toContactEntity
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.dto.ContactListResponse
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact
import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository

class ContactRepositoryImpl(
    private val supabase: SupabaseClient,
    database: AppDatabase,
) : ContactRepository {
    private val contactDao = database.contactDao()

    private suspend fun refreshContacts() {
        runCatching {
            val response = supabase.functions.invoke("contacts") {
                url { path("contacts") }
                method = HttpMethod.Get
            }
            val contacts = response.body<ContactListResponse>().data.contacts
            contactDao.deleteAll()
            contactDao.upsertAll(contacts.map { it.toContactEntity() })
        }.onFailure { it.printStackTrace() }
    }

    override suspend fun addContact(characterId: String) {
        supabase.functions.invoke("contacts") {
            url { path("contacts", characterId) }
            method = HttpMethod.Post
        }
        refreshContacts()
    }

    override suspend fun deleteContact(characterId: String) {
        supabase.functions.invoke("contacts") {
            url { path("contacts", characterId) }
            method = HttpMethod.Delete
        }
        refreshContacts()
    }

    override fun getContacts(): Flow<List<Contact>> {
        return contactDao.getContacts()
            .map { contactEntities ->
                contactEntities.map { it.toContact() }
            }
            .onStart {
                refreshContacts()
            }
    }
}
