package tech.hanasaki.momotalk_plus.features.login.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import kotlinx.coroutines.launch
import momotalkplus.composeapp.generated.resources.Res
import momotalkplus.composeapp.generated.resources.app_description
import momotalkplus.composeapp.generated.resources.text_logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.features.login.presentation.state.LoginIntent
import tech.hanasaki.momotalk_plus.features.login.presentation.state.LoginSideEffect
import tech.hanasaki.momotalk_plus.features.login.presentation.state.LoginState
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    loginViewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = loginViewModel.sideEffect) {
        loginViewModel.sideEffect.collect { effect ->
            when (effect) {
                is LoginSideEffect.NavigateToHome ->
                    onLoginSuccess()

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
                    onIntent = loginViewModel::processIntent
                )
            }
        }
    }

}

@Composable
fun LoginContent(
    uiState: LoginState,
    onIntent: (LoginIntent) -> Unit
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
            painter = painterResource(Res.drawable.text_logo),
            contentDescription = stringResource(Res.string.app_description),
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )

        OutlinedTextField(
            value = uiState.username,
            onValueChange = { onIntent(LoginIntent.UsernameChanged(it)) },
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = uiState.loginError != null
        )
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
            label = { Text("密码") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                val description = if (passwordVisible) "隐藏密码" else "显示密码"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            },
            isError = uiState.loginError != null
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
            enabled = !uiState.isLoading && uiState.username.isNotBlank() && uiState.password.isNotBlank()
        ) {
            Text("登录")
        }
    }
}
