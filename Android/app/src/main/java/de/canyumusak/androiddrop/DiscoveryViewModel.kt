package de.canyumusak.androiddrop

import android.app.Application
import android.content.Context
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.canyumusak.androiddrop.permissions.storagePermissionFlow
import de.canyumusak.androiddrop.permissions.wifiStateFlow
import de.canyumusak.androiddrop.sendables.ClassicFile
import de.canyumusak.androiddrop.sendables.SendableFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DiscoveryViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uris = MutableStateFlow<Array<Uri>?>(null)
    private val _clients = MutableStateFlow<List<AnDropClient>>(listOf())

    private val storagePermission = storagePermissionFlow(getApplication<Application>())

    val needsStoragePermission: StateFlow<Boolean> = combine(_uris, storagePermission) { uris, hasStoragePermssion ->
        needsStoragePermission(uris, hasStoragePermssion)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val clients: StateFlow<List<AnDropClient>> = _clients.asStateFlow()
    val fileTypeUnsupported = _uris.map { it == null }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val wifiState = viewModelScope.wifiStateFlow(getApplication<Application>())

    val nsdManager: NsdManager get() = getApplication<Application>().getSystemService(Context.NSD_SERVICE) as NsdManager
    private val wifiManager: WifiManager get() = getApplication<Application>().getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val discoveryListener = DiscoveryListener()

    private val multicastLock = wifiManager.createMulticastLock("DiscoveryViewModel")

    private var discovering = false

    init {
        Log.d("DiscoveryViewModel", "Registered Network Request")

        viewModelScope.launch {
            wifiState.collectLatest {
                when (it) {
                    WifiState.Disabled -> if (discovering) performEndDiscovery()
                    WifiState.Enabled -> if (discovering) performDiscoverClients()
                }
            }
        }
    }

    override fun onCleared() {
        Log.d("DiscoveryViewModel", "Clearing discovery")
        endDiscovery()
        super.onCleared()
    }

    suspend fun dataUrisRequested(uris: Array<Uri>?) {
        discoverClients()
        _uris.emit(uris)
    }

    fun discoverClients() {
        if (!discovering) {
            discovering = true
            performDiscoverClients()
        }
    }

    fun endDiscovery(): List<AnDropClient> {
        discovering = false
        return performEndDiscovery()
    }

    private fun performEndDiscovery(): List<AnDropClient> {
        return if (discovering) {
            val currentList = clients.value.toList()
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

    private fun performDiscoverClients() {
        Log.d("Bonjour", "starting discovery")
        try {
            multicastLock.acquire()
            nsdManager.discoverServices("_androp._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (exception: RuntimeException) {
            // fail silently if we can't acquire a multicast lock
        }
    }

    private fun needsStoragePermission(dataUris: Array<Uri>?, hasStoragePermssion: Boolean): Boolean {
        return dataUris?.any { dataUri ->
            val sendableFile = SendableFile.fromUri(dataUri, getApplication())
            val isClassic = sendableFile is ClassicFile
            val isMyOwn = dataUri.host == getApplication<Application>().packageName
            if (isClassic && !isMyOwn) {
                !hasStoragePermssion
            } else {
                false
            }
        } ?: false
    }

    private inner class DiscoveryListener : NsdManager.DiscoveryListener {

        override fun onServiceFound(serviceInformation: NsdServiceInfo?) {
            Log.w("DiscoveryViewModel", "Found $serviceInformation")
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
            serviceInformation?.let {
                Log.d("Bonjour", "onServiceLost(): $serviceInformation")
                _clients.value = clients.value.filterNot { it.name == serviceInformation.serviceName }
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