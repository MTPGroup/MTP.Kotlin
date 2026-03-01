package tech.hanasaki.momotalk_plus.features.auth.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.CheckmarkCircle
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Eye
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.EyeOff
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.LockClosed
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Mail
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.RefreshCircle
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.ShieldCheckmark
import momotalkplus.composeapp.generated.resources.Res
import momotalkplus.composeapp.generated.resources.auth_change_password
import momotalkplus.composeapp.generated.resources.auth_email_icon_desc
import momotalkplus.composeapp.generated.resources.auth_email_label
import momotalkplus.composeapp.generated.resources.auth_forgot_password_title
import momotalkplus.composeapp.generated.resources.auth_get_verification_code
import momotalkplus.composeapp.generated.resources.auth_new_password
import momotalkplus.composeapp.generated.resources.auth_otp_code_label
import momotalkplus.composeapp.generated.resources.auth_otp_icon_desc
import momotalkplus.composeapp.generated.resources.auth_password_hide
import momotalkplus.composeapp.generated.resources.auth_password_icon_desc
import momotalkplus.composeapp.generated.resources.auth_resend_in_seconds
import momotalkplus.composeapp.generated.resources.auth_password_reset_hint
import momotalkplus.composeapp.generated.resources.auth_password_reset_icon_desc
import momotalkplus.composeapp.generated.resources.auth_password_reset_success
import momotalkplus.composeapp.generated.resources.auth_password_reset_success_desc
import momotalkplus.composeapp.generated.resources.auth_password_reset_success_icon_desc
import momotalkplus.composeapp.generated.resources.auth_password_reset_title
import momotalkplus.composeapp.generated.resources.auth_password_show
import momotalkplus.composeapp.generated.resources.auth_to_login
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.app.ui.widgets.MTopBar
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.ForgotPasswordState
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.asString
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.resolve
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = koinViewModel(),
) {
    val uiState by viewModel.container.stateFlow.collectAsState()
    val pagerState = rememberPagerState { 2 }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is ForgotPasswordSideEffect.NavigateToSuccess -> pagerState.animateScrollToPage(1)
                is ForgotPasswordSideEffect.ShowToast -> {
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
                title = stringResource(Res.string.auth_forgot_password_title),
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
            when (page) {
                0 -> RequestEmailStep(uiState = uiState, onIntent = viewModel::onIntent)
                1 -> ResetSuccessStep(onNavigateToLogin = onNavigateBack)
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
        modifier = Modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Ionicons.Outline.RefreshCircle,
            contentDescription = stringResource(Res.string.auth_password_reset_icon_desc),
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(Res.string.auth_password_reset_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(Res.string.auth_password_reset_hint),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onIntent(ForgotPasswordIntent.EmailChanged(it)) },
            label = { Text(stringResource(Res.string.auth_email_label)) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.emailError != null,
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.Mail,
                    contentDescription = stringResource(Res.string.auth_email_icon_desc),
                    modifier = Modifier.size(24.dp),
                )
            },
            supportingText = {
                uiState.emailError?.let { Text(it.asString()) }
            },
            shape = MaterialTheme.shapes.small,
        )

        OutlinedTextField(
            value = uiState.newPassword,
            onValueChange = { onIntent(ForgotPasswordIntent.PasswordChanged(it)) },
            label = { Text(stringResource(Res.string.auth_new_password)) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.passwordError != null,
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.LockClosed,
                    contentDescription = stringResource(Res.string.auth_password_icon_desc),
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
            supportingText = {
                uiState.passwordError?.let { Text(it.asString()) }
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
                label = { Text(stringResource(Res.string.auth_otp_code_label)) },
                modifier = Modifier.weight(1f),
                isError = uiState.otpError != null,
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Ionicons.Outline.ShieldCheckmark,
                        contentDescription = stringResource(Res.string.auth_otp_icon_desc),
                        modifier = Modifier.size(24.dp),
                    )
                },
                supportingText = {
                    uiState.otpError?.let { Text(it.asString()) }
                },
                shape = MaterialTheme.shapes.small,
            )
            Button(
                onClick = { onIntent(ForgotPasswordIntent.SendVerificationCode) },
                enabled = !uiState.isRequestingCode && uiState.resendCooldownSeconds == 0,
            ) {
                val resendText = if (uiState.resendCooldownSeconds > 0) {
                    stringResource(Res.string.auth_resend_in_seconds, uiState.resendCooldownSeconds)
                } else {
                    stringResource(Res.string.auth_get_verification_code)
                }
                Text(resendText)
            }
        }

        Button(
            onClick = { onIntent(ForgotPasswordIntent.ResetPasswordClicked) },
            enabled = uiState.email.isNotBlank() && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Text(stringResource(Res.string.auth_change_password))
        }
    }
}

@Composable
private fun ResetSuccessStep(onNavigateToLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Ionicons.Outline.CheckmarkCircle,
            contentDescription = stringResource(Res.string.auth_password_reset_success_icon_desc),
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.auth_password_reset_success),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.auth_password_reset_success_desc),
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
