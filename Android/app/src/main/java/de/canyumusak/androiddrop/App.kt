package de.canyumusak.androiddrop

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import de.canyumusak.androiddrop.analytics.Analytics
import kotlinx.coroutines.launch

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get().lifecycleScope.launch {
            Analytics.setup(this@App)
        }
    }
}
