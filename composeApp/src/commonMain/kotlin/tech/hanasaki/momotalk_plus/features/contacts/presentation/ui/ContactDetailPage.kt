package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.core.Dialog
import com.composables.core.DialogPanel
import com.composables.core.Scrim
import com.composables.core.rememberDialogState
import com.composeunstyled.Button
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Filled
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.ChevronBack
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.Send
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.*
import kotlinx.coroutines.launch
import momotalkplus.composeapp.generated.resources.Res
import momotalkplus.composeapp.generated.resources.default_banner
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.viewmodel.AppViewModel
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel.ContactDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailPage(
    userId: String,
    onNavigateToEditContact: (contactId: String) -> Unit,
    onNavigateBack: () -> Unit,
    appViewModel: AppViewModel = koinViewModel(),
    viewModel: ContactDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val appUiState by appViewModel.uiState.collectAsState()
    val onIntent = viewModel::processIntent
    val dialogState = rememberDialogState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        onIntent(ContactDetailIntent.LoadContact(userId))
    }

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is ContactDetailSideEffect.ShowErrorMessage ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(sideEffect.message, withDismissAction = true)
                    }

                is ContactDetailSideEffect.NavigateToContactsList ->
                    onNavigateBack()

                is ContactDetailSideEffect.NavigateToChat ->
                    TODO("导航到聊天界面")
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.contact.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Ionicons.Filled.ChevronBack,
                            contentDescription = "返回上一页",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onNavigateToEditContact(userId)
                        },
                        enabled = uiState.contact.creator.id == appUiState.currentUser?.id
                    ) {
                        Icon(
                            Ionicons.Outline.Create,
                            contentDescription = "编辑联系人信息",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    IconButton(onClick = {
                        dialogState.visible = true
                    }) {
                        Icon(
                            Ionicons.Outline.Trash,
                            contentDescription = "删除联系人",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Dialog(state = dialogState) {
            Scrim()
            DialogPanel(
                modifier = Modifier
                    .displayCutoutPadding()
                    .systemBarsPadding()
                    .widthIn(min = 280.dp, max = 560.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text("确认删除该联系人吗？")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = { dialogState.visible = false }) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            onIntent(ContactDetailIntent.DeleteContact(userId))
                            dialogState.visible = false
                        }) {
                            Text("删除", color = Color.Red)
                        }
                    }
                }
            }
        }
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.default_banner),
                        contentDescription = "背景图",
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )

                    AsyncImage(
                        model = uiState.contact.avatarUrl.ifBlank {
                            "https://v2.xxapi.cn/api/head?return=302"
                        },
                        contentDescription = "${uiState.contact.name} 的头像",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Black, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-30).dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.contact.name,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            text = uiState.contact.signature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 发送消息按钮
                    Button(
                        onClick = { /* TODO: 发送消息 */ },
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentPadding = PaddingValues(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row {
                            Icon(
                                Ionicons.Filled.Send,
                                contentDescription = "发送消息",
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(24.dp)
                            )
                            Text("发送消息")
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "人物设定",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = uiState.contact.persona.ifBlank { "暂无设定" },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // 详细信息卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        val listColors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        )
                        Column {
                            ListItem(
                                colors = listColors,
                                headlineContent = { Text("创建者") },
                                supportingContent = { Text(uiState.contact.creator.name) },
                                leadingContent = {
                                    Icon(
                                        Ionicons.Outline.PersonCircle,
                                        contentDescription = "创建者",
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                colors = listColors,
                                headlineContent = { Text("可见性") },
                                supportingContent = { Text(if (uiState.contact.visibility == Visibility.PUBLIC) "公开" else "私有") },
                                leadingContent = {
                                    Icon(
                                        if (uiState.contact.visibility == Visibility.PUBLIC) Ionicons.Outline.Earth else Ionicons.Outline.LockClosed,
                                        contentDescription = "可见性",
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                colors = listColors,
                                headlineContent = { Text("创建于") },
                                supportingContent = { Text(uiState.contact.createdAt) },
                                leadingContent = {
                                    Icon(
                                        Ionicons.Outline.Calendar,
                                        contentDescription = "创建日期",
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                colors = listColors,
                                headlineContent = { Text("更新于") },
                                supportingContent = { Text(uiState.contact.updatedAt) },
                                leadingContent = {
                                    Icon(
                                        Ionicons.Outline.Calendar,
                                        contentDescription = "更新日期",
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}