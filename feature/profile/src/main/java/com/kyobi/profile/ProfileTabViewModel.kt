package com.kyobi.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kyobi.domain.provider.auth.AuthStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileTabViewModel @Inject constructor(
    private val authStateProvider: AuthStateProvider
) : ViewModel() {

    val authUiState = authStateProvider.authUiState

    val submitLogout = authStateProvider.logout()

    var profileTabUiState by mutableStateOf(ProfileTabUiState())
        private set
}