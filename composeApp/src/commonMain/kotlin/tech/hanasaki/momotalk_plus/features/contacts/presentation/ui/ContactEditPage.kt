@file:OptIn(kotlin.time.ExperimentalTime::class)

package tech.hanasaki.momotalk_plus.features.contacts.presentation.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Filled
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.Checkmark
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.ChevronBack
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.PersonCircle
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.*
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility
import tech.hanasaki.momotalk_plus.core.utils.rememberImagePicker
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactEditIndent
import tech.hanasaki.momotalk_plus.features.contacts.presentation.state.ContactEditSideEffect
import tech.hanasaki.momotalk_plus.features.contacts.presentation.ui.widgets.CustomTextField
import tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel.ContactEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditPage(
    currentUser: User?,
    contactId: String,
    onNavigateBack: () -> Unit,
    viewModel: ContactEditViewModel = koinViewModel(),
) {
    val uiState by viewModel.container.stateFlow.collectAsState()
    val onIntent = viewModel::onIntent
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val painter = rememberAsyncImagePainter(uiState.avatarUrl)
    val avatarState by painter.state.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    val launchImagePicker = rememberImagePicker { imageData ->
        if (imageData != null) {
            onIntent(ContactEditIndent.UploadAvatar(imageData, currentUser?.id))
        }
    }

    LaunchedEffect(viewModel.container) {
        viewModel.container.sideEffectFlow.collect { sideEffect ->
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
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

                    // 标题
                    Text(
                        text = "编辑联系人",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onBackground
                    )

                    // 保存按钮
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.isSaving) colorScheme.surfaceVariant
                                else colorScheme.primary
                            )
                            .clickable(
                                enabled = !uiState.isSaving,
                                onClick = { onIntent(ContactEditIndent.UpdateContactInfo(contactId)) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                Ionicons.Outline.Save,
                                contentDescription = "保存",
                                modifier = Modifier.size(22.dp),
                                tint = colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
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

                // 头像区域
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        // 背景模糊效果
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primary.copy(alpha = 0.1f))
                                .blur(20.dp)
                        )

                        when (avatarState) {
                            is AsyncImagePainter.State.Empty,
                            is AsyncImagePainter.State.Loading,
                                -> {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(40.dp),
                                        strokeWidth = 3.dp,
                                        color = colorScheme.primary
                                    )
                                }
                            }

                            is AsyncImagePainter.State.Success -> {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        uiState.avatarUrl.ifEmpty { "https://via.placeholder.com/120" }
                                    ),
                                    contentDescription = "头像",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .border(4.dp, colorScheme.surface, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            is AsyncImagePainter.State.Error -> {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Ionicons.Filled.PersonCircle,
                                        contentDescription = "头像",
                                        modifier = Modifier.size(80.dp),
                                        tint = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (uiState.isUploadingAvatar) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    strokeWidth = 3.dp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // 更换头像按钮
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(colorScheme.secondaryContainer)
                            .clickable(
                                enabled = !uiState.isUploadingAvatar,
                                onClick = {
                                    println("[ContactEditPage] Upload avatar button clicked")
                                    launchImagePicker()
                                }
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Ionicons.Outline.Camera,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = if (uiState.isUploadingAvatar) "上传中..." else "更换头像",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // 表单部分
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 角色名称
                    CustomTextField(
                        value = uiState.name,
                        onValueChange = { onIntent(ContactEditIndent.NameChanged(it)) },
                        label = "角色名称",
                        icon = Ionicons.Outline.Person,
                        singleLine = true
                    )

                    // 个性签名
                    CustomTextField(
                        value = uiState.signature,
                        onValueChange = { onIntent(ContactEditIndent.SignatureChanged(it)) },
                        label = "个性签名",
                        icon = Ionicons.Outline.Text,
                        maxLines = 3
                    )

                    // 角色人设
                    CustomTextField(
                        value = uiState.persona,
                        onValueChange = { onIntent(ContactEditIndent.PersonaChanged(it)) },
                        label = "角色人设",
                        icon = Ionicons.Outline.Document,
                        minLines = 5,
                        maxLines = 10
                    )

                    // 可见性设置
                    Column(
                        modifier = Modifier.fillMaxWidth(),
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
                                text = "可见性设置",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colorScheme.surfaceContainerHighest),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            VisibilityOption(
                                selected = uiState.visibility == Visibility.PUBLIC,
                                title = "公开",
                                description = "所有人可见",
                                icon = Ionicons.Outline.Earth,
                                onClick = { onIntent(ContactEditIndent.VisibilityChanged(Visibility.PUBLIC)) }
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .padding(horizontal = 16.dp)
                                    .background(colorScheme.outlineVariant)
                            )

                            VisibilityOption(
                                selected = uiState.visibility == Visibility.PRIVATE,
                                title = "私密",
                                description = "仅自己可见",
                                icon = Ionicons.Outline.LockClosed,
                                onClick = { onIntent(ContactEditIndent.VisibilityChanged(Visibility.PRIVATE)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
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


@Composable
private fun VisibilityOption(
    selected: Boolean,
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (selected) colorScheme.primaryContainer
                    else colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) colorScheme.primary else colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (selected) colorScheme.primary else colorScheme.outline,
                    shape = CircleShape
                )
                .background(
                    if (selected) colorScheme.primary else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Ionicons.Filled.Checkmark,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = colorScheme.onPrimary
                )
            }
        }
    }
}