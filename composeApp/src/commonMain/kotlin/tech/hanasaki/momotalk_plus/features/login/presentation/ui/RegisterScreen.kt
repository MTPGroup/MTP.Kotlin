package tech.hanasaki.momotalk_plus.features.login.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
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
import tech.hanasaki.momotalk_plus.features.login.presentation.state.RegisterIntent
import tech.hanasaki.momotalk_plus.features.login.presentation.state.RegisterSideEffect
import tech.hanasaki.momotalk_plus.features.login.presentation.state.RegisterState
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 3 }
    val tabTitles = listOf("1. 验证账户", "2. 设置信息")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is RegisterSideEffect.NavigateToLogin -> onNavigateBack()
                is RegisterSideEffect.ShowToast -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            withDismissAction = true,
                        )
                    }
                }

                is RegisterSideEffect.NavigateToNextStep -> {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }

                is RegisterSideEffect.NavigateToSuccessStep -> {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("创建账户") },
                navigationIcon = {
                    if (pagerState.currentPage < 2) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                if (pagerState.currentPage < 2) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                // 允许用户在已验证令牌后点击切换
                                if (uiState.verificationToken.isNotBlank() || index < pagerState.currentPage) {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                // 用户只有在获取到验证令牌后才能滑动到下一步
                userScrollEnabled = uiState.verificationToken.isNotBlank()
            ) { page ->
                // 根据页面索引显示不同内容
                when (page) {
                    0 -> VerificationStep(uiState = uiState, onIntent = viewModel::processIntent)
                    1 -> UserInfoStep(
                        uiState = uiState,
                        onIntent = viewModel::processIntent,
                        onNavigateToLogin = onNavigateBack
                    )

                    2 -> ResetSuccessStep(
                        onNavigateToLogin = onNavigateBack,
                    )
                }
            }
        }
    }
}

@Composable
private fun VerificationStep(
    uiState: RegisterState,
    onIntent: (RegisterIntent) -> Unit
) {
    if (uiState.showCaptchaDialog) {
        CaptchaDialog(
            uiState = uiState,
            onIntent = onIntent
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("输入邮箱或手机号", style = MaterialTheme.typography.titleLarge)
        Text(
            "我们将向您发送验证码以验证您的身份。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.binding,
            onValueChange = { onIntent(RegisterIntent.BindingChanged(it)) },
            label = { Text("邮箱或手机号") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
            isError = uiState.error?.contains("邮箱") == true || uiState.error?.contains("手机号") == true,
            supportingText = { if (uiState.error != null) Text(uiState.error) },
            shape = MaterialTheme.shapes.medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.verificationCode,
                onValueChange = { onIntent(RegisterIntent.VerificationCodeChanged(it)) },
                label = { Text("验证码") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.MailOutline, contentDescription = null) },
                isError = uiState.error?.contains("验证码") == true,
                shape = MaterialTheme.shapes.medium
            )
            Button(
                onClick = { onIntent(RegisterIntent.RequestVerificationCode) }, // 点击发送时触发获取图片验证码
                enabled = uiState.binding.isNotBlank() && !uiState.isLoading,
                modifier = Modifier.height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("发送")
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { onIntent(RegisterIntent.VerifyCodeAndProceed) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("下一步")
            }
        }
    }
}

@Composable
private fun UserInfoStep(
    uiState: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("设置您的账户信息", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.username,
            onValueChange = { onIntent(RegisterIntent.UsernameChanged(it)) },
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.AccountBox, contentDescription = null) },
            isError = uiState.error?.contains("用户名") == true,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
            label = { Text("密码 (至少6位)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
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
            isError = uiState.error?.contains("密码") == true,
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = { onIntent(RegisterIntent.ConfirmPasswordChanged(it)) },
            label = { Text("确认密码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = uiState.error?.contains("密码不一致") == true,
            supportingText = { if (uiState.error?.contains("密码不一致") == true) Text(uiState.error) },
            shape = MaterialTheme.shapes.medium
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { onIntent(RegisterIntent.RegisterClicked) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("完成注册")
            }
        }

        TextButton(onClick = onNavigateToLogin) {
            Text("已有账户？直接登录")
        }
    }
}

@Composable
private fun CaptchaDialog(
    uiState: RegisterState,
    onIntent: (RegisterIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(RegisterIntent.DismissCaptchaDialog) },
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
                    onValueChange = { onIntent(RegisterIntent.CaptchaInputChanged(it)) },
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
                onClick = { onIntent(RegisterIntent.SubmitCaptcha) },
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
            TextButton(onClick = { onIntent(RegisterIntent.DismissCaptchaDialog) }) {
                Text("取消")
            }
        }
    )
}

/**
 * 注册成功页面
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
            contentDescription = "注册成功",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "注册成功！",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "欢迎加入 MomoTalk Plus，立即登录开始体验吧。",
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