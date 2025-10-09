package tech.hanasaki.momotalk_plus.app.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Add
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.PersonAdd

@Composable
fun MTopAppBar(
    title: String,
    username: String,
    avatarUrl: String?,
    onAvatarClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .background(colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧：头像和用户信息
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
        ) {
            // 头像区域
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable(onClick = onAvatarClick)
            ) {
                // 背景光晕
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer.copy(alpha = 0.3f))
                )

                AsyncImage(
                    model = avatarUrl ?: "https://cdn.hanasaki.tech/avatars/users/default_avatar.png",
                    contentDescription = "用户头像",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, colorScheme.primary, CircleShape)
                )
            }

            // 用户名和状态（仅在没有标题时显示）
            if (title.isBlank()) {
                Column {
                    Text(
                        text = username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "在线",
                        fontSize = 12.sp,
                        color = colorScheme.primary
                    )
                }
            }
        }

        // 中间：标题（如果有）
        if (title.isNotBlank()) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        // 右侧：操作按钮
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onActionClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (title.isBlank())
                    Ionicons.Outline.Add
                else
                    Ionicons.Outline.PersonAdd,
                contentDescription = if (title.isBlank()) "创建会话" else "添加联系人",
                modifier = Modifier.size(24.dp),
                tint = colorScheme.onPrimaryContainer
            )
        }
    }
}
