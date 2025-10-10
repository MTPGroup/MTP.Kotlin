package tech.hanasaki.momotalk_plus.features.contacts.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.*
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import tech.hanasaki.momotalk_plus.db.AppDatabase
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.mapper.ContactMapper.toContact
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.mapper.ContactMapper.toContactEntity
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.api.ContactApi
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact
import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository
import kotlin.time.Duration.Companion.days

class ContactRepositoryImpl(
    private val contactApi: ContactApi,
    database: AppDatabase,
) : ContactRepository {
    private val contactDao = database.contactDao()

    private val contactsStore: Store<Unit, List<Contact>> = StoreBuilder
        .from(
            fetcher = Fetcher.of { _: Unit ->
                val response = contactApi.getContacts()
                response.data.contacts
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { _: Unit ->
                    contactDao.getContacts().map { contactEntities ->
                        contactEntities.map { it.toContact() }
                    }
                },
                writer = { _: Unit, contacts: List<Contact> ->
                    contactDao.deleteAll()
                    contactDao.upsertAll(contacts.map { it.toContactEntity() })
                },
                delete = { _: Unit ->
                    contactDao.deleteAll()
                },
                deleteAll = {
                    contactDao.deleteAll()
                }
            )
        )
        .cachePolicy(
            MemoryPolicy.builder<Unit, List<Contact>>()
                .setExpireAfterWrite(7.days)
                .build()
        )
        .build()

    override suspend fun addContact(characterId: String) {
        contactApi.addContact(characterId)
        contactsStore.fresh(Unit)
    }

    override suspend fun deleteContact(characterId: String) {
        contactApi.deleteContact(characterId)
        contactsStore.fresh(Unit)
    }

    override fun getContacts(): Flow<List<Contact>> {
        return contactsStore.stream(
            StoreReadRequest.cached(Unit, refresh = true)
        ).map { response ->
            when (response) {
                is StoreReadResponse.Data -> response.value
                is StoreReadResponse.Loading -> response.dataOrNull() ?: emptyList()
                is StoreReadResponse.Error -> {
                    println("获取联系人失败: ${response.errorMessageOrNull()}")
                    emptyList()
                }

                is StoreReadResponse.NoNewData -> emptyList()
                else -> emptyList()
            }
        }
    }

}