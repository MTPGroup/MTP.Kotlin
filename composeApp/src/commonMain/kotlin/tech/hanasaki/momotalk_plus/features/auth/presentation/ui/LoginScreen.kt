package tech.hanasaki.momotalk_plus.features.auth.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.woowla.compose.icon.collections.ionicons.Ionicons
import com.woowla.compose.icon.collections.ionicons.ionicons.Outline
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Eye
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.EyeOff
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.LockClosed
import com.woowla.compose.icon.collections.ionicons.ionicons.outline.Mail
import momotalkplus.composeapp.generated.resources.Res
import momotalkplus.composeapp.generated.resources.app_description
import momotalkplus.composeapp.generated.resources.arona
import momotalkplus.composeapp.generated.resources.auth_email_icon_desc
import momotalkplus.composeapp.generated.resources.auth_email_label
import momotalkplus.composeapp.generated.resources.auth_cancel
import momotalkplus.composeapp.generated.resources.auth_confirm
import momotalkplus.composeapp.generated.resources.auth_email_verification_dialog_desc
import momotalkplus.composeapp.generated.resources.auth_email_verification_dialog_title
import momotalkplus.composeapp.generated.resources.auth_forgot_password
import momotalkplus.composeapp.generated.resources.auth_get_verification_code
import momotalkplus.composeapp.generated.resources.auth_login
import momotalkplus.composeapp.generated.resources.auth_otp_code_label
import momotalkplus.composeapp.generated.resources.auth_resend_in_seconds
import momotalkplus.composeapp.generated.resources.auth_password_hide
import momotalkplus.composeapp.generated.resources.auth_password_icon_desc
import momotalkplus.composeapp.generated.resources.auth_password_label
import momotalkplus.composeapp.generated.resources.auth_password_show
import momotalkplus.composeapp.generated.resources.auth_register
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginState
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.asString
import tech.hanasaki.momotalk_plus.features.auth.presentation.support.resolve
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    loginViewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by loginViewModel.container.stateFlow.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        loginViewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                LoginSideEffect.NavigateToForgotPassword -> onForgotPassword()
                LoginSideEffect.NavigateToRegister -> onRegister()
                is LoginSideEffect.ShowToast -> {
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                LoginContent(
                    uiState = uiState,
                    onIntent = loginViewModel::onIntent,
                )
            }

            if (uiState.showEmailVerificationDialog) {
                EmailVerificationDialog(
                    uiState = uiState,
                    onIntent = loginViewModel::onIntent,
                )
            }
        }
    }
}

@Composable
private fun EmailVerificationDialog(
    uiState: LoginState,
    onIntent: (LoginIntent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onIntent(LoginIntent.DismissEmailVerificationDialog) },
        title = { Text(stringResource(Res.string.auth_email_verification_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.auth_email_verification_dialog_desc))
                OutlinedTextField(
                    value = uiState.verificationEmail,
                    onValueChange = { onIntent(LoginIntent.VerificationEmailChanged(it)) },
                    label = { Text(stringResource(Res.string.auth_email_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = uiState.verificationEmailError != null,
                    supportingText = {
                        uiState.verificationEmailError?.let { Text(it.asString()) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = uiState.verificationCode,
                    onValueChange = { onIntent(LoginIntent.VerificationCodeChanged(it)) },
                    label = { Text(stringResource(Res.string.auth_otp_code_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.verificationCodeError != null,
                    supportingText = {
                        uiState.verificationCodeError?.let { Text(it.asString()) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(LoginIntent.DismissEmailVerificationDialog) }) {
                Text(stringResource(Res.string.auth_cancel))
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { onIntent(LoginIntent.ResendVerificationCodeClicked) },
                    enabled = !uiState.isSendingVerificationCode && !uiState.isVerifyingEmail && uiState.resendCooldownSeconds == 0,
                ) {
                    val resendText = if (uiState.resendCooldownSeconds > 0) {
                        stringResource(Res.string.auth_resend_in_seconds, uiState.resendCooldownSeconds)
                    } else {
                        stringResource(Res.string.auth_get_verification_code)
                    }
                    Text(resendText)
                }
                Button(
                    onClick = { onIntent(LoginIntent.VerifyEmailClicked) },
                    enabled = uiState.verificationCode.isNotBlank() && !uiState.isVerifyingEmail,
                ) {
                    if (uiState.isVerifyingEmail) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text(stringResource(Res.string.auth_confirm))
                    }
                }
            }
        },
    )
}

@Composable
fun LoginContent(
    uiState: LoginState,
    onIntent: (LoginIntent) -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.arona),
            contentDescription = stringResource(Res.string.app_description),
            modifier = Modifier.fillMaxWidth(0.6f),
            contentScale = ContentScale.FillWidth,
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onIntent(LoginIntent.EmailChanged(it)) },
            label = { Text(stringResource(Res.string.auth_email_label)) },
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.Mail,
                    contentDescription = stringResource(Res.string.auth_email_icon_desc),
                    modifier = Modifier.size(24.dp),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = uiState.emailError != null,
            supportingText = {
                uiState.emailError?.let { Text(it.asString()) }
            },
            shape = MaterialTheme.shapes.small,
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
            label = { Text(stringResource(Res.string.auth_password_label)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.LockClosed,
                    contentDescription = stringResource(Res.string.auth_password_icon_desc),
                    modifier = Modifier.size(24.dp),
                )
            },
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
            isError = uiState.passwordError != null || uiState.formError != null,
            supportingText = {
                uiState.passwordError?.let { Text(it.asString()) }
                    ?: uiState.formError?.let { Text(it.asString()) }
            },
            shape = MaterialTheme.shapes.small,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { onIntent(LoginIntent.ForgotPasswordClicked) },
                enabled = !uiState.isLoading,
            ) {
                Text(
                    stringResource(Res.string.auth_forgot_password),
                    textDecoration = TextDecoration.Underline,
                )
            }

            TextButton(
                onClick = { onIntent(LoginIntent.RegisterClicked) },
                enabled = !uiState.isLoading,
            ) {
                Text(
                    stringResource(Res.string.auth_register),
                    textDecoration = TextDecoration.Underline,
                )
            }
        }

        Button(
            onClick = { onIntent(LoginIntent.LoginClicked) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.password.isNotBlank(),
        ) {
            Text(stringResource(Res.string.auth_login))
        }
    }
}
