package tech.hanasaki.momotalk_plus.features.chats.presentation.ui.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.Sharp
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Person
import com.woowla.compose.icon.collections.ionicons.ionicons.sharp.Pin
import com.woowla.compose.icon.collections.ionicons.ionicons.sharp.Trash
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.hanasaki.momotalk_plus.core.utils.formatTimestamp
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Chat
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableChatListItem(
    chat: Chat,
    onChatClick: (String) -> Unit,
    onPinClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val actionWidth = with(density) { 132.dp.toPx() } // 两个按钮的总宽度
    val scope = rememberCoroutineScope()

    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX)

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // 背景按钮层
        SwipeBackground(
            offsetX = animatedOffsetX,
            onPinClick = {
                onPinClick(chat.id)
                offsetX = 0f
            },
            onDeleteClick = {
                onDeleteClick(chat.id)
                offsetX = 0f
            }
        )

        // 前景内容层
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newOffset = offsetX + delta
                        // 只允许向左滑动,范围: -actionWidth 到 0
                        offsetX = newOffset.coerceIn(-actionWidth, 0f)
                    },
                    onDragStopped = { velocity ->
                        // 根据速度和位置决定停留状态
                        scope.launch {
                            offsetX = when {
                                velocity < -500f -> -actionWidth // 快速向左滑
                                offsetX < -actionWidth / 2 -> -actionWidth // 超过一半
                                else -> 0f // 回弹
                            }
                        }
                    }
                )
                .clickable {
                    if (offsetX < 0f) {
                        offsetX = 0f // 收回
                    } else {
                        onChatClick(chat.id)
                    }
                }
        ) {
            ChatContent(chat = chat)
        }
    }
}

@Composable
private fun BoxScope.SwipeBackground(
    offsetX: Float,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val isVisible = offsetX < 0f

    Box(
        modifier = Modifier
            .matchParentSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        if (isVisible) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 置顶按钮
                ActionButton(
                    icon = Ionicons.Sharp.Pin,
                    backgroundColor = Color(0xFF2196F3),
                    contentDescription = "置顶",
                    onClick = onPinClick,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(66.dp)
                )
                // 删除按钮
                ActionButton(
                    icon = Ionicons.Sharp.Trash,
                    backgroundColor = Color(0xFFFF5252),
                    contentDescription = "删除",
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(66.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f)

    Surface(
        modifier = modifier.scale(scale),
        shape = RectangleShape,
        color = backgroundColor,
        onClick = {
            pressed = true
            onClick()
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(100)
            pressed = false
        }
    }
}

@Composable
private fun ChatContent(chat: Chat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (chat.avatarUrl != null) {
                AsyncImage(
                    model = chat.avatarUrl,
                    contentDescription = "${chat.title} 的头像",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Ionicons.Outline.Person,
                    contentDescription = "${chat.title} 的头像",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = formatTimestamp(chat.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = chat.lastMessage ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}