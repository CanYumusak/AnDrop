package de.canyumusak.androiddrop

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.*
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import de.canyumusak.androiddrop.permissions.Permissions
import de.canyumusak.androiddrop.sendables.ClassicFile
import de.canyumusak.androiddrop.sendables.SendableFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DiscoveryViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uris = MutableStateFlow<Array<Uri>?>(null)

    val needsStoragePermission: StateFlow<Boolean> = combine(_uris, Permissions.storagePermission) { uris, hasStoragePermssion ->
        needsStoragePermission(uris, hasStoragePermssion)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )

    private val _clients = MutableStateFlow<List<AnDropClient>>(listOf())
    val clients: StateFlow<List<AnDropClient>> = _clients.asStateFlow()

    val wifiState = MutableLiveData<WifiState>(WifiState.Disabled)
    val nsdManager: NsdManager
        get() = getApplication<Application>().getSystemService(Context.NSD_SERVICE) as NsdManager

    private val wifiManager: WifiManager
        get() = getApplication<Application>().getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val discoveryListener = DiscoveryListener()

    private val multicastLock = wifiManager.createMulticastLock("DiscoveryViewModel")

    private var discovering = false

    private val connectivityManager: ConnectivityManager
        get() = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (wifiState.value !is WifiState.Enabled) {
                wifiState.postValue(WifiState.Enabled)
                discoverClients()
            }
        }

        override fun onLost(network: Network) {
            wifiState.postValue(WifiState.Disabled)
            endDiscovery()
        }

        override fun onUnavailable() {
            wifiState.postValue(WifiState.Disabled)
            endDiscovery()
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            wifiState.postValue(WifiState.Disabled)
            endDiscovery()
        }
    }

    init {
        val builder = NetworkRequest.Builder()
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

        val networkRequest = builder.build()
        Log.d("DiscoveryViewModel", "Registered Network Request")

        connectivityManager.registerNetworkCallback(
            networkRequest,
            networkCallback
        )
    }

    override fun onCleared() {
        Log.d("DiscoveryViewModel", "Clearing discovery")

        connectivityManager.unregisterNetworkCallback(networkCallback)
        endDiscovery()
        super.onCleared()
    }

    @SuppressLint("CheckResult")
    fun discoverClients() {
        if (!discovering) {
            discovering = true
            Log.d("Bonjour", "starting discovery")
            try {
                multicastLock.acquire();
            } catch (exception: java.lang.RuntimeException) {
                // fail silently if we can't acquire a multicast lock
            }

            nsdManager.discoverServices("_androp._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        }
    }

    fun endDiscovery(): List<AnDropClient> {
        return if (discovering) {
            discovering = false
            val currentList = clients.value?.toList() ?: emptyList()
            _clients.value = listOf()

            try {
                if (multicastLock.isHeld) {
                    multicastLock.release()
                }

                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (exception: RuntimeException) {
                // fail silently if those fail
            }

            currentList
        } else {
            emptyList()
        }
    }

    suspend fun dataUrisRequested(uris: Array<Uri>?) {
        _uris.emit(uris)
    }

    private fun needsStoragePermission(dataUris: Array<Uri>?, hasStoragePermssion: Boolean): Boolean {
        return dataUris?.any { dataUri ->
            val sendableFile = SendableFile.fromUri(dataUri, getApplication())
            val needsStorage = sendableFile is ClassicFile
            if (needsStorage) {
                !hasStoragePermssion
            } else {
                false
            }
        } ?: false
    }

    private inner class DiscoveryListener : NsdManager.DiscoveryListener {

        override fun onServiceFound(serviceInformation: NsdServiceInfo?) {
            var retries = 20
            val listener = object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    val resolveListener = this

                    viewModelScope.launch {
                        Log.w("DiscoveryViewModel", "onResolveFailed(): Resolve failed (serviceInfo = '$serviceInfo'; errorCode = '$errorCode'; retries = '$retries')")
                        delay((Math.random() * 20).toLong())
                        retries -= 1
                        if (retries > 0) {
                            nsdManager.resolveService(serviceInformation, resolveListener)
                        }
                    }
                }

                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                    Log.d("DiscoveryViewModel", "onServiceResolved(): Resolve successful (serviceInfo = '$serviceInfo')")

                    val oldClients = clients.value.toMutableList()
                    if (oldClients.any { it.host == serviceInfo.host.canonicalHostName }) {
                        oldClients.replaceAll {
                            if (it.host == serviceInfo.host.canonicalHostName) {
                                AnDropClient.fromServiceInfo(serviceInfo)
                            } else {
                                it
                            }
                        }
                    } else {
                        oldClients.add(AnDropClient.fromServiceInfo(serviceInfo))
                    }

                    _clients.value = oldClients
                }
            }

            nsdManager.resolveService(serviceInformation, listener)
        }

        override fun onServiceLost(serviceInformation: NsdServiceInfo?) {
            serviceInformation?.let { serviceInfo ->
                Log.d("Bonjour", "removed ${serviceInformation.serviceName}")

                val oldClients = clients.value.toMutableList()

                oldClients.removeAll {
                    it.host == serviceInfo.host.canonicalHostName
                }

                _clients.value = oldClients
            }
        }

        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.w("DiscoveryViewModel", "onStartDiscoveryFailed(): Discovery start failed (serviceType = '$serviceType'; errorCode = '$errorCode')")
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.w("DiscoveryViewModel", "onStopDiscoveryFailed(): Discovery stop failed (serviceType = '$serviceType'; errorCode = '$errorCode')")
        }

        override fun onDiscoveryStarted(serviceType: String?) {
            Log.w("DiscoveryViewModel", "onDiscoveryStarted(): Discovery started (serviceType = '$serviceType')")
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            Log.w("DiscoveryViewModel", "onDiscoveryStopped(): Discovery stopped (serviceType = '$serviceType')")
        }
    }
}

sealed class WifiState {
    object Disabled : WifiState()
    object Enabled : WifiState()
}

data class AnDropClient(
    val name: String,
    val host: String = "localhost",
    val port: Int = 8008,
) {
    companion object {
        fun fromServiceInfo(serviceInfo: NsdServiceInfo): AnDropClient {
            return AnDropClient(
                serviceInfo.serviceName,
                serviceInfo.host.canonicalHostName,
                serviceInfo.port,
            )
        }
    }
}