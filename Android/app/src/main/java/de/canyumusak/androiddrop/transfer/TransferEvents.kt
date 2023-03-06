package de.canyumusak.androiddrop.transfer

import android.content.Context
import android.net.Uri
import de.canyumusak.androiddrop.analytics.Analytics
import de.canyumusak.androiddrop.analytics.trackEvent
import de.canyumusak.androiddrop.connection.State
import de.canyumusak.androiddrop.sendables.SendableFile

object TransferEvents {

    fun trackTransferRequest(dataUris: Array<Uri>?, context: Context) {
        val sendableFileType = dataUris?.firstOrNull()?.let {
            SendableFile.fromUri(it, context).javaClass.simpleName
        }

        val count = dataUris?.size ?: 0

        Analytics.trackEvent(
            "Request_Transfer",
            "type" to sendableFileType,
            "count" to count
        )
    }

    fun trackFileConnectionEvent(state: State) {
        Analytics.trackEvent(
            "Send_File",
            "state" to state.javaClass.simpleName,
        )
    }
}
