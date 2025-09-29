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

    companion object {
        fun fromRoute(route: String?): ContactsRoute? {
            if (route == null) return null
            return when {
                route == "contact_list" -> ContactList
                route == "add_contact" -> AddContact
                route.startsWith("contact_detail/") -> {
                    val id = route.removePrefix("contact_detail/")
                    ContactDetail(id)
                }

                else -> null
            }
        }
    }
}