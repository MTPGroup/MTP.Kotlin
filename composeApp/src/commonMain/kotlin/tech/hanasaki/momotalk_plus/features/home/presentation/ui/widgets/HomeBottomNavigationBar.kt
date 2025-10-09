package tech.hanasaki.momotalk_plus.features.home.presentation.ui.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import momotalkplus.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import tech.hanasaki.momotalk_plus.features.home.presentation.state.HomeTab

@Composable
fun HomeBottomNavigationBar(
    currentTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val tabs = listOf(HomeTab.Chats, HomeTab.Contacts)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val selected = currentTab == tab

            NavigationItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                selectedIcon = when (tab) {
                    HomeTab.Chats -> painterResource(Res.drawable.ic_message_selected)
                    HomeTab.Contacts -> painterResource(Res.drawable.ic_contact_selected)
                },
                unselectedIcon = when (tab) {
                    HomeTab.Chats -> painterResource(Res.drawable.ic_message)
                    HomeTab.Contacts -> painterResource(Res.drawable.ic_contact)
                },
                label = when (tab) {
                    HomeTab.Chats -> "消息"
                    HomeTab.Contacts -> "联系人"
                }
            )
        }
    }
}

@Composable
private fun NavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    selectedIcon: Painter,
    unselectedIcon: Painter,
    label: String,
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }

    // 颜色动画
    val contentColor by animateColorAsState(
        targetValue = if (selected)
            colorScheme.primary
        else
            colorScheme.onSurfaceVariant,
        animationSpec = tween(300)
    )

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = if (selected) selectedIcon else unselectedIcon,
            contentDescription = label,
            modifier = Modifier.size(40.dp),
        )

        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}
