package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.core.domain.usecase.CharacterDetailUseCase
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.DeleteContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailState

class ContactDetailViewModel(
    private val characterDetailUseCase: CharacterDetailUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
) : BaseViewModel<ContactDetailState, ContactDetailSideEffect, ContactDetailIntent>(ContactDetailState()) {
    override fun processIntent(intent: ContactDetailIntent) {
        viewModelScope.launch {
            when (intent) {
                is ContactDetailIntent.LoadContact ->
                    loadContact(intent.userId)

                is ContactDetailIntent.ShowDeleteDialog ->
                    updateState {
                        it.copy(
                            showDialog = true,
                            errorMessage = null,
                        )
                    }

                is ContactDetailIntent.DeleteContact ->
                    deleteContact(intent.userId)
            }
        }
    }

    private suspend fun loadContact(characterId: String) {
        when (val result = characterDetailUseCase(characterId)) {
            is IResult.Success -> {
                updateState {
                    it.copy(
                        contact = result.data
                    )
                }
            }

            is IResult.Error -> {
                sendSideEffect(ContactDetailSideEffect.ShowErrorMessage(result.error.message))
            }
        }
    }

    private suspend fun deleteContact(characterId: String) {
        when (val result = deleteContactUseCase(characterId)) {
            is IResult.Success -> {
                updateState {
                    it.copy(
                        showDialog = false
                    )
                }
                sendSideEffect(ContactDetailSideEffect.NavigateToContactsList)
            }

            is IResult.Error -> {
                sendSideEffect(ContactDetailSideEffect.ShowErrorMessage(result.error.message))
            }
        }
    }
}