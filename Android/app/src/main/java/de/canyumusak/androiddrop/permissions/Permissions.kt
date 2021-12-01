package de.canyumusak.androiddrop.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

fun storagePermissionFlow(context: Context): StateFlow<Boolean> {
    val storagePermission = MutableStateFlow(false)
    val lifecycleOwner = ProcessLifecycleOwner.get()
    val lifecycleEventObserver = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_RESUME -> source.lifecycle.coroutineScope.launch {
                    storagePermission.emit(context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                }
                Lifecycle.Event.ON_DESTROY ->
                    lifecycleOwner.lifecycle.removeObserver(this)
                else -> {}
            }
        }
    }
    lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)

    return storagePermission.asStateFlow()
}
