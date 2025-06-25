package tech.hanasaki.momotalk_plus.features.login.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import momotalkplus.composeapp.generated.resources.Res
import momotalkplus.composeapp.generated.resources.text_logo
import momotalkplus.composeapp.generated.resources.app_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import tech.hanasaki.momotalk_plus.features.login.presentation.state.LoginIntent
import tech.hanasaki.momotalk_plus.features.login.presentation.state.LoginSideEffect
import tech.hanasaki.momotalk_plus.features.login.presentation.state.LoginState
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    loginViewModel: LoginViewModel  = koinViewModel()
) {
    val uiState by loginViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = loginViewModel.sideEffect) {
        loginViewModel.sideEffect.collect { effect ->
            when (effect) {
                LoginSideEffect.NavigateToHome -> {
                    onLoginSuccess()
                }

                is LoginSideEffect.ShowToast -> {
                    // TODO: 实现 Toast 显示逻辑
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
            value = uiState.email,
            onValueChange = { onIntent(LoginIntent.EmailChanged(it)) },
            label = { Text("邮箱") },
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
            enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.password.isNotBlank()
        ) {
            Text("登录")
        }
    }
    // TODO: 处理登录错误信息
    /*if (uiState.loginError != null) {
        Text(
            text = uiState.loginError,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }*/

}
