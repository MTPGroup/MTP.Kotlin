package tech.hanasaki.momotalk_plus.features.chats.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.ui.widgets.TopAppBar
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatListItem
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatsIntent
import tech.hanasaki.momotalk_plus.features.chats.presentation.state.ChatsSideEffect
import tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel.ChatsViewModel

@Composable
fun ChatsScreen(
    currentUser: UserProfile?,
    onAvatarClick: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    modifier: Modifier = Modifier,
    chatsViewModel: ChatsViewModel = koinViewModel(),
) {
    val uiState by chatsViewModel.uiState.collectAsState()
    val onIntent = chatsViewModel::processIntent


    LaunchedEffect(chatsViewModel.sideEffect) {
        chatsViewModel.sideEffect.collect { effect ->
            when (effect) {
                is ChatsSideEffect.NavigateToChatDetails -> onNavigateToChat(effect.chatId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "",
                avatarUrl = currentUser?.image,
                username = currentUser?.name ?: "未登录",
                onAvatarClick = onAvatarClick,
                onActionClick = {},
            )
        },
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.chatList, key = { it.id }) { chat ->
                        ChatItem(
                            chat = chat,
                            onClick = { onIntent(ChatsIntent.ChatClicked(chat.id)) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 88.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatItem(
    chat: ChatListItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = "${chat.name} 的头像",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = chat.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = chat.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (chat.unreadCount > 0) {
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(
                        text = chat.unreadCount.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}