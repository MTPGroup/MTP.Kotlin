package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.ListContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListState

class ContactListViewModel(
    private val listContactUseCase: ListContactUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactListState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<ContactListSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun processIntent(intent: ContactListIntent) {
        viewModelScope.launch {
            when (intent) {
                is ContactListIntent.SearchQueryChanged -> {
                    _uiState.update {
                        it.copy(
                            searchQuery = intent.query,
                        )
                    }
                }

                is ContactListIntent.ClearSearchQuery -> {
                    _uiState.update {
                        it.copy(
                            searchQuery = "",
                        )
                    }
                }

                is ContactListIntent.LoadContacts ->
                    loadContacts()

                is ContactListIntent.RefreshContacts -> {
                    TODO()
                }

                is ContactListIntent.ContactClicked -> {
                    _sideEffect.send(ContactListSideEffect.NavigateToContactDetail(intent.contactId))
                }

                is ContactListIntent.AddContactClicked -> {
                    _sideEffect.send(ContactListSideEffect.NavigateToAddContact)
                }
            }
        }
    }

    private suspend fun loadContacts() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        when (val result = listContactUseCase()) {
            is IResult.Success -> {
                _uiState.update { it.copy(isLoading = false, contacts = result.data) }
            }

            is IResult.Error -> {
                _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                _sideEffect.send(ContactListSideEffect.ShowErrorMessage(result.error.message))
            }
        }
    }
}