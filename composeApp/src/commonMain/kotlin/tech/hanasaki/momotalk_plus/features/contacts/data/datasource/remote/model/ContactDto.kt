package tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.model

import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact

@Serializable
data class ContactListData(
    val contacts: List<Contact>,
)

@Serializable
data class ContactListResponse(
    val success: Boolean,
    val data: ContactListData,
)
