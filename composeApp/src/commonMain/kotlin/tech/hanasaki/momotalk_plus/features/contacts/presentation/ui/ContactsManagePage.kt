package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Filled
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.*
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.ui.widgets.MSearchBar
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactsManageIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactsManageSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.ui.widgets.ContactManageListItem
import tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel.ContactsManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsManagePage(
    onNavigateBack: () -> Unit,
    viewModel: ContactsManageViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val onIntent = viewModel::processIntent
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        onIntent(ContactsManageIntent.LoadAvailableContacts)
    }

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is ContactsManageSideEffect.ShowToast ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = sideEffect.message,
                            withDismissAction = true
                        )
                    }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text("添加联系人") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Ionicons.Filled.ChevronBack,
                            contentDescription = "返回",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框
            MSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                query = uiState.query,
                onQueryChanged = { onIntent(ContactsManageIntent.UpdateQuery(it)) },
                onClear = { onIntent(ContactsManageIntent.UpdateQuery("")) },
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Ionicons.Filled.AlertCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = uiState.errorMessage ?: "加载失败",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = {
                                    onIntent(ContactsManageIntent.LoadAvailableContacts)
                                }
                            ) {
                                Icon(
                                    imageVector = Ionicons.Filled.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(end = 4.dp)
                                )
                                Text("重试")
                            }
                        }
                    }
                }

                uiState.availableContacts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Ionicons.Filled.People,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "暂无可添加的角色",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    val filteredContacts = remember(uiState.availableContacts, uiState.query) {
                        if (uiState.query.isEmpty()) {
                            uiState.availableContacts
                        } else {
                            uiState.availableContacts.filter {
                                it.name.contains(uiState.query, ignoreCase = true)
                            }
                        }
                    }

                    if (filteredContacts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Ionicons.Filled.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "未找到匹配的角色",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(
                                items = filteredContacts,
                                key = { it.id }
                            ) { character ->
                                val isAdded = character.id in uiState.addedContactIds
                                val isProcessing = uiState.processingContactId == character.id

                                ContactManageListItem(
                                    character = character,
                                    isAdded = isAdded,
                                    isProcessing = isProcessing,
                                    onAddClick = {
                                        onIntent(ContactsManageIntent.AddContact(character.id))
                                    },
                                    onRemoveClick = {
                                        onIntent(ContactsManageIntent.RemoveContact(character.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
