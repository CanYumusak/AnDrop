package de.canyumusak.androiddrop

import android.content.ComponentName
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Bundle
import android.service.chooser.ChooserTarget
import android.service.chooser.ChooserTargetService
import de.canyumusak.androiddrop.ui.root.DiscoveryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class AndropChooserTargetService : ChooserTargetService() {

    lateinit var discoveryViewModel: DiscoveryViewModel

    override fun onCreate() {
        super.onCreate()
        discoveryViewModel = DiscoveryViewModel(application)
        discoveryViewModel.discoverClients()
    }

    override fun onGetChooserTargets(targetActivityName: ComponentName?, matchedFilter: IntentFilter?): List<ChooserTarget> {
        val componentName = ComponentName(packageName, TransferServiceWrapperActivity::class.java.canonicalName)

        runBlocking { delay(1000) }
        discoveryViewModel.discovery?.dispose()

        return discoveryViewModel.clients.value?.map { client ->
            val extras = Bundle()
            extras.putString(TransferService.CLIENT_NAME, client.name)
            extras.putString(TransferService.IP_ADDRESS, client.v4Host?.canonicalHostName)
            extras.putInt(TransferService.PORT, client.port)

            ChooserTarget(
                    client.name,
                    Icon.createWithResource(this, R.drawable.icon_share_small_24dp),
                    1.0f,
                    componentName,
                    extras)
        } ?: listOf()
    }

}