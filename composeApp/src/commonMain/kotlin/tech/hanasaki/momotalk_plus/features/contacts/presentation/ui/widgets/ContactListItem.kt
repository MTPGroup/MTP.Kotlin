package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact

@Composable
fun ContactListItem(
    contact: Contact,
    onContactClick: (String) -> Unit,
) {
    ListItem(
        headlineContent = { Text(contact.name) },
        supportingContent = { Text(contact.signature) },
        leadingContent = {
            val avatarModifier = Modifier
                .size(40.dp)
                .clip(CircleShape)

            if (contact.avatarUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(contact.avatarUrl),
                    contentDescription = "用户头像",
                    modifier = avatarModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = rememberAsyncImagePainter("https://cdn.hanasaki.tech/avatars/characters/default_avatar.jpg"),
                    contentDescription = "默认头像",
                    modifier = avatarModifier,
                    contentScale = ContentScale.Crop
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onContactClick(contact.id) }),
    )
}