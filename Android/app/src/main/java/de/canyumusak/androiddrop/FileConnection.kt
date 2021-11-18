package de.canyumusak.androiddrop

import android.util.Log
import androidx.lifecycle.MutableLiveData
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings

class FileConnection(val context: Context, fileTransferCommand: FileTransferCommand, val callback: (State) -> Unit) {

    val connection = object : MutableLiveData<Connection>() {
        override fun onInactive() {
            value?.closeConnection()
        }
    }

    val messageDelegate: MessageDelegate = {
        when (it) {
            is FilePropositionResponse.Accepted -> {
                Log.i("ConnectionVM", "Accepted file proposition. Sending now")
                connection.value?.sendFiles(context, fileUris)
            }

            is FilePropositionResponse.Denied -> {
                Log.w("ConnectionVM", "Denied file proposition")
                cancel()
            }

            is HandshakeEvent -> {
                Log.i("ConnectionVM", "Received Handshake event. Proposing file")
                connection.value?.proposeFileSendRequest(context, fileUris)
            }
            is Unknown -> Log.w("ConnectionVM", "Received unknown event")
        }
    }

    val fileUris: List<Uri>

    init {
        val ipaddress = fileTransferCommand.ipAddress
        val port = fileTransferCommand.port
        val name = fileTransferCommand.clientName
        fileUris = fileTransferCommand.dataUris

        val connection = Connection(ipaddress, port, name)
        this.connection.value = connection

        connection.messageDelegate = messageDelegate
        connection.connectedDelegate = { callback(State.Connected) }
        connection.disconnectedDelegate = { callback(State.Disconnected) }
        connection.finishedDelegate = { callback(State.Finished) }
        connection.progressChangedDelegate = { callback(State.Transferring(it)) }
    }

    fun open() {
        connection.value?.openConnection()
    }

    fun cancel() {
        connection.value?.socket?.close()
    }
}

fun deviceName(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME) ?: Build.MODEL
    } else {
        Build.MODEL
    }
}

sealed class State {
    object Idle : State()
    object Connected : State()
    object WaitingForAccept : State()
    class Transferring(val progress: Int) : State()
    object Finished : State()
    object Disconnected : State()
}
