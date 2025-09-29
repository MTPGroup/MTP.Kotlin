package tech.hanasaki.momotalk_plus.features.home.presentation.ui.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.ChatbubbleEllipses
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.People
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeTab

@Composable
fun HomeBottomNavigationBar(
    currentTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
) {
    val tabs = listOf(HomeTab.Chats, HomeTab.Contacts)
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            HomeTab.Chats -> Ionicons.Outline.ChatbubbleEllipses
                            HomeTab.Contacts -> Ionicons.Outline.People
                        },
                        contentDescription = when (tab) {
                            HomeTab.Chats -> "消息"
                            HomeTab.Contacts -> "联系人"
                        },
                        modifier = Modifier.size(24.dp)
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
                onClick = { onTabSelected(tab) },
            )
        }
    }
}
