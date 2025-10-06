package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Filled
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.*
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Save
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactEditIndent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactEditSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel.ContactEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditPage(
    contactId: String,
    onNavigateBack: () -> Unit,
    viewModel: ContactEditViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val onIntent = viewModel::processIntent
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        onIntent(ContactEditIndent.LoadContactInfo(contactId))
    }

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is ContactEditSideEffect.ShowMessage ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            sideEffect.message,
                            withDismissAction = true,
                        )
                    }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text("编辑联系人") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Ionicons.Filled.ChevronBack,
                            contentDescription = "返回",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onIntent(ContactEditIndent.UpdateContactInfo(contactId))
                        },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Ionicons.Outline.Save,
                                contentDescription = "保存",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // 头像
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = uiState.avatarUrl.ifEmpty { "https://via.placeholder.com/120" },
                        contentDescription = "头像",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Black, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    TextButton(
                        onClick = { /* TODO: 选择头像 */ },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Ionicons.Filled.Camera,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                        Text("更换头像")
                    }
                }

                // 表单部分
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = {
                            onIntent(ContactEditIndent.NameChanged(it))
                        },
                        label = { Text("角色名称") },
                        leadingIcon = {
                            Icon(
                                imageVector = Ionicons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 签名
                    OutlinedTextField(
                        value = uiState.signature,
                        onValueChange = {
                            onIntent(ContactEditIndent.SignatureChanged(it))
                        },
                        label = { Text("个性签名") },
                        leadingIcon = {
                            Icon(
                                imageVector = Ionicons.Filled.Text,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // 人设
                    OutlinedTextField(
                        value = uiState.persona,
                        onValueChange = {
                            onIntent(ContactEditIndent.PersonaChanged(it))
                        },
                        label = { Text("角色人设") },
                        leadingIcon = {
                            Icon(
                                imageVector = Ionicons.Filled.Document,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        maxLines = 10
                    )

                    // 可见性
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "可见性设置",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                VisibilityOption(
                                    selected = uiState.visibility == Visibility.PUBLIC,
                                    title = "公开",
                                    description = "所有人可见",
                                    icon = Ionicons.Filled.Globe,
                                    onClick = {
                                        onIntent(ContactEditIndent.VisibilityChanged(Visibility.PUBLIC))
                                    }
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                VisibilityOption(
                                    selected = uiState.visibility == Visibility.PRIVATE,
                                    title = "私密",
                                    description = "仅自己可见",
                                    icon = Ionicons.Filled.LockClosed,
                                    onClick = {
                                        onIntent(ContactEditIndent.VisibilityChanged(Visibility.PRIVATE))
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun VisibilityOption(
    selected: Boolean,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}