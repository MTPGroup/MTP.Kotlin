package tech.hanasaki.momotalk_plus.features.auth.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch
import momotalkplus.composeapp.generated.resources.Res
import momotalkplus.composeapp.generated.resources.app_description
import momotalkplus.composeapp.generated.resources.arona
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginIntent
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginSideEffect
import tech.hanasaki.momotalk_plus.features.auth.presentation.state.LoginState
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    loginViewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by loginViewModel.container.stateFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(loginViewModel.container) {
        loginViewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                LoginSideEffect.NavigateToForgotPassword ->
                    onForgotPassword()

                LoginSideEffect.NavigateToRegister ->
                    onRegister()

                is LoginSideEffect.ShowToast -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(message = effect.message, withDismissAction = true)
                    }
                }
            }
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                LoginContent(
                    uiState = uiState,
                    onIntent = loginViewModel::onIntent
                )
            }
        }
    }

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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.arona),
            contentDescription = stringResource(Res.string.app_description),
            modifier = Modifier.fillMaxWidth(0.6f),
            contentScale = ContentScale.FillWidth
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onIntent(LoginIntent.EmailChanged(it)) },
            label = { Text("邮箱") },
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.Mail,
                    contentDescription = "邮箱图标",
                    modifier = Modifier.size(24.dp)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = uiState.loginError != null,
            shape = MaterialTheme.shapes.small,
        )
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
            label = { Text("密码") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = {
                Icon(
                    imageVector = Ionicons.Outline.LockClosed,
                    contentDescription = "密码图标",
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                val image =
                    if (passwordVisible) Ionicons.Outline.EyeOff else Ionicons.Outline.Eye
                val description = if (passwordVisible) "隐藏密码" else "显示密码"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = description,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            isError = uiState.loginError != null,
            shape = MaterialTheme.shapes.small,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { onIntent(LoginIntent.ForgotPasswordClicked) },
                enabled = !uiState.isLoading
            ) {
                Text(
                    "忘记密码?",
                    textDecoration = TextDecoration.Underline
                )
            }

            TextButton(
                onClick = { onIntent(LoginIntent.RegisterClicked) },
                enabled = !uiState.isLoading
            ) {
                Text(
                    "注册",
                    textDecoration = TextDecoration.Underline
                )
            }
        }

        Button(
            onClick = { onIntent(LoginIntent.LoginClicked) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.password.isNotBlank()
        ) {
            Text("登录")
        }
    }
}
