package tech.hanasaki.momotalk_plus.features.contacts.presentation.state

import tech.hanasaki.momotalk_plus.core.domain.model.Character

data class ContactDetailState(
    val showDialog: Boolean = false,
    val contact: Character = Character.default(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed class ContactDetailIntent {
    data class LoadContact(val userId: String) : ContactDetailIntent()
    data object ShowDeleteDialog : ContactDetailIntent()
    data class DeleteContact(val userId: String) : ContactDetailIntent()
}

sealed class ContactDetailSideEffect {
    data class ShowErrorMessage(val message: String) : ContactDetailSideEffect()
    data class NavigateToChat(val userId: String) : ContactDetailSideEffect()
    data object NavigateToContactsList : ContactDetailSideEffect()
}