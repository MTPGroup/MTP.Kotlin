package tech.hanasaki.momotalk_plus.features.chats.presentation.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.core.Dialog
import com.composables.core.DialogPanel
import com.composables.core.Scrim
import com.composables.core.rememberDialogState
import tech.hanasaki.momotalk_plus.core.domain.repository.ContactInfo

@Composable
fun CreateChatDialog(
    contacts: List<ContactInfo>,
    isLoading: Boolean,
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onCreateChat: (characterId: String, title: String, description: String, avatarUrl: String) -> Unit,
) {
    val dialogState = rememberDialogState(initiallyVisible = true)
    var selectedContact by remember { mutableStateOf<ContactInfo?>(null) }
    var step by remember { mutableStateOf(1) } // 1: 选择联系人, 2: 自定义信息
    var customTitle by remember { mutableStateOf("") }
    var customDescription by remember { mutableStateOf("") }

    LaunchedEffect(dialogState.visible) {
        if (!dialogState.visible) {
            onDismiss()
        }
    }

    Dialog(state = dialogState) {
        Scrim()
        DialogPanel(
            modifier = Modifier
                .displayCutoutPadding()
                .systemBarsPadding()
                .widthIn(min = 320.dp, max = 640.dp)
                .heightIn(max = 600.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
                Text(
                    text = if (step == 1) "选择角色" else "自定义会话",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                // 内容
                when (step) {
                    1 -> ContactSelectionStep(
                        contacts = contacts,
                        isLoading = isLoading,
                        selectedContact = selectedContact,
                        onContactSelected = { selectedContact = it }
                    )

                    2 -> CustomizationStep(
                        contact = selectedContact!!,
                        title = customTitle,
                        description = customDescription,
                        onTitleChanged = { customTitle = it },
                        onDescriptionChanged = { customDescription = it }
                    )
                }

                Spacer(Modifier.height(24.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (step == 2) {
                                step = 1
                            } else {
                                dialogState.visible = false
                            }
                        },
                        enabled = !isCreating
                    ) {
                        Text(if (step == 2) "上一步" else "取消")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (step == 1) {
                                if (selectedContact != null) {
                                    customTitle = selectedContact!!.name
                                    customDescription = selectedContact!!.signature
                                    step = 2
                                }
                            } else {
                                onCreateChat(
                                    selectedContact!!.id,
                                    customTitle,
                                    customDescription,
                                    selectedContact!!.avatarUrl
                                )
                            }
                        },
                        enabled = when (step) {
                            1 -> selectedContact != null && !isCreating
                            2 -> customTitle.isNotBlank() && !isCreating
                            else -> false
                        }
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (step == 1) "下一步" else "创建")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactSelectionStep(
    contacts: List<ContactInfo>,
    isLoading: Boolean,
    selectedContact: ContactInfo?,
    onContactSelected: (ContactInfo) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            contacts.isEmpty() -> {
                Text(
                    text = "暂无可用角色",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        ContactItem(
                            contact = contact,
                            isSelected = selectedContact?.id == contact.id,
                            onClick = { onContactSelected(contact) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactItem(
    contact: ContactInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box {
                AsyncImage(
                    model = contact.avatarUrl.ifBlank { "https://v2.xxapi.cn/api/head?return=302" },
                    contentDescription = "${contact.name} 的头像",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )

                if (isSelected) {
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "已选择",
                            modifier = Modifier
                                .padding(2.dp)
                                .size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = contact.signature,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CustomizationStep(
    contact: ContactInfo,
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 预览卡片
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = contact.avatarUrl.ifBlank { "https://v2.xxapi.cn/api/head?return=302" },
                    contentDescription = "${contact.name} 的头像",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.ifBlank { "未命名会话" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description.ifBlank { "暂无描述" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }

        // 输入字段
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChanged,
            label = { Text("会话标题") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            label = { Text("会话描述") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            maxLines = 4,
            shape = RoundedCornerShape(8.dp)
        )
    }
}