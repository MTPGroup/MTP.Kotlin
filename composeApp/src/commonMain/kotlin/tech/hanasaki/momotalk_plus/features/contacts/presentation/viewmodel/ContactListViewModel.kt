package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.ListContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListState

class ContactListViewModel(
    private val listContactUseCase: ListContactUseCase,
) : BaseViewModel<ContactListState, ContactListSideEffect, ContactListIntent>(ContactListState()) {
    override fun processIntent(intent: ContactListIntent) {
        viewModelScope.launch {
            when (intent) {
                is ContactListIntent.SearchQueryChanged -> {
                    updateState {
                        it.copy(
                            searchQuery = intent.query
                        )
                    }
                }

                is ContactListIntent.ClearSearchQuery -> {
                    updateState {
                        it.copy(
                            searchQuery = "",
                        )
                    }
                }

                is ContactListIntent.LoadContacts ->
                    loadContacts()

                is ContactListIntent.ContactClicked -> {
                    sendSideEffect(ContactListSideEffect.NavigateToContactDetail(intent.contactId))
                }

                is ContactListIntent.AddContactClicked -> {
                    sendSideEffect(ContactListSideEffect.NavigateToAddContact)
                }
            }
        }
    }

    private suspend fun loadContacts() {
        updateState { it.copy(isLoading = true, error = null) }

        when (val result = listContactUseCase()) {
            is IResult.Success -> {
                updateState { it.copy(isLoading = false, contacts = result.data) }
            }

            is IResult.Error -> {
                updateState { it.copy(isLoading = false, error = result.error.message) }
                sendSideEffect(ContactListSideEffect.ShowErrorMessage(result.error.message))
            }
        }
    }
}