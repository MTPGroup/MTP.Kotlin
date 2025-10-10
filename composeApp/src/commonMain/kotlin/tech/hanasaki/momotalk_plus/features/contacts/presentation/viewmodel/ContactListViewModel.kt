package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.ListContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListState

class ContactListViewModel(
    private val listContactUseCase: ListContactUseCase,
) : BaseViewModel<ContactListState, ContactListSideEffect, ContactListIntent>(ContactListState()) {
    init {
        loadContacts()
    }

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

                is ContactListIntent.ContactClicked -> {
                    sendSideEffect(ContactListSideEffect.NavigateToContactDetail(intent.contactId))
                }

                is ContactListIntent.AddContactClicked -> {
                    sendSideEffect(ContactListSideEffect.NavigateToAddContact)
                }
            }
        }
    }

    private fun loadContacts() {
        listContactUseCase()
            .onStart {
                updateState { it.copy(isLoading = true, error = null) }
            }
            .onEach { contacts ->
                updateState { it.copy(isLoading = false, contacts = contacts) }
            }
            .catch { e ->
                e.printStackTrace()
                updateState { it.copy(isLoading = false) }
                sendSideEffect(ContactListSideEffect.ShowErrorMessage("加载联系人失败: ${e.message}"))
            }
            .launchIn(viewModelScope)
    }
}