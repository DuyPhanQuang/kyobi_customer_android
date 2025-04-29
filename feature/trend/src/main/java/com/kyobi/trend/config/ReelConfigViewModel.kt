package com.kyobi.trend.config

import android.app.ActivityManager
import android.content.Context
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.featurecommon.monitor.network.NetworkMonitor
import com.kyobi.featurecommon.monitor.network.NetworkType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ReelConfigViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _config = MutableStateFlow(ReelConfig())
    val config: StateFlow<ReelConfig> = _config.asStateFlow()

    init {
        // Cấu hình ban đầu dựa trên thông tin thiết bị
        updateConfigBasedOnDeviceInfo()
        // Theo dõi thay đổi mạng bằng NetworkMonitor
        startNetworkMonitoring()
    }

    private fun startNetworkMonitoring() {
        viewModelScope.launch {
            networkMonitor.networkType.collect { networkType ->
                Timber.tag("ReelConfigViewModel").d("Network type changed: $networkType")
                updateConfigBasedOnDeviceInfo()
            }
        }
    }

    private fun updateConfigBasedOnDeviceInfo() {
        val ramInfo = getRamInfo()
        val storageInfo = getStorageInfo()
        val cpuCores = Runtime.getRuntime().availableProcessors()
        // Lấy networkType từ NetworkMonitor
        val networkType = networkMonitor.networkType.value

        val newConfig = calculateConfig(ramInfo, storageInfo, cpuCores, networkType)
        _config.value = newConfig
        Timber.tag("ReelConfigViewModel").d("Updated config: $newConfig")
    }

    private fun getRamInfo(): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / (1024 * 1024) // MB
    }

    private fun getStorageInfo(): Long {
        val cacheDir = context.cacheDir
        val statFs = StatFs(cacheDir.path)
        return statFs.availableBytes / (1024 * 1024) // MB
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
            NetworkType.WIFI -> listOf(300, 900, 300, 300)
            NetworkType.FIVE_G -> listOf(400, 1200, 400, 400)
            NetworkType.FOUR_G -> listOf(500, 1500, 500, 500)
            else -> listOf(150, 450, 150, 150)
        }

        val cacheSizeMb = when {
            storageMb > 1000 -> 100
            storageMb > 500 -> 50
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