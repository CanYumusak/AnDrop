package de.canyumusak.androiddrop

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle

class TransferServiceWrapperActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, TransferService::class.java)
        serviceIntent.putExtra(TransferService.CLIENT_NAME, intent.extras[TransferService.CLIENT_NAME] as String)
        serviceIntent.putExtra(TransferService.IP_ADDRESS, intent.extras[TransferService.IP_ADDRESS] as String)
        serviceIntent.putExtra(TransferService.PORT, intent.extras[TransferService.PORT] as Int )

        val dataUri = intent?.extras?.get(TransferService.DATA) as Uri
        serviceIntent.putExtra(TransferService.DATA, dataUri)
        grantUriPermission(packageName, dataUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}