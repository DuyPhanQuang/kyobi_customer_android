package com.kyobi.trend.config

import android.content.Context
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.featurecommon.monitor.network.NetworkMonitor
import com.kyobi.featurecommon.monitor.network.NetworkType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ReelConfigViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _config = MutableStateFlow(ReelConfig())
    val config: StateFlow<ReelConfig> = _config.asStateFlow()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    init {
        // run on background thread
        ioScope.launch {
            updateConfigBasedOnDeviceInfo()
        }
        startNetworkMonitoring()
    }

    private fun startNetworkMonitoring() {
        viewModelScope.launch {
            networkMonitor.isConnected.collectLatest { isConnected ->
                if (isConnected) {
                    // run on background thread
                    ioScope.launch {
                        updateConfigBasedOnDeviceInfo()
                    }
                }
            }
        }
    }

    private suspend fun updateConfigBasedOnDeviceInfo() {
        val storageInfo = getStorageInfo()
        // Logic cập nhật config dựa trên storageInfo
        Timber.tag("ReelConfigViewModel").d("Updated config based on device info: $storageInfo")
    }

    private suspend fun getStorageInfo(): String {
        return withContext(Dispatchers.IO) {
            val cacheDir = context.cacheDir // avoid DiskReadViolation
            val statFs = StatFs(cacheDir.path) // avoid DiskReadViolation
            val availableBytes = statFs.availableBytes
            "Available storage: $availableBytes bytes"
        }
    }

    private fun calculateConfig(ramMb: Long, storageMb: Long, cpuCores: Int, networkType: NetworkType): ReelConfig {
        val (bufferMinMs, bufferMaxMs, bufferPlaybackMs, bufferRebufferMs) = when (networkType) {
            NetworkType.WIFI -> listOf(2000, 6000, 2000, 2000)
            NetworkType.FIVE_G -> listOf(2000, 6000, 2000, 2000)
            NetworkType.FOUR_G -> listOf(1000, 3000, 1000, 1000)
            else -> listOf(1000, 3000, 1000, 1000)
        }

        return ReelConfig(
            bufferMinMs = bufferMinMs,
            bufferMaxMs = bufferMaxMs,
            bufferPlaybackMs = bufferPlaybackMs,
            bufferRebufferMs = bufferRebufferMs,
        )
    }

}