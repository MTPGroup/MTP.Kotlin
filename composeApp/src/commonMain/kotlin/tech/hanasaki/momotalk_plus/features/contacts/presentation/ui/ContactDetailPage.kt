package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.composables.core.Dialog
import com.composables.core.DialogPanel
import com.composables.core.Scrim
import com.composables.core.rememberDialogState
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Filled
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.ChevronBack
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
import tech.hanasaki.momotalk_plus.features.contacts.presentation.ui.widgets.InfoRow
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

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is ContactDetailSideEffect.ShowErrorMessage ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(sideEffect.message, withDismissAction = true)
                    }

                is ContactDetailSideEffect.NavigateToContactsList ->
                    onNavigateBack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 自定义顶部栏
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 4.dp)
                        .zIndex(10f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 返回按钮
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorScheme.surface.copy(alpha = 0.9f))
                                .clickable(onClick = onNavigateBack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Ionicons.Filled.ChevronBack,
                                contentDescription = "返回",
                                modifier = Modifier.size(24.dp),
                                tint = colorScheme.onSurface
                            )
                        }

                        // 操作按钮组
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.surface.copy(alpha = 0.9f))
                                    .clickable(
                                        enabled = uiState.contact.creator.id == appUiState.currentUser?.id,
                                        onClick = { onNavigateToEditContact(userId) }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Ionicons.Outline.Create,
                                    contentDescription = "编辑",
                                    modifier = Modifier.size(22.dp),
                                    tint = if (uiState.contact.creator.id == appUiState.currentUser?.id)
                                        colorScheme.primary
                                    else
                                        colorScheme.outline
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.surface.copy(alpha = 0.9f))
                                    .clickable(onClick = { dialogState.visible = true }),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Ionicons.Outline.Trash,
                                    contentDescription = "删除",
                                    modifier = Modifier.size(22.dp),
                                    tint = colorScheme.error
                                )
                            }
                        }
                    }
                }

                // 头部背景区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .offset(y = (-56).dp)
                ) {
                    // 背景图片
                    Image(
                        painter = painterResource(Res.drawable.default_banner),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )

                    // 渐变遮罩
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )

                    // 头像
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .offset(y = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 头像背景模糊效果
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .background(colorScheme.surface.copy(alpha = 0.2f))
                                .blur(20.dp)
                        )

                        AsyncImage(
                            model = uiState.contact.avatarUrl.ifBlank {
                                "https://v2.xxapi.cn/api/head?return=302"
                            },
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 5.dp,
                                    color = colorScheme.surface,
                                    shape = CircleShape
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // 主要内容区域
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-56).dp)
                        .padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 名字
                    Text(
                        text = uiState.contact.name,
                        fontSize = 28.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = colorScheme.onBackground
                    )

                    // 签名
                    Text(
                        text = uiState.contact.signature,
                        fontSize = 15.sp,
                        color = colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 人物设定卡片
                    if (uiState.contact.persona.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colorScheme.surfaceContainerHighest)
                                .padding(20.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(colorScheme.primary)
                                    )
                                    Text(
                                        text = "人物设定",
                                        fontSize = 16.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                        color = colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = uiState.contact.persona,
                                    fontSize = 14.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    // 详细信息区域
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 创建者
                        InfoRow(
                            icon = Ionicons.Outline.PersonCircle,
                            label = "创建者",
                            value = uiState.contact.creator.name,
                            iconColor = colorScheme.primary
                        )

                        // 可见性
                        InfoRow(
                            icon = if (uiState.contact.visibility == Visibility.PUBLIC)
                                Ionicons.Outline.Earth
                            else
                                Ionicons.Outline.LockClosed,
                            label = "可见性",
                            value = if (uiState.contact.visibility == Visibility.PUBLIC) "公开" else "私有",
                            iconColor = if (uiState.contact.visibility == Visibility.PUBLIC)
                                colorScheme.tertiary
                            else
                                colorScheme.outline
                        )

                        // 创建时间
                        InfoRow(
                            icon = Ionicons.Outline.Calendar,
                            label = "创建时间",
                            value = uiState.contact.createdAt,
                            iconColor = colorScheme.secondary
                        )

                        // 更新时间
                        InfoRow(
                            icon = Ionicons.Outline.Time,
                            label = "更新时间",
                            value = uiState.contact.updatedAt,
                            iconColor = colorScheme.tertiary
                        )
                    }
                }
            }
        }

        // 删除确认对话框
        Dialog(state = dialogState) {
            Scrim()
            DialogPanel(
                modifier = Modifier
                    .displayCutoutPadding()
                    .systemBarsPadding()
                    .widthIn(min = 280.dp, max = 400.dp)
                    .padding(24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 图标
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Ionicons.Outline.Trash,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = colorScheme.error
                        )
                    }

                    Text(
                        text = "确认删除？",
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = colorScheme.onSurface
                    )

                    Text(
                        text = "删除后将无法恢复该联系人",
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.surfaceVariant)
                                .clickable { dialogState.visible = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "取消",
                                fontSize = 15.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.error)
                                .clickable {
                                    onIntent(ContactDetailIntent.DeleteContact(userId))
                                    dialogState.visible = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "删除",
                                fontSize = 15.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = colorScheme.onError
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

