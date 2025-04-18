package com.kyobi.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun ProfileTab(
    navController: NavController,
    viewModel: ProfileTabViewModel = hiltViewModel()
) {
    val profileTabUiState = viewModel.profileTabUiState
    val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hồ sơ",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (authUiState.isLoggedIn) {
            authUiState.currentUser?.let { user ->
                Text(
                    text = "ID: ${user.id}",
                    style = MaterialTheme.typography.bodyMedium
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
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.submitLogout },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Đăng xuất")
                }
            }
        } else {
            Text(
                text = "Bạn chưa đăng nhập",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Đăng nhập")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("signup") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Đăng ký")
            }
        }
    }
}