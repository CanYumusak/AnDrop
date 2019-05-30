package de.canyumusak.androiddrop

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle

class TransferServiceWrapperActivity : Activity() {

    private val dataUris: Array<Uri>?
        get() {
            return if (intent.action == Intent.ACTION_SEND) {
                (intent.extras?.get(TransferService.DATA) as Uri?)?.let {
                    arrayOf(it)
                }
            } else if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
                (intent.extras?.get(TransferService.DATA) as List<Uri?>?)?.let {
                    it.filterNotNull().toTypedArray()
                }
            } else {
                null
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, TransferService::class.java)
        serviceIntent.putExtra(TransferService.CLIENT_NAME, intent.extras[TransferService.CLIENT_NAME] as String)
        serviceIntent.putExtra(TransferService.IP_ADDRESS, intent.extras[TransferService.IP_ADDRESS] as String?)
        serviceIntent.putExtra(TransferService.PORT, intent.extras[TransferService.PORT] as Int )
        serviceIntent.putExtra(TransferService.DATA, dataUris)
        dataUris?.forEach {
            grantUriPermission(packageName, it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}