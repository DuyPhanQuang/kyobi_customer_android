package com.kyobi.authentication.login

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel()
) {
    val loginUiState = viewModel.loginUiState
    val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Đăng nhập",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        authUiState.currentUser?.let { user ->
            Text(
                text = "Người dùng hiện tại: ${user.id}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            user.info?.email?.let {
                Text(
                    text = "Email: $it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            user.info?.nickname?.let {
                Text(
                    text = "Tên: $it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = loginUiState.email,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            isError = loginUiState.error != null
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = loginUiState.password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            isError = loginUiState.error != null
        )

        loginUiState.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.submitLogin() },
            enabled = !loginUiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loginUiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Đăng nhập")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Chưa có tài khoản? Đăng ký")
        }
    }
}