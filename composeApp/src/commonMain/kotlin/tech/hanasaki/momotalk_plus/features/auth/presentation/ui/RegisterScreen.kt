package tech.hanasaki.momotalk_plus.features.auth.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.*
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.ui.widgets.MTopBar
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterState
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 3 }
    val tabTitles = listOf("1. 注册账号", "2. 验证邮箱")
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
            MTopBar(
                title = "创建账户",
                onNavigateBack = onNavigateBack,
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
                                // 允许用户在已验证邮箱后点击切换
                                if (uiState.isEmailValid && index < pagerState.currentPage) {
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
            ) { page ->
                // 根据页面索引显示不同内容
                when (page) {
                    0 -> UserInfoStep(
                        uiState = uiState,
                        onIntent = viewModel::processIntent,
                        onNavigateToLogin = onNavigateBack
                    )

                    1 -> VerificationStep(uiState = uiState, onIntent = viewModel::processIntent)

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
    onIntent: (RegisterIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "验证您的邮箱",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "我们已向您的邮箱发送了验证码，请输入以完成验证。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.otpCode,
            onValueChange = { onIntent(RegisterIntent.OTPCodeChanged(it)) },
            label = { Text("6位验证码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.Mail,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            isError = uiState.error?.contains("验证码") == true,
            supportingText = { if (uiState.error != null) Text(uiState.error) },
            shape = MaterialTheme.shapes.small,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = { onIntent(RegisterIntent.ResendOTPCodeClicked) },
                enabled = !uiState.isLoading,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(
                    "没有收到？重新发送",
                    textDecoration = TextDecoration.Underline,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onIntent(RegisterIntent.VerifyEmailClicked) },
            enabled = uiState.otpCode.isNotBlank() && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("验证")
            }
        }
    }
}

@Composable
private fun UserInfoStep(
    uiState: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
    onNavigateToLogin: () -> Unit,
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
            value = uiState.email,
            onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
            label = { Text("请输入邮箱") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.Mail,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            isError = uiState.error?.contains("邮箱") == true,
            shape = MaterialTheme.shapes.small,
        )

        OutlinedTextField(
            value = uiState.username,
            onValueChange = { onIntent(RegisterIntent.UsernameChanged(it)) },
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.PersonCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            isError = uiState.error?.contains("用户名") == true,
            shape = MaterialTheme.shapes.small,
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
            label = { Text("密码 (至少8位)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.LockClosed,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image =
                    if (passwordVisible) Ionicons.Outline.EyeOff else Ionicons.Outline.Eye
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
            isError = uiState.error?.contains("密码") == true,
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = { onIntent(RegisterIntent.ConfirmPasswordChanged(it)) },
            label = { Text("确认密码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.LockClosed,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = uiState.error?.contains("密码不一致") == true,
            supportingText = { if (uiState.error?.contains("密码不一致") == true) Text(uiState.error) },
            shape = MaterialTheme.shapes.small
        )

        Button(
            onClick = { onIntent(RegisterIntent.RegisterClicked) },
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

        TextButton(onClick = onNavigateToLogin) {
            Text(
                "已有账户？直接登录",
                textDecoration = TextDecoration.Underline,
            )
        }
    }
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
            imageVector = Ionicons.Outline.Checkmark,
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