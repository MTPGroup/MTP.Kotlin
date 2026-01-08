package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
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
) : ViewModel(), ContainerHost<ContactsManageState, ContactsManageSideEffect> {

    override val container: Container<ContactsManageState, ContactsManageSideEffect> =
        viewModelScope.container(ContactsManageState())

    init {
        loadAvailableContacts()
    }

    fun onIntent(intent: ContactsManageIntent) {
        when (intent) {
            is ContactsManageIntent.UpdateQuery -> intent { reduce { state.copy(query = intent.query) } }
            is ContactsManageIntent.AddContact -> viewModelScope.launch { addContact(intent.userId) }
            is ContactsManageIntent.RemoveContact -> viewModelScope.launch { removeContact(intent.userId) }
        }
    }

    private fun loadAvailableContacts() {
        listCharacterUseCase()
            .onStart {
                intent {
                    reduce {
                        state.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                }
            }
            .onEach { characters ->
                intent {
                    reduce {
                        state.copy(
                            availableContacts = characters,
                            isLoading = false,
                        )
                    }
                }
            }
            .catch { error ->
                error.printStackTrace()
                intent {
                    reduce {
                        state.copy(
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                    postSideEffect(ContactsManageSideEffect.ShowToast(error.message ?: ""))
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun addContact(userId: String) {
        intent {
            reduce {
                state.copy(
                    errorMessage = null,
                    processingContactId = userId
                )
            }
        }
        addContactUseCase(userId)
            .onSuccess {
                intent {
                    reduce {
                        state.copy(
                            addedContactIds = state.addedContactIds + userId,
                            processingContactId = null,
                        )
                    }
                }
            }
            .onFailure { e ->
                intent {
                    reduce { state.copy(processingContactId = null) }
                    postSideEffect(ContactsManageSideEffect.ShowToast("添加失败: ${e.message}"))
                }
            }
    }

    private suspend fun removeContact(userId: String) {
        intent {
            reduce {
                state.copy(
                    errorMessage = null,
                    processingContactId = userId
                )
            }
        }
        removeContactUseCase(userId)
            .onSuccess {
                intent {
                    reduce {
                        state.copy(
                            addedContactIds = state.addedContactIds - userId,
                            processingContactId = null,
                        )
                    }
                }
            }
            .onFailure { e ->
                intent {
                    reduce { state.copy(processingContactId = null) }
                    postSideEffect(ContactsManageSideEffect.ShowToast("删除失败: ${e.message}"))
                }
            }
    }
}
