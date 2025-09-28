package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.ui.widgets.ISearchBar
import tech.hanasaki.momotalk_plus.app.ui.widgets.TopAppBar
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.ui.widgets.ContactListItem
import tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel.ContactListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListPage(
    currentUser: UserProfile?,
    onAvatarClick: () -> Unit,
    onNavigateToContactDetail: (String) -> Unit,
    onNavigateToAddContact: () -> Unit,
    viewModel: ContactListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val onIntent = viewModel::processIntent
    val coroutineScope = rememberCoroutineScope()
    val snackHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val filteredContacts = remember(uiState.contacts, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) {
            uiState.contacts
        } else {
            uiState.contacts.filter {
                it.name.contains(uiState.searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        onIntent(ContactListIntent.LoadContacts)
    }

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ContactListSideEffect.NavigateToContactDetail ->
                    onNavigateToContactDetail(effect.contactId)

                is ContactListSideEffect.NavigateToAddContact ->
                    onNavigateToAddContact()

                is ContactListSideEffect.ShowErrorMessage -> {
                    coroutineScope.launch {
                        snackHostState.showSnackbar(effect.message)
                    }
                }
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackHostState) },
        topBar = {
            TopAppBar(
                title = "联系人",
                avatarUrl = currentUser?.image,
                username = currentUser?.name ?: "未登录",
                onAvatarClick = onAvatarClick,
                onActionClick = {
                    onIntent(ContactListIntent.AddContactClicked)
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                            onIntent(ContactListIntent.ClearSearchQuery)
                        }
                    )
                }
        ) {
            ISearchBar(
                modifier = Modifier.padding(horizontal = 16.dp),
                query = uiState.searchQuery,
                onQueryChanged = { query ->
                    onIntent(ContactListIntent.SearchQueryChanged(query))
                },
                onClear = {
                    onIntent(ContactListIntent.ClearSearchQuery)
                }
            )
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        items(filteredContacts.size) { index ->
                            ContactListItem(
                                contact = filteredContacts[index],
                                onContactClick = { contactId ->
                                    onIntent(ContactListIntent.ContactClicked(contactId))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}