package tech.hanasaki.momotalk_plus.features.home.presentation.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Cog
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.InformationCircle
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
    ModalDrawerSheet(
        modifier = modifier.fillMaxWidth(),
        drawerShape = RectangleShape,
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        DrawerHeader(user = currentUser)
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            icon = {
                Icon(
                    Ionicons.Outline.Person,
                    contentDescription = "个人资料",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "个人资料",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            selected = false,
            onClick = onProfileClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        NavigationDrawerItem(
            icon = {
                Icon(
                    Ionicons.Outline.Cog,
                    contentDescription = "设置",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "设置",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            selected = false,
            onClick = onSettingsClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        NavigationDrawerItem(
            icon = {
                Icon(
                    Ionicons.Outline.InformationCircle,
                    contentDescription = "关于",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "关于",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            selected = false,
            onClick = { /* TODO: Navigate to About */ },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(Modifier.weight(1f))

        NavigationDrawerItem(
            icon = {
                Icon(
                    Ionicons.Outline.LogOut,
                    contentDescription = "退出登录",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "退出登录",
                    color = MaterialTheme.colorScheme.error
                )
            },
            selected = false,
            onClick = onLogoutClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedIconColor = MaterialTheme.colorScheme.error,
                unselectedTextColor = MaterialTheme.colorScheme.error
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DrawerHeader(user: UserProfile?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                AsyncImage(
                    model = user?.image,
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = user?.name ?: "未登录",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = user?.email ?: "点击头像登录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}