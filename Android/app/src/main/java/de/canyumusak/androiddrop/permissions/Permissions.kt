package de.canyumusak.androiddrop.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.startup.Initializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object Permissions {

    private val _storagePermission = MutableStateFlow(false)
    val storagePermission = _storagePermission.asStateFlow()

    fun initialize(context: Context) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { source, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                source.lifecycle.coroutineScope.launch {
                    updateFlow(context)
                }
            }
        })
    }

    private suspend fun updateFlow(context: Context) {
        _storagePermission.emit(context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }
}

class PermissionsInitializer : Initializer<Permissions> {
    override fun create(context: Context): Permissions {
        Permissions.initialize(context)
        return Permissions
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }
}