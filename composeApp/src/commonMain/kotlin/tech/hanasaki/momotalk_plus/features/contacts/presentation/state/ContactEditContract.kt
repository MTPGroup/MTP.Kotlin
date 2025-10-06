package tech.hanasaki.momotalk_plus.features.contacts.presentation.state

import tech.hanasaki.momotalk_plus.core.domain.model.Visibility

data class ContactEditState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val name: String = "",
    val signature: String = "",
    val persona: String = "",
    val avatarUrl: String = "",
    val visibility: Visibility = Visibility.PUBLIC,
    val isSaving: Boolean = false,
)

sealed class ContactEditIndent {
    data class LoadContactInfo(val contactId: String) : ContactEditIndent()
    data class NameChanged(val name: String) : ContactEditIndent()
    data class SignatureChanged(val signature: String) : ContactEditIndent()
    data class PersonaChanged(val persona: String) : ContactEditIndent()
    data class AvatarUrlChanged(val avatarUrl: String) : ContactEditIndent()
    data class VisibilityChanged(val visibility: Visibility) : ContactEditIndent()
    data class UpdateContactInfo(val id: String) : ContactEditIndent()
}

sealed class ContactEditSideEffect {
    data class ShowMessage(val message: String) : ContactEditSideEffect()
}