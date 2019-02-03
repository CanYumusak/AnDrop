package de.canyumusak.androiddrop

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
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

class DiscoveryViewModel(application: Application) : AndroidViewModel(application) {

    val bonjour = RxBonjour.Builder()
            .platform(AndroidPlatform.create(application))
            .driver(JmDNSDriver.create())
            .create()

    val clients = MutableLiveData<List<BonjourService>>().also { it.value = listOf() }
    val error = MutableLiveData<String>()
    var discovery: Disposable? = null

    @SuppressLint("CheckResult")
    fun discoverClients() {
        Log.d("Bonjour", "starting discovery")

        discovery = bonjour.newDiscovery("_androp._tcp")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { event ->
                            when (event) {
                                is BonjourEvent.Added -> {
                                    Log.d("Bonjour", "added ${event.service.name}")
                                    val oldClients = clients.value?.toMutableList()

                                    oldClients?.removeIf { it.host == event.service.host }

                                    oldClients?.add(event.service)
                                    clients.value = oldClients
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
    }

    fun endDiscovery() {
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

}