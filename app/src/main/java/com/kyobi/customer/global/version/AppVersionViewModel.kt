package com.kyobi.customer.global.version

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.core.coroutines.handleErrors
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.usecase.AppVersionUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import com.kyobi.core.coroutines.withLoading
import kotlinx.coroutines.delay
import androidx.core.content.edit
import com.kyobi.customer.constants.KeyConstant
import com.kyobi.customer.BuildConfig
import org.semver4j.Semver
import org.semver4j.SemverException
import timber.log.Timber

@HiltViewModel
class AppVersionViewModel @Inject constructor(
    private val appVersionUseCase: AppVersionUsecase,
    private val sharedPreferences: SharedPreferences
): ViewModel() {
    private val _uiState = MutableStateFlow(AppVersionUiState())
    val uiState: StateFlow<AppVersionUiState> = _uiState

    init {
        checkAppVersion()
    }

    fun onAppForeground() {
        Timber.tag("AppVersionViewModel").d("onAppForeground called")
        checkAppVersion()
    }

    private fun checkAppVersion() {
        viewModelScope.launchOnIO {
            while (true) {
                appVersionUseCase.getAppVersion()
                    .withLoading {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }
                    .handleErrors { throwable ->
                        _uiState.value = AppVersionUiState(
                            isMaintenance = true,
                            maintenanceMessage = throwable.message ?: "Server is currently unavailable",
                            isLoading = false,
                            error = throwable.message
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is DomainNetworkResult.Success -> {
                                val data = result.data
                                val currentVersion = BuildConfig.VERSION_NAME
                                val latestVersion = data.maximumVersion
                                val seenVersion = sharedPreferences.getString(KeyConstant.SharePrefs.seenVersion, null)
                                // Case 0: maintenance = true
                                if (data.isMaintenance) {
                                    _uiState.value = AppVersionUiState(
                                        isMaintenance = true,
                                        maintenanceMessage = data.maintenanceMessage,
                                    )
                                    return@collect
                                }
                                // Case 1: forceUpdate = true
                                if (data.forceUpdate) {
                                    _uiState.value = AppVersionUiState(
                                        showForceUpdate = true,
                                        forceUpdateMessage = data.forceUpdateMessage
                                    )
                                    return@collect
                                } else {
                                    // Case 2: forceUpdate = false
                                    if (isVersionLower(currentVersion, data.minimumVersion)) {
                                        // Case 2.1: currentVersion < minimumVersion
                                        _uiState.value = AppVersionUiState(
                                            showForceUpdate = true,
                                            forceUpdateMessage = "Your app version is too old. Please update to continue."
                                        )
                                    } else if (isVersionLower(currentVersion, latestVersion) && seenVersion != latestVersion) {
                                        // Case 2.2: Có version mới trên store, chưa show popup
                                        _uiState.value = AppVersionUiState(
                                            showUpdateNotification = true,
                                            updateNotificationMessage = "A new version is available. Update now for the best experience."
                                        )
                                    } else {
                                        // Version hợp lệ, không cần hiển thị popup
                                        _uiState.value = AppVersionUiState()
                                    }
                                    return@collect
                                }
                            }
                            is DomainNetworkResult.Error -> {
                                _uiState.value = AppVersionUiState(
                                    isMaintenance = true,
                                    maintenanceMessage = result.exception.message,
                                    isLoading = false,
                                    error = result.exception.message
                                )
                            }
                            is DomainNetworkResult.Loading -> {
                                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                            }
                        }
                    }
                delay(60_000) // Poll mỗi 60 giây để check version mới
            }
        }
    }

    // Đóng popup thông báo và lưu version đã thấy
    fun onUpdateNotificationDismissed() {
        val latestVersion = _uiState.value.maximumVersion
        sharedPreferences.edit {
            putString(KeyConstant.SharePrefs.seenVersion, latestVersion)
        }
        _uiState.value = _uiState.value.copy(showUpdateNotification = false)
    }

    private fun isVersionLower(version1: String, version2: String): Boolean {
        if (version1.isBlank() || version2.isBlank()) return false
        return try {
            Semver(version1).isLowerThan(version2)
        } catch (e: SemverException) {
            false
        }
    }
}