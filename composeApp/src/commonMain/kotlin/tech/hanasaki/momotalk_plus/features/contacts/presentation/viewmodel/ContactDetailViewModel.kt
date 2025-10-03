package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.domain.usecase.CharacterDetailUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailState

class ContactDetailViewModel(
    private val characterDetailUseCase: CharacterDetailUseCase,
) : BaseViewModel<ContactDetailState, ContactDetailSideEffect, ContactDetailIntent>(ContactDetailState()) {
    override fun processIntent(intent: ContactDetailIntent) {
        viewModelScope.launch {
            when (intent) {
                is ContactDetailIntent.LoadContact ->
                    loadContact(intent.userId)

                is ContactDetailIntent.RefreshContact -> {
                    TODO()
                }

                is ContactDetailIntent.ChangeContact -> {
                    TODO()
                }
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
}