package tech.hanasaki.momotalk_plus.features.auth.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.*
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.ui.widgets.MTopBar
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordState
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = koinViewModel(),
) {
    val uiState by viewModel.container.stateFlow.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 2 }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.container) {
        viewModel.container.sideEffectFlow.collect { effect ->
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
            MTopBar(
                title = "忘记密码",
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) { page ->
            // 根据页面索引显示不同内容
            when (page) {
                0 -> RequestEmailStep(uiState = uiState, onIntent = viewModel::onIntent)
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
    onIntent: (ForgotPasswordIntent) -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Ionicons.Outline.RefreshCircle,
            contentDescription = "重置密码图标",
            modifier = Modifier.size(100.dp),
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
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.Mail,
                    contentDescription = "邮箱图标",
                    modifier = Modifier.size(24.dp),
                )
            },
            supportingText = {
                if (uiState.error != null && uiState.error.contains("邮箱")) {
                    Text(uiState.error)
                }
            },
            shape = MaterialTheme.shapes.small,
        )
        OutlinedTextField(
            value = uiState.newPassword,
            onValueChange = { onIntent(ForgotPasswordIntent.PasswordChanged(it)) },
            label = { Text("新密码") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.error != null,
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.LockClosed,
                    contentDescription = "密码图标",
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
            supportingText = {
                if (uiState.error != null && uiState.error.contains("密码")) {
                    Text(uiState.error)
                }
            },
            shape = MaterialTheme.shapes.small,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = uiState.otpCode,
                onValueChange = { onIntent(ForgotPasswordIntent.VerificationCodeChanged(it)) },
                label = { Text("验证码") },
                modifier = Modifier.weight(1f),
                isError = uiState.error != null,
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Ionicons.Outline.ShieldCheckmark,
                        contentDescription = "验证码图标",
                        modifier = Modifier.size(24.dp),
                    )
                },
                supportingText = {
                    if (uiState.error != null && uiState.error.contains("验证码")) {
                        Text(uiState.error)
                    }
                },
                shape = MaterialTheme.shapes.small,
            )
            Button(
                onClick = { onIntent(ForgotPasswordIntent.SendVerificationCode) },
                enabled = !uiState.isRequestingCode,
            ) {
                Text("获取验证码")
            }
        }
        Button(
            onClick = { onIntent(ForgotPasswordIntent.ResetPasswordClicked) },
            enabled = uiState.email.isNotBlank() && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("更改密码")
        }
    }
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
            imageVector = Ionicons.Outline.CheckmarkCircle,
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
