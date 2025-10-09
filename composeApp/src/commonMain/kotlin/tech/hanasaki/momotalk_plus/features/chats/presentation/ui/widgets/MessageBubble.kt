package tech.hanasaki.momotalk_plus.features.chats.presentation.ui.widgets

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Filled
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.CheckmarkDone
import kotlinx.coroutines.delay
import tech.hanasaki.momotalk_plus.core.utils.formatTimestamp
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message
import tech.hanasaki.momotalk_plus.features.chats.domain.model.MessageSenderRole

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier,
) {
    val isUser = message.role == MessageSenderRole.USER

    // 逐字显示状态
    var displayedText by remember(message.id) {
        mutableStateOf(if (message.isStreaming) "" else message.content)
    }

    // 逐字显示动画
    LaunchedEffect(message.id, message.content, message.isStreaming) {
        if (message.isStreaming) {
            // 只追加新字符,不重置
            if (displayedText.length < message.content.length) {
                val newContent = message.content.substring(displayedText.length)
                for (char in newContent) {
                    displayedText += char
                    delay(10) // 更快的打字速度
                }
            }
        } else {
            // 流式结束,显示完整内容
            displayedText = message.content
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            AsyncImage(
                model = message.sender.avatar ?: "https://cdn.hanasaki.tech/avatars/users/default_avatar.png",
                contentDescription = "${message.sender.name}的头像",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = message.sender.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Surface(
                shape = if (isUser) {
                    RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
                } else {
                    RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
                },
                color = if (isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                },
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    when {
                        // 等待 AI 响应
                        displayedText.isEmpty() && message.isStreaming -> {
                            TypingDots(
                                dotColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        // 显示文本
                        else -> {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = displayedText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isUser) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                        },
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))

                                Row {
                                    Text(
                                        text = formatTimestamp(message.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isUser) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                        },
                                        modifier = Modifier.align(Alignment.Bottom)
                                    )
                                    Icon(
                                        Ionicons.Filled.CheckmarkDone,
                                        contentDescription = "已读",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(Modifier.width(8.dp))
            AsyncImage(
                model = message.sender.avatar ?: "https://v2.xxapi.cn/api/head?return=302",
                contentDescription = "用户头像",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
