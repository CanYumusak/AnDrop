package de.canyumusak.androiddrop

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import de.canyumusak.androiddrop.connection.FileConnection
import de.canyumusak.androiddrop.connection.State
import de.canyumusak.androiddrop.inappreview.InAppReviewManager
import de.canyumusak.androiddrop.sendables.SendableFile
import de.canyumusak.androiddrop.transfer.TransferEvents

class TransferService : Service() {

    var currentFileConnection: FileConnection? = null
    val transferServiceBroadcastReceiver = TransferServiceBroadcastReceiver(this)

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                transferServiceBroadcastReceiver,
                IntentFilter(CANCEL_REQUEST_ACTION),
                RECEIVER_NOT_EXPORTED,
            )
        } else {
            registerReceiver(
                transferServiceBroadcastReceiver,
                IntentFilter(CANCEL_REQUEST_ACTION),
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(transferServiceBroadcastReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val onStartCommand = super.onStartCommand(intent, flags, startId)

        val createNotification = createStartupNotification(this)
        startForeground(SERVICE_ID, createNotification)

        intent?.let {
            startConnectionHandling(FileTransferCommand(it))
        }
        return onStartCommand
    }

    private fun startConnectionHandling(transferCommand: FileTransferCommand) {
        currentFileConnection = FileConnection(this, transferCommand) {
            when (it) {
                is State.Transferring -> {
                    startForeground(SERVICE_ID, createTransferNotification(this, it.progress))
                }

                is State.Finished -> {
                    InAppReviewManager.fileTransferSucceeded(this)
                    stopForeground(true)
                    stopSelf()
                    notificationManager.cancel(RESPONSE_NOTIFICATION_ID)
                    notificationManager.notify(
                        RESPONSE_NOTIFICATION_ID,
                        createSuccessNotification(this, transferCommand)
                    )
                }

                is State.Disconnected -> {
                    stopForeground(true)
                    stopSelf()
                    notificationManager.cancel(RESPONSE_NOTIFICATION_ID)
                    notificationManager.notify(
                        RESPONSE_NOTIFICATION_ID,
                        createErrorNotification(this, transferCommand)
                    )
                }

                else -> Log.i("TransferService", "Change state to $it")
            }

            if (it !is State.Transferring) {
                TransferEvents.trackFileConnectionEvent(it)
            }
        }

        currentFileConnection?.open()
    }

    fun cancelCurrentTransfer() {
        stopForeground(true)
        currentFileConnection?.cancel()
    }


    companion object {
        const val SERVICE_ID = 53453345
        const val RESPONSE_NOTIFICATION_ID = 286312135
        const val HIGH_PRIO_CHANNEL_ID = "high_prio_notification_channel_id"
        const val LOW_PRIO_CHANNEL_ID = "low_prio_notification_channel_id"
        const val CANCEL_REQUEST_ACTION = "de.canyumusak.androiddrop.cancel_request_action"

        const val CLIENT_NAME = "client_name"
        const val IP_ADDRESS = "ip_address"
        const val PORT = "port"
        const val DATA = "android.intent.extra.STREAM"

        private fun createHighPriorityChannel(context: Context) {
            val rhmiNotificationChannel = NotificationChannel(
                HIGH_PRIO_CHANNEL_ID,
                "Success/Fail Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            context.notificationManager.createNotificationChannel(rhmiNotificationChannel)
        }

        private fun createLowPriorityChannel(context: Context) {
            val notificationChannel = NotificationChannel(
                LOW_PRIO_CHANNEL_ID,
                "Transfer Notification",
                NotificationManager.IMPORTANCE_LOW
            )
            context.notificationManager.createNotificationChannel(notificationChannel)
        }

        fun createStartupNotification(context: TransferService): Notification {
            createLowPriorityChannel(context)

            val intent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(CANCEL_REQUEST_ACTION),
                PendingIntent.FLAG_IMMUTABLE
            )
            val action = NotificationCompat.Action(0, "Cancel", intent)

            return NotificationCompat.Builder(context, LOW_PRIO_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setSmallIcon(R.drawable.ic_stat_share)
                .setColor(context.getColor(R.color.colorPrimaryDark))
                .addAction(action)
                .setContentTitle("Transferring File")
                .setProgress(100, 0, true)
                .setContentIntent(null)
                .build()

        }

        fun createTransferNotification(context: TransferService, progress: Int): Notification {
            createLowPriorityChannel(context)

            val intent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(CANCEL_REQUEST_ACTION),
                PendingIntent.FLAG_IMMUTABLE
            )
            val action = NotificationCompat.Action(0, "Cancel", intent)

            return NotificationCompat.Builder(context, LOW_PRIO_CHANNEL_ID)
                .setDefaults(0)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setSmallIcon(R.drawable.ic_stat_share)
                .setColor(context.getColor(R.color.colorPrimaryDark))
                .addAction(action)
                .setContentTitle("Transferring File")
                .setProgress(100, progress, false)
                .setContentIntent(null)
                .build()

        }

        fun createErrorNotification(
            context: Context,
            fileTransferCommand: FileTransferCommand
        ): Notification {
            createHighPriorityChannel(context)

            val sendableFiles = SendableFile.fromUris(fileTransferCommand.dataUris, context)
            return NotificationCompat.Builder(context, HIGH_PRIO_CHANNEL_ID)
                .setShowWhen(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setSmallIcon(R.drawable.ic_stat_share)
                .setColor(context.getColor(R.color.colorPrimaryDark))
                .setContentTitle("Failed")
                .setContentText("Could not send ${sendableFiles.joinToString { it.fileName }}")
                .setContentIntent(null)
                .build()
        }

        fun createSuccessNotification(
            context: Context,
            fileTransferCommand: FileTransferCommand
        ): Notification {
            createHighPriorityChannel(context)

            val sendableFiles = SendableFile.fromUris(fileTransferCommand.dataUris, context)
            return NotificationCompat.Builder(context, HIGH_PRIO_CHANNEL_ID)
                .setShowWhen(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_stat_share)
                .setColor(context.getColor(R.color.colorPrimaryDark))
                .setContentTitle(context.getString(R.string.transfer_notification_success_title))
                .setContentText(
                    context.getString(
                        R.string.transfer_notification_success_description,
                        sendableFiles.joinToString { it.fileName })
                )
                .setContentIntent(null)
                .build()

        }
    }
}

class TransferServiceBroadcastReceiver(val service: TransferService) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TransferService.CANCEL_REQUEST_ACTION) {
            service.cancelCurrentTransfer()
        }
    }

}

data class FileTransferCommand(
    val clientName: String,
    val ipAddress: String,
    val port: Int,
    val dataUris: List<Uri>
) {

    constructor(intent: Intent) : this(
        intent.getStringExtra(TransferService.CLIENT_NAME)!!,
        intent.getStringExtra(TransferService.IP_ADDRESS)!!,
        intent.getIntExtra(TransferService.PORT, 8080),
        intent.getParcelableArrayExtra(TransferService.DATA)!!.map { it as Uri }
    )
}

val Context.notificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
