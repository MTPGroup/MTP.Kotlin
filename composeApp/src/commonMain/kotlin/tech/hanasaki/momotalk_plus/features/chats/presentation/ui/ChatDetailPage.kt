package tech.hanasaki.momotalk_plus.features.chats.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.core.Dialog
import com.composables.core.DialogPanel
import com.composables.core.Scrim
import com.composables.core.rememberDialogState
import com.composeunstyled.Button
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Filled
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.ChevronBack
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.TrashBin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatDetailIntent
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatDetailSideEffect
import tech.hanasaki.momotalk_plus.features.chats.presentation.ui.widgets.MessageBubble
import tech.hanasaki.momotalk_plus.features.chats.presentation.ui.widgets.MessageInputBar
import tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel.ChatDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailPage(
    chatId: String,
    currentUser: UserProfile?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val onIntent = viewModel::processIntent
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val dialogState = rememberDialogState()

    // 记录上一次的消息内容长度
    var lastContentLength by remember { mutableStateOf(0) }

    LaunchedEffect(chatId) {
        onIntent(ChatDetailIntent.LoadChat(chatId))
    }

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ChatDetailSideEffect.ShowToast -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }

                is ChatDetailSideEffect.ScrollToBottom -> {
                    if (uiState.messages.isNotEmpty()) {
                        listState.animateScrollToItem(uiState.messages.size - 1)
                    }
                }
            }
        }
    }

    // 流式输出时实时滚动
    LaunchedEffect(uiState.messages.lastOrNull()?.content) {
        val lastMessage = uiState.messages.lastOrNull()
        if (lastMessage?.isStreaming == true) {
            val currentLength = lastMessage.content.length
            // 只有内容变化时才滚动
            if (currentLength > lastContentLength) {
                listState.scrollToItem(uiState.messages.size - 1)
            }
        }
    }

    // 消息数量变化或流式结束时滚动
    LaunchedEffect(uiState.messages.size, uiState.isStreaming) {
        if (uiState.messages.isNotEmpty()) {
            delay(50)
            if (uiState.isStreaming) {
                listState.scrollToItem(uiState.messages.size - 1)
            } else {
                listState.animateScrollToItem(uiState.messages.size - 1)
                lastContentLength = 0
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.avatar != null) {
                            AsyncImage(
                                model = uiState.avatar,
                                contentDescription = "会话头像",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column {
                            Text(
                                text = uiState.title.ifEmpty { "聊天" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.isTyping) {
                                Text(
                                    text = "正在输入...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Ionicons.Filled.ChevronBack,
                            contentDescription = "返回",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { dialogState.visible = true }) {
                        Icon(
                            imageVector = Ionicons.Outline.TrashBin,
                            contentDescription = "删除会话",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Dialog(state = dialogState) {
            Scrim()
            DialogPanel(
                modifier = Modifier
                    .displayCutoutPadding()
                    .systemBarsPadding()
                    .widthIn(min = 280.dp, max = 560.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text("确认清空历史记录吗？")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = { dialogState.visible = false }) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            onIntent(ChatDetailIntent.ClearChatHistory(chatId))
                            dialogState.visible = false
                        }) {
                            Text("清空", color = Color.Red)
                        }
                    }
                }
            }
        }
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { message ->
                        MessageBubble(
                            message = message,
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                MessageInputBar(
                    message = uiState.inputMessage,
                    onMessageChange = { onIntent(ChatDetailIntent.InputMessageChanged(it)) },
                    onSendClick = {
                        if (uiState.inputMessage.isNotBlank()) {
                            onIntent(
                                ChatDetailIntent.SendMessage(
                                    chatId = chatId,
                                    message = uiState.inputMessage,
                                    currentUser = currentUser
                                )
                            )
                            onIntent(ChatDetailIntent.InputMessageChanged(""))
                        }
                    },
                    enabled = true,
                    paddingValues = paddingValues,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}