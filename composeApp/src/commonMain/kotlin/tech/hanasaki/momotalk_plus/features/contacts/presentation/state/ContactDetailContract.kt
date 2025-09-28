package tech.hanasaki.momotalk_plus.features.contacts.presentation.state

import tech.hanasaki.momotalk_plus.core.domain.model.Character

data class ContactDetailState(
    val contact: Character = Character.default(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed class ContactDetailIntent {
    data class LoadContact(val userId: String) : ContactDetailIntent()
    data object RefreshContact : ContactDetailIntent()
    data object ChangeContact : ContactDetailIntent()
}

sealed class ContactDetailSideEffect {
    data class ShowErrorMessage(val message: String) : ContactDetailSideEffect()
    data object NavigateBack : ContactDetailSideEffect()
}