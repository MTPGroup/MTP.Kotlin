package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.AddCircle
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.RemoveCircle
import tech.hanasaki.momotalk_plus.core.domain.model.Character

@Composable
fun ContactManageListItem(
    character: Character,
    isAdded: Boolean,
    isProcessing: Boolean,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 头像区域
        Box(contentAlignment = Alignment.Center) {
            // 背景光晕
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAdded) colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        else colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
            )

            AsyncImage(
                model = character.avatarUrl.ifEmpty { "https://cdn.hanasaki.tech/avatars/characters/default_avatar.jpg" },
                contentDescription = "头像",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, colorScheme.surface, CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        // 信息区域
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = character.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (character.signature.isNotEmpty()) {
                Text(
                    text = character.signature,
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 操作按钮
        Box(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    when {
                        isProcessing -> colorScheme.surfaceVariant
                        isAdded -> colorScheme.tertiaryContainer
                        else -> colorScheme.primaryContainer
                    }
                )
                .clickable(
                    enabled = !isProcessing,
                    onClick = if (isAdded) onRemoveClick else onAddClick
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAdded)
                            Ionicons.Outline.RemoveCircle
                        else
                            Ionicons.Outline.AddCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isAdded)
                            colorScheme.onTertiaryContainer
                        else
                            colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (isAdded) "移除" else "添加",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isAdded)
                            colorScheme.onTertiaryContainer
                        else
                            colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
