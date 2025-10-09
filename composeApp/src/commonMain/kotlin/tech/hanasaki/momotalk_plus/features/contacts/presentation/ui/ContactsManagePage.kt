package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.AlertCircle
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.People
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Refresh
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Search
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.ui.widgets.MSearchBar
import tech.hanasaki.momotalk_plus.app.ui.widgets.MTopBar
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
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = MaterialTheme.colorScheme

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
        containerColor = colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            MTopBar(
                title = "添加联系人",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(top = paddingValues.calculateTopPadding())
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                        }
                    )
                }
        ) {
            // 搜索框
            MSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                query = uiState.query,
                onQueryChanged = { onIntent(ContactsManageIntent.UpdateQuery(it)) },
                onClear = { onIntent(ContactsManageIntent.UpdateQuery("")) },
            )

            // 内容区域
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Ionicons.Outline.AlertCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = colorScheme.error.copy(alpha = 0.7f)
                            )
                            Text(
                                text = uiState.errorMessage ?: "加载失败",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant
                            )
                            // 重试按钮
                            Box(
                                modifier = Modifier
                                    .height(44.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(22.dp))
                                    .background(colorScheme.primaryContainer)
                                    .clickable {
                                        onIntent(ContactsManageIntent.LoadAvailableContacts)
                                    }
                                    .padding(horizontal = 24.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Ionicons.Outline.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "重试",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                uiState.availableContacts.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Ionicons.Outline.People,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "暂无可添加的角色",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    if (uiState.filteredContacts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Ionicons.Outline.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = "未找到匹配的角色",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(
                                items = uiState.filteredContacts,
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
