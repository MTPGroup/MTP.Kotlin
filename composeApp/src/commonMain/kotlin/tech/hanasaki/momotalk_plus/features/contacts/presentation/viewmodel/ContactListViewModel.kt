package tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.ListContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListState

class ContactListViewModel(
    private val listContactUseCase: ListContactUseCase,
) : ViewModel(), ContainerHost<ContactListState, ContactListSideEffect> {

    override val container: Container<ContactListState, ContactListSideEffect> =
        viewModelScope.container(ContactListState())

    init {
        loadContacts()
    }

    fun onIntent(intent: ContactListIntent) {
        when (intent) {
            is ContactListIntent.SearchQueryChanged -> intent { reduce { state.copy(searchQuery = intent.query) } }
            is ContactListIntent.ClearSearchQuery -> intent { reduce { state.copy(searchQuery = "") } }
            is ContactListIntent.ContactClicked -> intent {
                postSideEffect(
                    ContactListSideEffect.NavigateToContactDetail(
                        intent.contactId
                    )
                )
            }

            is ContactListIntent.AddContactClicked -> intent { postSideEffect(ContactListSideEffect.NavigateToAddContact) }
        }
    }

    private fun loadContacts() {
        listContactUseCase()
            .onStart {
                intent { reduce { state.copy(isLoading = true, error = null) } }
            }
            .onEach { contacts ->
                intent { reduce { state.copy(isLoading = false, contacts = contacts) } }
            }
            .catch { e ->
                e.printStackTrace()
                intent { reduce { state.copy(isLoading = false) } }
                intent { postSideEffect(ContactListSideEffect.ShowErrorMessage("加载联系人失败: ${e.message}")) }
            }
            .launchIn(viewModelScope)
    }
}
