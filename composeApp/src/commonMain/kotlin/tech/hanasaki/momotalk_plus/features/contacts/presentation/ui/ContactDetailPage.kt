package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import momotalkplus.composeapp.generated.resources.Res
import momotalkplus.composeapp.generated.resources.default_banner
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactDetailIntent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel.ContactDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailPage(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: ContactDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val onIntent = viewModel::processIntent

    LaunchedEffect(Unit) {
        onIntent(ContactDetailIntent.LoadContact(userId))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.contact.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回上一页",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: 编辑联系人 */ }) {
                        Icon(
                            Icons.Default.EditNote,
                            contentDescription = "编辑联系人信息",
                        )
                    }
                    IconButton(onClick = { /* TODO: 删除联系人 */ }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除联系人",
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
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
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.TopCenter
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
                            .offset(y = 200.dp - 40.dp),
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
                }

                Spacer(modifier = Modifier.height(50.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 发送消息按钮
                    Button(
                        onClick = { /* TODO: 发送消息 */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row {
                            Icon(
                                Icons.Default.Message,
                                contentDescription = "发送消息",
                                modifier = Modifier.padding(end = 8.dp)
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
                                text = uiState.contact.persona.ifBlank { "暂无设定。" },
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
                                        Icons.Default.Face,
                                        contentDescription = "创建者"
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
                                        if (uiState.contact.visibility == Visibility.PUBLIC) Icons.Default.Public else Icons.Default.Lock,
                                        contentDescription = "可见性"
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
                                        Icons.Default.CalendarMonth,
                                        contentDescription = "创建日期"
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
                                        Icons.Default.CalendarMonth,
                                        contentDescription = "更新日期"
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