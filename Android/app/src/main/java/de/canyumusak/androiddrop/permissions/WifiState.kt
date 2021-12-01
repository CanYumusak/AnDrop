package de.canyumusak.androiddrop.permissions

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import de.canyumusak.androiddrop.WifiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

fun CoroutineScope.wifiStateFlow(context: Context): StateFlow<WifiState> {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    val wifiState = MutableStateFlow<WifiState>(WifiState.Disabled)
    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            launch {
                wifiState.emit(WifiState.Enabled)
            }
        }

        override fun onLost(network: Network) {
            launch {
                wifiState.emit(WifiState.Disabled)
            }
        }

        override fun onUnavailable() {
            launch {
                wifiState.emit(WifiState.Disabled)
            }
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            launch {
                wifiState.emit(WifiState.Disabled)
            }
        }
    }

    connectivityManager.registerNetworkCallback(
        networkRequest,
        networkCallback,
    )

    coroutineContext.job.invokeOnCompletion {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    return wifiState.asStateFlow()
}