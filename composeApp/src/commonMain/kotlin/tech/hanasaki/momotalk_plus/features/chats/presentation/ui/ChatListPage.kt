package tech.hanasaki.momotalk_plus.features.chats.presentation.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Chatbubbles
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Search
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.ui.widgets.MSearchBar
import tech.hanasaki.momotalk_plus.app.ui.widgets.MTopAppBar
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatsIntent
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatsSideEffect
import tech.hanasaki.momotalk_plus.features.chats.presentation.ui.widgets.CreateChatDialog
import tech.hanasaki.momotalk_plus.features.chats.presentation.ui.widgets.SwipeableChatListItem
import tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel.ChatsViewModel

@Composable
fun ChatListPage(
    currentUser: User?,
    onNavigateToChatDetails: (String) -> Unit,
    onAvatarClick: () -> Unit,
    viewModel: ChatsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val onIntent = viewModel::processIntent
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ChatsSideEffect.NavigateToChatDetails ->
                    onNavigateToChatDetails(effect.chatId)

                is ChatsSideEffect.ShowToast ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .systemGesturesPadding()
            )
        },
        topBar = {
            MTopAppBar(
                title = "",
                avatarUrl = currentUser?.avatar,
                username = currentUser?.name ?: "未登录",
                onAvatarClick = onAvatarClick,
                onActionClick = { onIntent(ChatsIntent.ShowCreateChatDialog) },
            )
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        if (uiState.showCreateChatDialog) {
            CreateChatDialog(
                contacts = uiState.availableContacts,
                isLoading = uiState.isLoadingContacts,
                isCreating = uiState.isCreatingChat,
                onDismiss = { onIntent(ChatsIntent.DismissCreateChatDialog) },
                onCreateChat = { characterId, title, description, avatarUrl ->
                    onIntent(
                        ChatsIntent.CreateChat(
                            characterId,
                            title,
                            description,
                            avatarUrl
                        )
                    )
                },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                            onIntent(ChatsIntent.UpdateSearchQuery(""))
                        }
                    )
                }
        ) {
            // 搜索栏
            MSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                query = uiState.searchQuery,
                onQueryChanged = { onIntent(ChatsIntent.UpdateSearchQuery(it)) },
                onClear = { onIntent(ChatsIntent.UpdateSearchQuery("")) }
            )

            // 聊天列表
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

                uiState.filteredChatList.isEmpty() -> {
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
                                imageVector = if (uiState.searchQuery.isNotBlank())
                                    Ionicons.Outline.Search
                                else
                                    Ionicons.Outline.Chatbubbles,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                text = if (uiState.searchQuery.isNotBlank())
                                    "未找到匹配的会话"
                                else
                                    "暂无会话",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant
                            )
                            if (uiState.searchQuery.isBlank()) {
                                Text(
                                    text = "点击右上角开始新对话",
                                    fontSize = 14.sp,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(uiState.filteredChatList, key = { it.id }) { chat ->
                            SwipeableChatListItem(
                                chat = chat,
                                onChatClick = { chatId ->
                                    onIntent(ChatsIntent.ChatClicked(chatId))
                                },
                                onPinClick = { chatId ->
                                    onIntent(ChatsIntent.PinChat(chatId))
                                },
                                onDeleteClick = { chatId ->
                                    onIntent(ChatsIntent.DeleteChat(chatId))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}