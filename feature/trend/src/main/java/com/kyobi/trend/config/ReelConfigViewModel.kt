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
        // Trì hoãn gọi updateConfigBasedOnDeviceInfo, chạy trên background thread
        ioScope.launch {
            updateConfigBasedOnDeviceInfo()
        }
        startNetworkMonitoring()
    }

    private fun startNetworkMonitoring() {
        viewModelScope.launch {
            networkMonitor.isConnected.collectLatest { isConnected ->
                if (isConnected) {
                    // Chạy updateConfigBasedOnDeviceInfo trên background thread
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
            val cacheDir = context.cacheDir // Chạy trên Dispatchers.IO, không gây DiskReadViolation
            val statFs = StatFs(cacheDir.path) // Chạy trên Dispatchers.IO, không gây DiskReadViolation
            val availableBytes = statFs.availableBytes
            "Available storage: $availableBytes bytes"
        }
    }

    private fun calculateConfig(ramMb: Long, storageMb: Long, cpuCores: Int, networkType: NetworkType): ReelConfig {
        val downloadSizeMb = when (networkType) {
            NetworkType.WIFI -> 8
            NetworkType.FIVE_G -> 6
            NetworkType.FOUR_G -> 4
            else -> 2
        }

        val positionsToKeepRange = when {
            ramMb > 2000 && storageMb > 500 -> 4
            ramMb > 1000 && storageMb > 200 -> 3
            else -> 2
        }

        val (bufferMinMs, bufferMaxMs, bufferPlaybackMs, bufferRebufferMs) = when (networkType) {
            NetworkType.WIFI -> listOf(2000, 6000, 2000, 2000)
            NetworkType.FIVE_G -> listOf(2000, 6000, 2000, 2000)
            NetworkType.FOUR_G -> listOf(1000, 3000, 1000, 1000)
            else -> listOf(1000, 3000, 1000, 1000)
        }

        val cacheSizeMb = when {
            storageMb > 1000 -> 200
            storageMb > 500 -> 100
            else -> 30
        }

        return ReelConfig(
            downloadSizeMb = downloadSizeMb,
            positionsToKeepRange = positionsToKeepRange,
            bufferMinMs = bufferMinMs,
            bufferMaxMs = bufferMaxMs,
            bufferPlaybackMs = bufferPlaybackMs,
            bufferRebufferMs = bufferRebufferMs,
            cacheSizeMb = cacheSizeMb
        )
    }

}