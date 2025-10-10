package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.common.BaseViewModel
import tech.hanasaki.momotalk_plus.core.domain.usecase.ListCharacterUseCase
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.AddContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.DeleteContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactsManageIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactsManageSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactsManageState

class ContactsManageViewModel(
    private val listCharacterUseCase: ListCharacterUseCase,
    private val addContactUseCase: AddContactUseCase,
    private val removeContactUseCase: DeleteContactUseCase,
) : BaseViewModel<ContactsManageState, ContactsManageSideEffect, ContactsManageIntent>(ContactsManageState()) {
    init {
        loadAvailableContacts()
    }

    override fun processIntent(intent: ContactsManageIntent) {
        viewModelScope.launch {
            when (intent) {
                is ContactsManageIntent.UpdateQuery ->
                    updateState { it.copy(query = intent.query) }

                is ContactsManageIntent.AddContact ->
                    addContact(intent.userId)

                is ContactsManageIntent.RemoveContact ->
                    removeContact(intent.userId)
            }
        }
    }

    private fun loadAvailableContacts() {
        listCharacterUseCase()
            .onStart {
                updateState {
                    it.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                }
            }
            .onEach { characters ->
                updateState {
                    it.copy(
                        availableContacts = characters,
                        isLoading = false,
                    )
                }
            }
            .catch { error ->
                error.printStackTrace()
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message,
                    )
                }
                sendSideEffect(ContactsManageSideEffect.ShowToast(error.message ?: ""))
            }
            .launchIn(viewModelScope)
    }

    private suspend fun addContact(userId: String) {
        updateState {
            it.copy(
                errorMessage = null,
                processingContactId = userId
            )
        }
        addContactUseCase(userId)
            .onSuccess {
                updateState {
                    it.copy(
                        addedContactIds = it.addedContactIds + userId,
                        processingContactId = null,
                    )
                }
            }
            .onFailure { e ->
                updateState {
                    it.copy(
                        processingContactId = null,
                    )
                }
                sendSideEffect(ContactsManageSideEffect.ShowToast("添加失败: ${e.message}"))
            }
    }

    private suspend fun removeContact(userId: String) {
        updateState {
            it.copy(
                errorMessage = null,
                processingContactId = userId
            )
        }
        removeContactUseCase(userId)
            .onSuccess {
                updateState {
                    it.copy(
                        addedContactIds = it.addedContactIds - userId,
                        processingContactId = null,
                    )
                }
            }
            .onFailure { e ->
                updateState {
                    it.copy(
                        processingContactId = null,
                    )
                }
                sendSideEffect(ContactsManageSideEffect.ShowToast("删除失败: ${e.message}"))
            }
    }
}