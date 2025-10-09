package tech.hanasaki.momotalk_plus.features.chats.presentation.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MessageInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val hasContent = message.isNotBlank()
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp + paddingValues.calculateBottomPadding()
            ),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 输入框容器
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(colorScheme.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { isFocused = it.isFocused },
                    enabled = enabled,
                    textStyle = TextStyle(
                        color = if (enabled)
                            colorScheme.onSurface
                        else
                            colorScheme.onSurface.copy(alpha = 0.38f),
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (enabled && hasContent) {
                                onSendClick()
                            }
                        }
                    ),
                    cursorBrush = SolidColor(colorScheme.primary),
                    maxLines = 5,
                    decorationBox = { innerTextField ->
                        Box {
                            if (message.isEmpty()) {
                                Text(
                                    text = "输入消息...",
                                    style = TextStyle(
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontSize = 16.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        // 发送按钮
        /* Box(
             modifier = Modifier
                 .size(48.dp)
                 .clip(CircleShape)
                 .background(
                     if (hasContent && enabled)
                         colorScheme.primary
                     else
                         colorScheme.surfaceVariant
                 )
                 .clickable(
                     enabled = hasContent && enabled,
                     onClick = onSendClick
                 ),
             contentAlignment = Alignment.Center
         ) {
             Icon(
                 imageVector = Ionicons.Outline.Send,
                 contentDescription = "发送",
                 modifier = Modifier.size(22.dp),
                 tint = if (hasContent && enabled)
                     colorScheme.onPrimary
                 else
                     colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
             )
         }*/
    }
}