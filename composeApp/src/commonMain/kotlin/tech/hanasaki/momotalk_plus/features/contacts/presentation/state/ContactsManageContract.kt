package tech.hanasaki.momotalk_plus.features.contacts.presentation.state

import tech.hanasaki.momotalk_plus.core.domain.model.Character

data class ContactsManageState(
    val query: String = "",
    val availableContacts: List<Character> = emptyList(),
    val addedContactIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val processingContactId: String? = null,
)

sealed class ContactsManageIntent {
    data class UpdateQuery(val query: String) : ContactsManageIntent()
    data object LoadAvailableContacts : ContactsManageIntent()
    data class AddContact(val userId: String) : ContactsManageIntent()
    data class RemoveContact(val userId: String) : ContactsManageIntent()
}

sealed class ContactsManageSideEffect {
    data class ShowToast(val message: String) : ContactsManageSideEffect()
}