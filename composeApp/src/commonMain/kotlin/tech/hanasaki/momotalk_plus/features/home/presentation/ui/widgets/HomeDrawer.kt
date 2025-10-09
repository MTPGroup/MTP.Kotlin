package tech.hanasaki.momotalk_plus.features.home.presentation.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Cog
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.LogOut
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Person
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile

@Composable
fun HomeDrawerContent(
    currentUser: UserProfile?,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {
        // 头部区域
        DrawerHeader(user = currentUser)

        // 菜单项
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
        ) {
            DrawerMenuItem(
                icon = Ionicons.Outline.Person,
                label = "个人资料",
                onClick = onProfileClick,
                iconTint = colorScheme.primary
            )

            DrawerMenuItem(
                icon = Ionicons.Outline.Cog,
                label = "设置",
                onClick = onSettingsClick,
                iconTint = colorScheme.primary
            )
        }

        // 底部退出按钮
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            DrawerMenuItem(
                icon = Ionicons.Outline.LogOut,
                label = "退出登录",
                onClick = onLogoutClick,
                iconTint = colorScheme.error
            )
        }
    }
}

@Composable
private fun DrawerHeader(
    user: UserProfile?,
) {
    val colorScheme = MaterialTheme.colorScheme
    val painter = rememberAsyncImagePainter(
        user?.image ?: "https://cdn.hanasaki.tech/avatars/users/default_avatar.png"
    )
    val avatarState by painter.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.primaryContainer)
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 头像区域
        Box(contentAlignment = Alignment.Center) {
            // 背景光晕
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surface.copy(alpha = 0.2f))
            )

            when (avatarState) {
                AsyncImagePainter.State.Empty,
                is AsyncImagePainter.State.Loading,
                    -> {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp,
                            color = colorScheme.primary
                        )
                    }
                }

                is AsyncImagePainter.State.Success -> {
                    Image(
                        painter = painter,
                        contentDescription = "用户头像",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(3.dp, colorScheme.surface, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                is AsyncImagePainter.State.Error -> {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Ionicons.Outline.Person,
                            contentDescription = "用户头像",
                            tint = colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }

        // 用户信息
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = user?.name ?: "未登录",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onPrimaryContainer
            )
            Text(
                text = user?.email ?: "点击登录",
                fontSize = 14.sp,
                color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconTint: Color,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp),
                tint = iconTint
            )
        }

        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface
        )
    }
}