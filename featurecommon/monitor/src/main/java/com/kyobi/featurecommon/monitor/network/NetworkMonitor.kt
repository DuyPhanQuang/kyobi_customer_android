package com.kyobi.featurecommon.monitor.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.MainScope
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

enum class NetworkType {
    WIFI, FIVE_G, FOUR_G, NONE
}

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkUtils: NetworkUtils
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _isConnected = MutableStateFlow(networkUtils.isNetworkAvailable())
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _networkType = MutableStateFlow(NetworkType.NONE)
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()

    private var isNetworkCallbackRegistered = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
            updateNetworkType()
            Timber.tag("NetworkMonitor").d("Network available")
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
            _networkType.value = NetworkType.NONE
            Timber.tag("NetworkMonitor").d("Network lost")
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            _isConnected.value = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            updateNetworkType()
            Timber.tag("NetworkMonitor").d("Network capabilities changed: ${_networkType.value}")
        }
    }
    private fun updateNetworkType() {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        _networkType.value = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    if (capabilities.linkDownstreamBandwidthKbps >= 5000) NetworkType.FIVE_G
                    else NetworkType.FOUR_G
                } else NetworkType.NONE
            }
            else -> NetworkType.NONE
        }
    }

    init {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            isNetworkCallbackRegistered = true
            updateNetworkType()
            Timber.tag("NetworkMonitor").d("NetworkCallback registered successfully, initial state: isConnected=${_isConnected.value}, networkType=${_networkType.value}")
        } catch (e: Exception) {
            Timber.tag("NetworkMonitor").e(e, "Failed to register NetworkCallback")
        }
    }


    @Composable
    fun observeNetwork(onNetworkChange: (Boolean) -> Unit): State<Boolean> {
        val isConnectedState = remember { mutableStateOf(_isConnected.value) }
        val onNetworkChangeCallback = rememberUpdatedState(onNetworkChange)

        DisposableEffect(this) {
            val job = MainScope().launch {
                _isConnected.collect { isConnected ->
                    isConnectedState.value = isConnected
                    onNetworkChangeCallback.value(isConnected)
                }
            }

            onDispose {
                job.cancel()
            }
        }

        return isConnectedState
    }

    fun cleanup() {
        if (isNetworkCallbackRegistered) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
                isNetworkCallbackRegistered = false
                Timber.tag("NetworkMonitor").d("NetworkCallback unregistered successfully")
            } catch (e: Exception) {
                Timber.tag("NetworkMonitor").e(e, "Failed to unregister NetworkCallback")
            }
        }
    }
}