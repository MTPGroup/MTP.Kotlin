package tech.hanasaki.momotalk_plus.features.home.presentation.ui.widgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeTab

@Composable
fun HomeBottomNavigationBar(
    currentTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
) {
    val tabs = listOf(HomeTab.Chats, HomeTab.Contacts)
    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            HomeTab.Chats -> Icons.AutoMirrored.Filled.Chat
                            HomeTab.Contacts -> Icons.Default.Contacts
                        },
                        contentDescription = when (tab) {
                            HomeTab.Chats -> "消息"
                            HomeTab.Contacts -> "联系人"
                        }
                    )
                },
                label = {
                    Text(
                        when (tab) {
                            HomeTab.Chats -> "消息"
                            HomeTab.Contacts -> "联系人"
                        }
                    )
                },
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}
