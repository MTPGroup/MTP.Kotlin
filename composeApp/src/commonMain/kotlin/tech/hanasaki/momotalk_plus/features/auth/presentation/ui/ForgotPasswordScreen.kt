package tech.hanasaki.momotalk_plus.features.auth.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.core.utils.decodeBase64ToBitmap
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordState
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 2 }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ForgotPasswordSideEffect.NavigateToSuccess ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }

                is ForgotPasswordSideEffect.ShowToast -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message = effect.message, withDismissAction = true)
                    }
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
                title = { Text("忘记密码") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            // 用户只有在获取到验证令牌后才能滑动到下一步
            userScrollEnabled = uiState.verificationToken.isNotBlank()
        ) { page ->
            // 根据页面索引显示不同内容
            when (page) {
                0 -> RequestEmailStep(uiState = uiState, onIntent = viewModel::processIntent)
                1 -> ResetSuccessStep(
                    onNavigateToLogin = onNavigateBack,
                )
            }
        }
    }
}


@Composable
private fun RequestEmailStep(
    uiState: ForgotPasswordState,
    onIntent: (ForgotPasswordIntent) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    if (uiState.showCaptchaDialog) {
        CaptchaDialog(
            uiState = uiState,
            onIntent = onIntent
        )
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.LockReset,
            contentDescription = "重置密码图标",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "重置您的密码",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "请输入关联您账户的邮箱地址，我们将向您发送验证码。",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onIntent(ForgotPasswordIntent.EmailChanged(it)) },
            label = { Text("邮箱") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.error != null,
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "邮箱图标") },
            supportingText = {
                if (uiState.error != null) {
                    Text(uiState.error)
                }
            }
        )
        OutlinedTextField(
            value = uiState.newPassword,
            onValueChange = { onIntent(ForgotPasswordIntent.PasswordChanged(it)) },
            label = { Text("新密码") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.error != null,
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Password, contentDescription = "密码图标") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                    )
                }
            },
            supportingText = {
                if (uiState.error != null) {
                    Text(uiState.error)
                }
            }
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = uiState.verificationCode,
                onValueChange = { onIntent(ForgotPasswordIntent.VerificationCodeChanged(it)) },
                label = { Text("验证码") },
                modifier = Modifier.weight(1f),
                isError = uiState.error != null,
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "验证码图标") },
                supportingText = {
                    if (uiState.error != null) {
                        Text(uiState.error)
                    }
                }
            )
            Button(
                onClick = { onIntent(ForgotPasswordIntent.GetCaptcha) },
                enabled = !uiState.isLoading,
            ) {
                Text("获取验证码")
            }
        }
        Button(
            onClick = { onIntent(ForgotPasswordIntent.VerifyCode) },
            enabled = uiState.email.isNotBlank() && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("更改密码")
        }
    }
}

@Composable
private fun CaptchaDialog(
    uiState: ForgotPasswordState,
    onIntent: (ForgotPasswordIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(ForgotPasswordIntent.DismissCaptchaDialog) },
        title = { Text("请输入图片验证码") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 将 Base64 字符串解码为图片
                val imageBitmap: ImageBitmap? = remember(uiState.captchaImage) {
                    uiState.captchaImage?.let { decodeBase64ToBitmap(it) }
                }

                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "图片验证码",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(vertical = 8.dp)
                    )
                } else {
                    // 图片加载失败或加载中
                    Box(modifier = Modifier.height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                OutlinedTextField(
                    value = uiState.captchaInput,
                    onValueChange = { onIntent(ForgotPasswordIntent.CaptchaInputChanged(it)) },
                    label = { Text("验证码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.error != null
                )
                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onIntent(ForgotPasswordIntent.VerifyCaptcha) },
                enabled = !uiState.isRequestingCode
            ) {
                if (uiState.isRequestingCode) {
                    CircularProgressIndicator(Modifier.size(20.dp))
                } else {
                    Text("确认")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(ForgotPasswordIntent.DismissCaptchaDialog) }) {
                Text("取消")
            }
        }
    )
}

/**
 * 重置成功页面
 */
@Composable
private fun ResetSuccessStep(onNavigateToLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "重置密码成功",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "重置密码成功！",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "现在您可以使用新密码登录您的账户。",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("前往登录")
        }
    }
}
