package com.kyobi.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileTabViewModel @Inject constructor() : ViewModel() {
    var profileTabUiState by mutableStateOf(ProfileTabUiState())
        private set

    fun submitLogout(
        onLogout: () -> Unit
    ) {
        onLogout()
    }
}