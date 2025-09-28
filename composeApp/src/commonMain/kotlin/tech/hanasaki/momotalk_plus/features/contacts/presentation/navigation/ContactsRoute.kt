package tech.hanasaki.momotalk_plus.features.contacts.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface ContactsRoute {
    @Serializable
    data object ContactList : ContactsRoute

    @Serializable
    data object AddContact : ContactsRoute

    @Serializable
    data class ContactDetail(val id: String) : ContactsRoute
}