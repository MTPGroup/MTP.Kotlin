package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.domain.usecase.ListCharacterUseCase
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.AddContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.DeleteContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.ListContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactsManageIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactsManageSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactsManageState

class ContactsManageViewModel(
    private val listCharacterUseCase: ListCharacterUseCase,
    private val listContactUseCase: ListContactUseCase,
    private val addContactUseCase: AddContactUseCase,
    private val removeContactUseCase: DeleteContactUseCase,
) : BaseViewModel<ContactsManageState, ContactsManageSideEffect, ContactsManageIntent>(ContactsManageState()) {
    override fun processIntent(intent: ContactsManageIntent) {
        viewModelScope.launch {
            when (intent) {
                is ContactsManageIntent.UpdateQuery ->
                    updateState { it.copy(query = intent.query) }

                is ContactsManageIntent.LoadAvailableContacts ->
                    loadAvailableContacts()

                is ContactsManageIntent.AddContact ->
                    addContact(intent.userId)

                is ContactsManageIntent.RemoveContact ->
                    removeContact(intent.userId)
            }
        }
    }

    private suspend fun loadAvailableContacts() {
        updateState {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        when (val characterResults = listCharacterUseCase()) {
            is IResult.Success -> {
                when (val contacts = listContactUseCase()) {
                    is IResult.Success -> {
                        val addedIds = contacts.data.map { it.id }.toSet()
                        updateState {
                            it.copy(
                                availableContacts = characterResults.data,
                                addedContactIds = addedIds,
                                isLoading = false,
                                errorMessage = null,
                            )
                        }
                    }

                    is IResult.Error -> {
                        updateState {
                            it.copy(
                                availableContacts = characterResults.data,
                                isLoading = false,
                                errorMessage = contacts.error.message
                            )
                        }
                    }
                }
            }

            is IResult.Error -> {
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = characterResults.error.message
                    )
                }
            }
        }
    }

    private suspend fun addContact(userId: String) {
        updateState {
            it.copy(
                errorMessage = null,
                processingContactId = userId
            )
        }
        when (val result = addContactUseCase(userId)) {
            is IResult.Success -> {
                updateState {
                    it.copy(
                        addedContactIds = it.addedContactIds + userId,
                        processingContactId = null,
                    )
                }
            }

            is IResult.Error -> {
                updateState {
                    it.copy(
                        processingContactId = null,
                        errorMessage = result.error.message,
                    )
                }
                sendSideEffect(ContactsManageSideEffect.ShowToast(result.error.message))
            }
        }
    }

    private suspend fun removeContact(userId: String) {
        updateState {
            it.copy(
                errorMessage = null,
                processingContactId = userId
            )
        }
        when (val result = removeContactUseCase(userId)) {
            is IResult.Success -> {
                updateState {
                    it.copy(
                        addedContactIds = it.addedContactIds - userId,
                        processingContactId = null,
                    )
                }
            }

            is IResult.Error -> {
                updateState {
                    it.copy(
                        processingContactId = null,
                        errorMessage = result.error.message,
                    )
                }
                sendSideEffect(ContactsManageSideEffect.ShowToast(result.error.message))
            }
        }
    }
}