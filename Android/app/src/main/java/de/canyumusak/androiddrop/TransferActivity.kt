package de.canyumusak.androiddrop

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import de.canyumusak.androiddrop.sendables.ExampleFile
import de.canyumusak.androiddrop.theme.AnDropTheme
import de.canyumusak.androiddrop.transfer.TransferEvents
import de.canyumusak.androiddrop.ui.ScanScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TransferActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AnDropTheme {
                val context = LocalContext.current
                val discoveryViewModel: DiscoveryViewModel = viewModel()
                LaunchedEffect(true) {
                    discoveryViewModel.dataUrisRequested(dataUris())
                    TransferEvents.trackTransferRequest(dataUris(), context)
                }
                ScanScreen(
                    discoveryViewModel = discoveryViewModel,
                    clientSelected = {
                        clientSelected(discoveryViewModel, it)
                    },
                    permissionRequested = {
                        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
                    }
                )
            }
        }
    }

    private fun clientSelected(viewModel: DiscoveryViewModel, client: AnDropClient) {
        val dataUris = dataUris()
        viewModel.endDiscovery()

        lifecycleScope.launch {

            val ipaddress = client.host
            val intent = Intent(this@TransferActivity, TransferService::class.java)
            intent.putExtra(TransferService.CLIENT_NAME, client.name)
            intent.putExtra(TransferService.IP_ADDRESS, ipaddress)
            intent.putExtra(TransferService.PORT, client.port)
            intent.putExtra(TransferService.DATA, dataUris)
            dataUris?.forEach {
                grantUriPermission(packageName, it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            delay(200)
            supportFinishAfterTransition()
        }
    }

    private fun dataUris(): Array<Uri>? {
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                (intent.extras?.get(TransferService.DATA) as Uri?)?.let {
                    arrayOf(it)
                }
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                (intent.extras?.get(TransferService.DATA) as List<Uri?>?)?.let {
                    it.filterNotNull().toTypedArray()
                }
            }

            INTENT_SEND_EXAMPLE -> {
                arrayOf(ExampleFile.URI)
            }

            else -> {
                null
            }
        }
    }

    companion object {
        const val INTENT_SEND_EXAMPLE = "SEND_EXAMPLE_FILE"
    }
}
