@file:OptIn(kotlin.time.ExperimentalTime::class)

package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemGesturesPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.ui.widgets.MSearchBar
import tech.hanasaki.momotalk_plus.app.ui.widgets.MTopAppBar
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactListSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.ui.widgets.ContactListItem
import tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel.ContactListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListPage(
    currentUser: User?,
    onAvatarClick: () -> Unit,
    onNavigateToContactDetail: (String) -> Unit,
    onNavigateToAddContact: () -> Unit,
    viewModel: ContactListViewModel = koinViewModel(),
) {
    val uiState by viewModel.container.stateFlow.collectAsState()
    val onIntent = viewModel::onIntent
    val coroutineScope = rememberCoroutineScope()
    val snackHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current


    LaunchedEffect(viewModel.container) {
        viewModel.container.sideEffectFlow.collect { effect ->
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
        snackbarHost = {
            SnackbarHost(
                hostState = snackHostState,
                modifier = Modifier
                    .systemGesturesPadding()
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        },
        topBar = {
            MTopAppBar(
                title = "联系人",
                avatarUrl = currentUser?.avatar,
                username = currentUser?.username ?: "未登录",
                onAvatarClick = onAvatarClick,
                onActionClick = {
                    onIntent(ContactListIntent.AddContactClicked)
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
            MSearchBar(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                query = uiState.searchQuery,
                onQueryChanged = { onIntent(ContactListIntent.SearchQueryChanged(it)) },
                onClear = { onIntent(ContactListIntent.ClearSearchQuery) }
            )

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                uiState.filteredContacts.isEmpty() -> {
                    Text(
                        text = if (uiState.searchQuery.isBlank()) {
                            "暂无联系人"
                        } else {
                            "未找到匹配的联系人"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    LazyColumn {
                        uiState.filteredContacts.forEach { contact ->
                            item(key = contact.id) {
                                ContactListItem(
                                    contact = contact,
                                    onContactClick = { onIntent(ContactListIntent.ContactClicked(it)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
