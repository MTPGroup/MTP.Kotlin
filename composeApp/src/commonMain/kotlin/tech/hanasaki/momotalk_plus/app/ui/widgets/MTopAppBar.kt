package tech.hanasaki.momotalk_plus.app.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Add
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.PersonAdd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MTopAppBar(
    title: String,
    username: String,
    avatarUrl: String?,
    onAvatarClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onAvatarClick,
                ) {
                    val avatarModifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)

                    if (avatarUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(avatarUrl),
                            contentDescription = "用户头像",
                            contentScale = ContentScale.Crop,
                            modifier = avatarModifier,
                        )
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter("https://cdn.hanasaki.tech/avatars/users/default_avatar.png"),
                            contentDescription = "用户头像",
                            contentScale = ContentScale.Crop,
                            modifier = avatarModifier,
                        )
                    }
                }
                if (title.isBlank()) {
                    Column {
                        Text(
                            username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "在线",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        actions = {
            if (title.isBlank()) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        Ionicons.Outline.Add,
                        contentDescription = "创建会话",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                IconButton(onClick = onActionClick) {
                    Icon(
                        Ionicons.Outline.PersonAdd,
                        contentDescription = "添加联系人",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
