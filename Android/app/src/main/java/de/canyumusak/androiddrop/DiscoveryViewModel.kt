package de.canyumusak.androiddrop

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.*
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import de.canyumusak.androiddrop.sendables.ClassicFile
import de.canyumusak.androiddrop.sendables.SendableFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class DiscoveryViewModel(application: Application) : AndroidViewModel(application) {

    val clients = MutableLiveData<List<NsdServiceInfo>>().also { it.value = listOf() }
    val wifiState = MutableLiveData<WifiState>(WifiState.Disabled)
    val nsdManager: NsdManager
        get() = getApplication<Application>().getSystemService(Context.NSD_SERVICE) as NsdManager

    val wifiManager: WifiManager
        get() = getApplication<Application>().getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val discoveryListener = DiscoveryListener()

    val multicastLock = wifiManager.createMulticastLock("DiscoveryViewModel")

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

    fun endDiscovery(): List<NsdServiceInfo> {
        return if (discovering) {
            discovering = false
            val currentList = clients.value?.toList() ?: emptyList()
            clients.postValue(listOf())

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

    fun needsStoragePermission(dataUris: Array<Uri>?): Boolean {
        return dataUris?.any { dataUri ->
            val sendableFile = SendableFile.fromUri(dataUri, getApplication())
            val needsStorage = sendableFile is ClassicFile
            if (needsStorage) {
                !hasStoragePermssion()
            } else {
                false
            }
        } ?: false
    }

    private fun hasStoragePermssion(): Boolean {
        return ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
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

                    val oldClients = clients.value?.toMutableList()
                    if (oldClients?.any { it.host == serviceInfo.host } == true) {
                        oldClients.replaceAll {
                            if (it.host == serviceInfo.host) {
                                serviceInfo
                            } else {
                                it
                            }
                        }
                    } else {
                        oldClients?.add(serviceInfo)
                    }

                    clients.postValue(oldClients)
                }
            }

            nsdManager.resolveService(serviceInformation, listener)
        }

        override fun onServiceLost(serviceInformation: NsdServiceInfo?) {
            serviceInformation?.let { serviceInfo ->
                Log.d("Bonjour", "removed ${serviceInformation.serviceName}")

                val oldClients = clients.value?.toMutableList()

                oldClients?.removeAll {
                    it.host == serviceInfo.host
                }

                clients.postValue(oldClients)
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