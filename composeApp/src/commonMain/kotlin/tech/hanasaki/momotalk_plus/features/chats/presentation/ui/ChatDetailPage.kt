@file:OptIn(kotlin.time.ExperimentalTime::class)

package tech.hanasaki.momotalk_plus.features.chats.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.core.Dialog
import com.composables.core.DialogPanel
import com.composables.core.Scrim
import com.composables.core.rememberDialogState
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Filled
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.ChevronBack
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Trash
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatDetailIntent
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatDetailSideEffect
import tech.hanasaki.momotalk_plus.features.chats.presentation.ui.widgets.MessageBubble
import tech.hanasaki.momotalk_plus.features.chats.presentation.ui.widgets.MessageInputBar
import tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel.ChatDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailPage(
    chatId: String,
    currentUser: User?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.container.stateFlow.collectAsState()
    val onIntent = viewModel::onIntent
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val dialogState = rememberDialogState()
    val colorScheme = MaterialTheme.colorScheme

    // 记录上一次的消息内容长度
    var lastContentLength by remember { mutableStateOf(0) }

    LaunchedEffect(viewModel.container) {
        viewModel.container.sideEffectFlow.collect { effect ->
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = modifier.fillMaxSize()) {
            // 自定义顶部栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .background(colorScheme.surface)
                .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 返回按钮
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Ionicons.Filled.ChevronBack,
                        contentDescription = "返回",
                        modifier = Modifier.size(24.dp),
                        tint = colorScheme.onSurface
                    )
                }

                // 标题和头像
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    /*  if (uiState.avatar != null) {
                          Box(contentAlignment = Alignment.Center) {
                              // 背景光晕
                              Box(
                                  modifier = Modifier
                                      .size(44.dp)
                                      .clip(CircleShape)
                                      .background(colorScheme.primaryContainer.copy(alpha = 0.3f))
                              )
                              AsyncImage(
                                  model = uiState.avatar,
                                  contentDescription = "会话头像",
                                  modifier = Modifier
                                      .size(40.dp)
                                      .clip(CircleShape),
                                  contentScale = ContentScale.Crop
                              )
                          }
                      }*/

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = uiState.title.ifEmpty { "聊天" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface
                        )
                        if (uiState.isTyping) {
                            Text(
                                text = "正在输入...",
                                fontSize = 12.sp,
                                color = colorScheme.primary
                            )
                        }
                    }
                }

                // 删除按钮
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { dialogState.visible = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Ionicons.Outline.Trash,
                        contentDescription = "删除会话",
                        modifier = Modifier.size(22.dp),
                        tint = colorScheme.error
                    )
                }
            }

            // 消息列表
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(colorScheme.surfaceContainerLowest),
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

            // 输入框
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
                paddingValues = PaddingValues(0.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 删除确认对话框
        Dialog(state = dialogState) {
            Scrim()
            DialogPanel(
                modifier = Modifier
                    .displayCutoutPadding()
                    .systemBarsPadding()
                    .widthIn(min = 280.dp, max = 400.dp)
                    .padding(24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 图标
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Ionicons.Outline.Trash,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = colorScheme.error
                        )
                    }

                    Text(
                        text = "确认清空历史记录？",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )

                    Text(
                        text = "此操作将删除所有消息记录，且不可恢复。",
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.surfaceVariant)
                                .clickable { dialogState.visible = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "取消",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.error)
                                .clickable {
                                    onIntent(ChatDetailIntent.ClearChatHistory(chatId))
                                    dialogState.visible = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "确认",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onError
                            )
                        }
                    }
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
