package tech.hanasaki.momotalk_plus.features.contacts.presentation.state

import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact

data class ContactListState(
    val searchQuery: String = "",
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed class ContactListIntent {
    data class SearchQueryChanged(val query: String) : ContactListIntent()
    data object ClearSearchQuery : ContactListIntent()
    data object AddContactClicked : ContactListIntent()
    data class ContactClicked(val contactId: String) : ContactListIntent()
    data object LoadContacts : ContactListIntent()
    data object RefreshContacts : ContactListIntent()
}

sealed class ContactListSideEffect {
    data class NavigateToContactDetail(val contactId: String) : ContactListSideEffect()
    data object NavigateToAddContact : ContactListSideEffect()
    data class ShowErrorMessage(val message: String) : ContactListSideEffect()
}