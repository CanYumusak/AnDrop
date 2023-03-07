package de.canyumusak.androiddrop.transfer

import android.content.Context
import android.net.Uri
import de.canyumusak.androiddrop.analytics.Analytics
import de.canyumusak.androiddrop.analytics.trackEvent
import de.canyumusak.androiddrop.connection.State
import de.canyumusak.androiddrop.sendables.ClassicFile
import de.canyumusak.androiddrop.sendables.ContentProviderFile
import de.canyumusak.androiddrop.sendables.ExampleFile
import de.canyumusak.androiddrop.sendables.SendableFile

object TransferEvents {

    fun trackTransferRequest(dataUris: Array<Uri>?, context: Context) {
        val sendableFileType = dataUris?.firstOrNull()?.let {

            when (SendableFile.fromUri(it, context)) {
                is ClassicFile -> "classic_file"
                is ContentProviderFile -> "content_provider"
                is ExampleFile -> "example"
            }
        }

        val count = dataUris?.size ?: 0

        Analytics.trackEvent(
            "Request_Transfer",
            "type" to sendableFileType,
            "count" to count
        )
    }

    fun trackFileConnectionEvent(state: State) {
        val stateName = when (state) {
            State.Connected -> "connected"
            State.Disconnected -> "disconnected"
            State.Finished -> "finished"
            State.Idle -> "idle"
            is State.Transferring -> "transferring"
            State.WaitingForAccept -> "waiting_for_accept"
        }
        Analytics.trackEvent(
            "Send_File",
            "state" to stateName,
        )
    }
}
