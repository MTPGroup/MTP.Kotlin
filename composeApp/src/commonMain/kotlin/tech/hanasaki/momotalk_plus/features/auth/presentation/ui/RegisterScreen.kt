package tech.hanasaki.momotalk_plus.features.auth.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Checkmark
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Eye
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.EyeOff
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.LockClosed
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Mail
import momotalkplus.composeapp.generated.resources.Res
import momotalkplus.composeapp.generated.resources.auth_account_exists_login
import momotalkplus.composeapp.generated.resources.auth_confirm_password_label
import momotalkplus.composeapp.generated.resources.auth_create_account
import momotalkplus.composeapp.generated.resources.auth_email_input_label
import momotalkplus.composeapp.generated.resources.auth_email_verify_code_6_digit
import momotalkplus.composeapp.generated.resources.auth_email_verify_hint
import momotalkplus.composeapp.generated.resources.auth_email_verify_title
import momotalkplus.composeapp.generated.resources.auth_next_step
import momotalkplus.composeapp.generated.resources.auth_no_code_resend
import momotalkplus.composeapp.generated.resources.auth_password_hide
import momotalkplus.composeapp.generated.resources.auth_password_label_min
import momotalkplus.composeapp.generated.resources.auth_password_show
import momotalkplus.composeapp.generated.resources.auth_register_success
import momotalkplus.composeapp.generated.resources.auth_register_success_desc
import momotalkplus.composeapp.generated.resources.auth_register_success_icon_desc
import momotalkplus.composeapp.generated.resources.auth_resend_in_seconds
import momotalkplus.composeapp.generated.resources.auth_set_account_info
import momotalkplus.composeapp.generated.resources.auth_tab_sign_up
import momotalkplus.composeapp.generated.resources.auth_tab_verify_email
import momotalkplus.composeapp.generated.resources.auth_to_login
import momotalkplus.composeapp.generated.resources.auth_verify
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.ui.widgets.MTopBar
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterState
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.RegisterStep
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.asString
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.resolve
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    initialEmail: String? = null,
    forceVerify: Boolean = false,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val uiState by viewModel.container.stateFlow.collectAsState()
    val pagerState = rememberPagerState { 3 }

    LaunchedEffect(initialEmail, forceVerify) {
        if (forceVerify && !initialEmail.isNullOrBlank()) {
            viewModel.onIntent(RegisterIntent.InitializePendingVerification(initialEmail))
        }
    }
    val tabTitles = listOf(
        stringResource(Res.string.auth_tab_sign_up),
        stringResource(Res.string.auth_tab_verify_email),
    )
    val targetPage = when (uiState.currentStep) {
        RegisterStep.USER_INFO -> 0
        RegisterStep.VERIFY_EMAIL -> 1
        RegisterStep.SUCCESS -> 2
    }
    val tabIndex = minOf(targetPage, tabTitles.lastIndex)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(targetPage) {
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is RegisterSideEffect.NavigateToLogin -> onNavigateBack()
                is RegisterSideEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message.resolve(),
                        withDismissAction = true,
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            MTopBar(
                title = stringResource(Res.string.auth_create_account),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (pagerState.currentPage < 2) {
                PrimaryTabRow(selectedTabIndex = tabIndex) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { },
                            enabled = false,
                            text = { Text(title) },
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> UserInfoStep(
                        uiState = uiState,
                        onIntent = viewModel::onIntent,
                        onNavigateToLogin = onNavigateBack,
                    )

                    1 -> VerificationStep(uiState = uiState, onIntent = viewModel::onIntent)
                    2 -> RegisterSuccessStep(onNavigateToLogin = onNavigateBack)
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
            text = stringResource(Res.string.auth_email_verify_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.auth_email_verify_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.otpCode,
            onValueChange = { onIntent(RegisterIntent.OTPCodeChanged(it)) },
            label = { Text(stringResource(Res.string.auth_email_verify_code_6_digit)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.Mail,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            isError = uiState.otpError != null,
            supportingText = {
                uiState.otpError?.let { Text(it.asString()) }
            },
            shape = MaterialTheme.shapes.small,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = { onIntent(RegisterIntent.ResendOTPCodeClicked) },
                enabled = !uiState.isLoading && uiState.resendCooldownSeconds == 0,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                val resendText = if (uiState.resendCooldownSeconds > 0) {
                    stringResource(Res.string.auth_resend_in_seconds, uiState.resendCooldownSeconds)
                } else {
                    stringResource(Res.string.auth_no_code_resend)
                }
                Text(
                    resendText,
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
            shape = MaterialTheme.shapes.medium,
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(Res.string.auth_verify))
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
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(Res.string.auth_set_account_info), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
            label = { Text(stringResource(Res.string.auth_email_input_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.Mail,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            isError = uiState.emailError != null,
            supportingText = {
                uiState.emailError?.let { Text(it.asString()) }
            },
            shape = MaterialTheme.shapes.small,
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
            label = { Text(stringResource(Res.string.auth_password_label_min)) },
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
                val image = if (passwordVisible) Ionicons.Outline.EyeOff else Ionicons.Outline.Eye
                val description = if (passwordVisible) {
                    stringResource(Res.string.auth_password_hide)
                } else {
                    stringResource(Res.string.auth_password_show)
                }
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = description,
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
            isError = uiState.passwordError != null,
            supportingText = {
                uiState.passwordError?.let { Text(it.asString()) }
            },
            shape = MaterialTheme.shapes.small,
        )

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = { onIntent(RegisterIntent.ConfirmPasswordChanged(it)) },
            label = { Text(stringResource(Res.string.auth_confirm_password_label)) },
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
            trailingIcon = {
                val image = if (confirmPasswordVisible) Ionicons.Outline.EyeOff else Ionicons.Outline.Eye
                val description = if (confirmPasswordVisible) {
                    stringResource(Res.string.auth_password_hide)
                } else {
                    stringResource(Res.string.auth_password_show)
                }
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = description,
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
            isError = uiState.confirmPasswordError != null,
            supportingText = {
                uiState.confirmPasswordError?.let { Text(it.asString()) }
            },
            shape = MaterialTheme.shapes.small,
        )

        Button(
            onClick = { onIntent(RegisterIntent.RegisterClicked) },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(Res.string.auth_next_step))
            }
        }

        TextButton(onClick = onNavigateToLogin) {
            Text(
                stringResource(Res.string.auth_account_exists_login),
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Composable
private fun RegisterSuccessStep(onNavigateToLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Ionicons.Outline.Checkmark,
            contentDescription = stringResource(Res.string.auth_register_success_icon_desc),
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.auth_register_success),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.auth_register_success_desc),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(stringResource(Res.string.auth_to_login))
        }
    }
}
