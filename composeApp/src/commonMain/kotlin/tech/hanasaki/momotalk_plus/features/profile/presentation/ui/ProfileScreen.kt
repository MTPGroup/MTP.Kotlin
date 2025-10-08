package tech.hanasaki.momotalk_plus.features.profile.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Filled
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.filled.ChevronBack
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.*
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.utils.rememberImagePicker
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileIntent
import tech.hanasaki.momotalk_plus.features.profile.presentation.state.ProfileSideEffect
import tech.hanasaki.momotalk_plus.features.profile.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: UserProfile?,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel { parametersOf(currentUser) },
) {
    val uiState by viewModel.uiState.collectAsState()
    val onIntent = viewModel::processIntent
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        onIntent(ProfileIntent.LoadProfile)
    }

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ProfileSideEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }

                is ProfileSideEffect.NavigateToChangePassword -> {
                    // TODO: Navigate to change password screen
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("修改密码功能即将推出")
                    }
                }

                is ProfileSideEffect.NavigateToLogin -> {
                    onLogout()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "个人资料",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Ionicons.Filled.ChevronBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        TextButton(
                            onClick = { onIntent(ProfileIntent.CancelEdit) },
                            enabled = !uiState.isSaving
                        ) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = { onIntent(ProfileIntent.SaveProfile) },
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("保存")
                            }
                        }
                    } else {
                        IconButton(onClick = { onIntent(ProfileIntent.StartEdit) }) {
                            Icon(
                                Ionicons.Outline.Create,
                                contentDescription = "编辑",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 用户头像和基本信息
            item {
                ProfileHeader(
                    user = uiState.user,
                    isEditing = uiState.isEditing,
                    isUploadingAvatar = uiState.isUploadingAvatar,
                    onUploadAvatar = { imageData ->
                        onIntent(ProfileIntent.UploadAvatar(imageData, currentUser?.id))
                    }
                )
            }

            // 个人信息卡片
            item {
                ProfileInfoSection(
                    isEditing = uiState.isEditing,
                    name = if (uiState.isEditing) uiState.editedName else uiState.user?.name ?: "",
                    email = uiState.user?.email ?: "",
                    onNameChange = { onIntent(ProfileIntent.NameChanged(it)) }
                )
            }

            // 账户信息
            item {
                AccountInfoSection(
                    user = uiState.user
                )
            }

            // 账户操作
            item {
                AccountActionsSection(
                    onChangePassword = { onIntent(ProfileIntent.ChangePassword) },
                    onLogout = { onIntent(ProfileIntent.Logout) }
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    user: UserProfile?,
    isEditing: Boolean,
    isUploadingAvatar: Boolean,
    onUploadAvatar: (ImageData) -> Unit,
) {
    val painter = rememberAsyncImagePainter(
        user?.image ?: "https://cdn.hanasaki.tech/avatars/users/default_avatar.png"
    )
    val avatarState by painter.state.collectAsState()

    val launchImagePicker = rememberImagePicker { imageData ->
        if (imageData != null) {
            onUploadAvatar(imageData)
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isUploadingAvatar -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            strokeWidth = 3.dp
                        )
                    }

                    avatarState is AsyncImagePainter.State.Empty ||
                            avatarState is AsyncImagePainter.State.Loading -> {
                        CircularProgressIndicator()
                    }

                    avatarState is AsyncImagePainter.State.Success -> {
                        Image(
                            painter = painter,
                            contentDescription = "用户头像",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )
                    }

                    avatarState is AsyncImagePainter.State.Error -> {
                        Icon(
                            Ionicons.Outline.Person,
                            contentDescription = "默认头像",
                            modifier = Modifier.size(50.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (isEditing) {
                TextButton(
                    onClick = {
                        println("[ProfileScreen] Upload avatar button clicked")
                        launchImagePicker()
                    },
                    enabled = !isUploadingAvatar
                ) {
                    Icon(
                        Ionicons.Outline.Camera,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isUploadingAvatar) "上传中..." else "更换头像")
                }
            } else {
                Text(
                    text = user?.name ?: "未知用户",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoSection(
    isEditing: Boolean,
    name: String,
    email: String,
    onNameChange: (String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "个人信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (isEditing) {
                // 编辑模式 - 只允许编辑用户名
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("用户名") },
                    leadingIcon = {
                        Icon(
                            Ionicons.Outline.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                // 邮箱只读显示
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                ProfileInfoItem(
                    icon = Ionicons.Outline.Mail,
                    label = "邮箱",
                    value = email
                )
                Text(
                    text = "邮箱地址不可修改",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 36.dp)
                )
            } else {
                // 查看模式
                ProfileInfoItem(
                    icon = Ionicons.Outline.Person,
                    label = "用户名",
                    value = name
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                ProfileInfoItem(
                    icon = Ionicons.Outline.Mail,
                    label = "邮箱",
                    value = email
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AccountInfoSection(
    user: UserProfile?,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "账户信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            ProfileInfoItem(
                icon = Ionicons.Outline.IdCard,
                label = "用户ID",
                value = user?.id ?: "N/A"
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            ProfileInfoItem(
                icon = Ionicons.Outline.CheckmarkCircle,
                label = "邮箱验证",
                value = if (user?.emailVerified == true) "已验证" else "未验证"
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            ProfileInfoItem(
                icon = Ionicons.Outline.Time,
                label = "注册时间",
                value = user?.createdAt ?: "N/A"
            )
        }
    }
}

@Composable
private fun AccountActionsSection(
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ActionItem(
                icon = Ionicons.Outline.LockClosed,
                title = "修改密码",
                onClick = onChangePassword
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            ActionItem(
                icon = Ionicons.Outline.LogOut,
                title = "退出登录",
                onClick = onLogout,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )
        Icon(
            Ionicons.Outline.ChevronForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
