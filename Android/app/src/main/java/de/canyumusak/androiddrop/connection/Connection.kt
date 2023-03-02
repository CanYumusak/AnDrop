package de.canyumusak.androiddrop.connection

import android.content.Context
import android.net.Uri
import android.util.Log
import de.canyumusak.androiddrop.sendables.SendableFile
import org.json.JSONObject
import java.io.*
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*

typealias DisconnectedDelegate = () -> Unit
typealias ConnectedDelegate = () -> Unit
typealias FinishedDelegate = () -> Unit
typealias MessageDelegate = (Event) -> Unit
typealias ProgressChangedDelegate = (Int) -> Unit

class Connection(val ipaddress: String?, val port: Int, val name: String?) {

    val ioScope = CoroutineScope(Job() + Dispatchers.IO)

    var disconnectedDelegate: DisconnectedDelegate? = null
    var connectedDelegate: ConnectedDelegate? = null
    var finishedDelegate: FinishedDelegate? = null
    var messageDelegate: MessageDelegate? = null
    var progressChangedDelegate: ProgressChangedDelegate? = null

    var connectionDisposed = false

    var socket: Socket? = null
    internal val dispatcher = Dispatcher()

    fun openConnection() = ioScope.launch(dispatcher) {
        withCurrentConnection {
            socket = Socket(ipaddress, port)
            connectedDelegate?.invoke()

            listenForMessages()
        }
    }

    fun closeConnection() = ioScope.launch(dispatcher) {
        socket?.close()
    }

    fun proposeFileSendRequest(context: Context, uris: List<Uri>) {
        val files = SendableFile.fromUris(uris, context).map { File(it.fileName, it.size) }
        try {
            sendEvent(FileProposition(deviceName(context), files))
        } catch (exception: Exception) {
            Log.w("Socket", "Socket closed with exception", exception)
            disconnectedDelegate?.invoke()
        }
    }

    fun sendEvent(event: SentRequest) = ioScope.launch(dispatcher) {
        Log.i("Socket", "Sending event ($event)")
        withCurrentConnection {
            if (socket == null) {
                throw IllegalStateException("Trying to send file before socket is initialized")
            }
            socket?.getOutputStream()?.let {
                val writer = PrintWriter(it, false)
                writer.println(event.serialize())
                writer.println()
                writer.flush()
            }
        }
    }

    fun listenForMessages() {
        val input = socket?.getInputStream()
        val reader = BufferedReader(InputStreamReader(input))
        Log.d("Socket", "Listening for messages with stream $input")

        ioScope.launch {
            try {

                Log.d("Socket", "isConnected? ${socket?.isConnected}")
                while (socket?.isConnected == true) {
                    val stringBuilder = StringBuilder()

                    for (line in reader.readLine()) {
                        stringBuilder.append(line)
                    }

                    if (!stringBuilder.isEmpty()) {
                        receivedMessage(stringBuilder.toString())
                    } else {
                        Log.d("Socket", "inputstream empty")
                    }

                    delay(100)

                }
            } catch (exception: Exception) {
                if (!connectionDisposed) {
                    Log.w("Socket", "Socket closed with exception", exception)
                    disconnectedDelegate?.invoke()
                }
            }
        }
    }

    private inline fun withCurrentConnection(crossinline block: () -> Unit) {
        ioScope.launch(dispatcher) {
            try {
                block()
            } catch (ex: UnknownHostException) {
                Log.w("Socket", "Server not found: $ex")
                disconnectedDelegate?.invoke()
            } catch (ex: IOException) {
                Log.w("Socket", "I/O error: $ex")
                disconnectedDelegate?.invoke()
            }
        }
    }

    fun receivedMessage(string: String) {
        val event = try {
            val jsonObject = JSONObject(string)
            when (jsonObject.get("type")) {
                "send_file_response" -> FilePropositionResponse.fromJsonObject(jsonObject)
                "handshake" -> HandshakeEvent()
                else -> Unknown()
            }

        } catch (exception: Exception) {
            Log.e("Socket", "error while parsing json", exception)
            Unknown()
        }

        messageDelegate?.invoke(event)
    }

    val DEFAULT_BUFFER_SIZE: Int = 8 * 1024


    fun sendFiles(context: Context, fileUris: List<Uri>) {
        Log.i("Socket", "Sending file now")
        val files = SendableFile.fromUris(fileUris, context)
        val totalFileSize = files.sumByLong { it.size }
        socket?.getOutputStream()?.let {
            var bytesCopied: Long = 0
            files.forEach { file ->
                file.inputStream.buffered().use { stream ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytes = stream.read(buffer)
                    while (bytes >= 0) {
                        it.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        bytes = stream.read(buffer)
                        progressChangedDelegate?.invoke(((bytesCopied.toDouble() / totalFileSize.toDouble()) * 100).toInt())
                    }
                }
            }
        }

        finishedDelegate?.invoke()
        connectionDisposed = true
        socket?.close()
    }


}

internal class Dispatcher : CoroutineDispatcher() {
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        this.singleThreadExecutor.submit(block)
    }
}

private inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}