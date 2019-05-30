package de.canyumusak.androiddrop

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.net.NetworkInfo
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.canyumusak.androiddrop.sendables.ClassicFile
import de.canyumusak.androiddrop.sendables.SendableFile
import de.mannodermaus.rxbonjour.BonjourEvent
import de.mannodermaus.rxbonjour.BonjourService
import de.mannodermaus.rxbonjour.RxBonjour
import de.mannodermaus.rxbonjour.drivers.jmdns.JmDNSDriver
import de.mannodermaus.rxbonjour.platforms.android.AndroidPlatform
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import android.net.wifi.WifiManager


class DiscoveryViewModel(application: Application) : AndroidViewModel(application) {

    val bonjour = RxBonjour.Builder()
            .platform(AndroidPlatform.create(application))
            .driver(JmDNSDriver.create())
            .create()

    val clients = MutableLiveData<List<BonjourService>>().also { it.value = listOf() }
    val error = MutableLiveData<String>()
    var discovery: Disposable? = null
    val wifiState = MutableLiveData<WifiState>()

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateWifiState()
        }
    }

    private fun updateWifiState() {
        val wifiState = currentWifiState()
        this@DiscoveryViewModel.wifiState.value = wifiState

        when (wifiState) {
            WifiState.Disabled -> endDiscovery()
            is WifiState.Enabled -> {
                if (discovery?.isDisposed != false) {
                    discoverClients()
                }
            }
        }
    }

    val connectivityManager: ConnectivityManager
        get() = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        application.registerReceiver(broadcastReceiver, IntentFilter(CONNECTIVITY_ACTION))
        updateWifiState()
    }

    override fun onCleared() {
        getApplication<Application>().unregisterReceiver(broadcastReceiver)
        super.onCleared()
    }

    @SuppressLint("CheckResult")
    fun discoverClients() {
        Log.d("Bonjour", "starting discovery")

        val discovery = bonjour.newDiscovery("_androp._tcp")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { event ->
                            when (event) {
                                is BonjourEvent.Added -> {
                                    if (discovery?.isDisposed == false) {
                                        Log.d("Bonjour", "added ${event.service.name} with host ${event.service.host}")
                                        val oldClients = clients.value?.toMutableList()

                                        oldClients?.removeIf { it.host == event.service.host }

                                        oldClients?.add(event.service)
                                        clients.value = oldClients
                                    } else {
                                        Log.d("Bonjour", "ignoring ${event.service.name} since discovery was disposed")
                                    }
                                }

                                is BonjourEvent.Removed -> {
                                    Log.d("Bonjour", "removed ${event.service.name}")

                                    val oldClients = clients.value?.toMutableList()
                                    oldClients?.remove(event.service)
                                    clients.value = oldClients
                                }
                            }
                        },
                        { error.value = it.message }
                )

        this.discovery = discovery
    }

    fun endDiscovery() {
        clients.value = listOf()
        discovery?.dispose()
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

    private fun currentWifiState(): WifiState {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        val isWiFi: Boolean = activeNetwork?.type == ConnectivityManager.TYPE_WIFI

        return if (isWiFi && isConnected) {
            WifiState.Enabled
        } else {
            WifiState.Disabled
        }
    }
}

sealed class WifiState {
    object Disabled : WifiState()
    object Enabled : WifiState()
}