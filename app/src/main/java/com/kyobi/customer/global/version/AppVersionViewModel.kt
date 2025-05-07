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
import com.kyobi.featurecommon.auth.session.SessionEventBus
import org.semver4j.Semver
import org.semver4j.SemverException
import timber.log.Timber

@HiltViewModel
class AppVersionViewModel @Inject constructor(
    private val appVersionUseCase: AppVersionUsecase,
    private val sharedPreferences: SharedPreferences,
    val sessionEventBus: SessionEventBus
): ViewModel() {
    private val tag = "AppVersionViewModel"
    private val _uiState = MutableStateFlow(AppVersionUiState())
    val uiState: StateFlow<AppVersionUiState> = _uiState

    init {
        checkAppVersion()
    }

    fun onAppForeground() {
        Timber.tag(tag).d("onAppForeground called")
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
                        Timber.tag(tag).e("Failed to get app version error: ${throwable.message}")
                    }
                    .collect { result ->
                        when (result) {
                            is DomainNetworkResult.Success -> {
                                val data = result.data
                                val minimumVersion = data.minimumVersion
                                val latestVersion = data.maximumVersion
                                val seenVersion = sharedPreferences.getString(KeyConstant.SharePrefs.seenVersion, null)
                                // Case 0: maintenance = true
                                if (data.isMaintenance) {
                                    _uiState.value = _uiState.value.copy(
                                        isMaintenance = true,
                                        maintenanceMessage = data.maintenanceMessage,
                                        showForceUpdate = false,
                                        showUpdateNotification = false,
                                        maximumVersion = latestVersion,
                                        isLoading = false,
                                        error = null
                                    )
                                    return@collect
                                }
                                // Case 1: forceUpdate = true
                                if (data.forceUpdate) {
                                    _uiState.value = _uiState.value.copy(
                                        isMaintenance = false,
                                        showForceUpdate = true,
                                        forceUpdateMessage = data.forceUpdateMessage,
                                        showUpdateNotification = false,
                                        maximumVersion = latestVersion,
                                        isLoading = false,
                                        error = null
                                    )
                                    return@collect
                                } else {
                                    Timber.tag(tag).d("latestVersion: $latestVersion, seenVersion: $seenVersion")
                                    // Case 2: forceUpdate = false
                                    if (isVersionLower(minimumVersion)) {
                                        // Case 2.1: currentVersion < minimumVersion
                                        _uiState.value = _uiState.value.copy(
                                            isMaintenance = false,
                                            showForceUpdate = true,
                                            forceUpdateMessage = "Your app version is too old. Please update to continue.",
                                            showUpdateNotification = false,
                                            maximumVersion = latestVersion,
                                            isLoading = false,
                                            error = null
                                        )
                                    } else if (isVersionLower(latestVersion) && seenVersion != latestVersion) {
                                        // Case 2.2: Có version mới trên store, chưa show popup
                                        _uiState.value = _uiState.value.copy(
                                            isMaintenance = false,
                                            showForceUpdate = false,
                                            showUpdateNotification = true,
                                            updateNotificationMessage = "A new version is available. Update now for the best experience.",
                                            maximumVersion = latestVersion,
                                            isLoading = false,
                                            error = null
                                        )
                                    } else {
                                        // Version hợp lệ, không cần hiển thị popup
                                        _uiState.value = _uiState.value.copy(
                                            isMaintenance = false,
                                            showForceUpdate = false,
                                            showUpdateNotification = false,
                                            maximumVersion = latestVersion,
                                            isLoading = false,
                                            error = null
                                        )
                                    }
                                    return@collect
                                }
                            }
                            is DomainNetworkResult.Error -> {
                                val errorMessage = when (result) {
                                    is DomainNetworkResult.Error.KyobiApi -> result.exception.message
                                    is DomainNetworkResult.Error.Generic -> result.throwable.message
                                    is DomainNetworkResult.Error.ShopifyApi -> null
                                } ?: "Something went wrong"
                                Timber.tag(tag).e("Failed to get app version")
                                _uiState.value = _uiState.value.copy(isLoading = true, error = errorMessage)
                            }
                            is DomainNetworkResult.Loading -> {
                                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                            }
                        }
                    }
                delay(10 * 60_000) // Poll mỗi 10 phút để check version mới
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

    private fun isVersionLower(compareVersion: String): Boolean {
        val currentVersion = BuildConfig.VERSION_NAME
        if (currentVersion.isBlank() || compareVersion.isBlank()) return false
        return try {
            Semver(currentVersion).isLowerThan(compareVersion)
        } catch (e: SemverException) {
            false
        }
    }
}