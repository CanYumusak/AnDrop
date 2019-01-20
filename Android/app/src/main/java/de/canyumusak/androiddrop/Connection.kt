package de.canyumusak.androiddrop

import android.content.Context
import android.net.Uri
import android.util.Log
import org.json.JSONObject
import java.io.*
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import android.provider.OpenableColumns
import kotlinx.coroutines.*


typealias DisconnectedDelegate = () -> Unit
typealias ConnectedDelegate = () -> Unit
typealias FinishedDelegate = () -> Unit
typealias MessageDelegate = (Event) -> Unit
typealias ProgressChangedDelegate = (Int) -> Unit

class Connection(val ipaddress: String?, val port: Int, val name: String?) {
    var disconnectedDelegate: DisconnectedDelegate? = null
    var connectedDelegate: ConnectedDelegate? = null
    var finishedDelegate: FinishedDelegate? = null
    var messageDelegate: MessageDelegate? = null
    var progressChangedDelegate: ProgressChangedDelegate? = null

    var connectionDisposed = false

    var socket: Socket? = null
    internal val dispatcher = Dispatcher()

    fun openConnection() = GlobalScope.launch(dispatcher) {
        withCurrentConnection {
            socket = Socket(ipaddress, port)
            connectedDelegate?.invoke()

            listenForMessages()
        }
    }

    fun closeConnection() = GlobalScope.launch(dispatcher) {
        socket?.close()
    }

    fun proposeFileSendRequest(context: Context, uri: Uri) {
        val file = SendableFile.fromUri(uri, context)
        try {
            sendEvent(FileProposition(deviceName, file.fileName, file.size))
        } catch (exception: Exception) {
            Log.w("Socket", "Socket closed with exception", exception)
            disconnectedDelegate?.invoke()
        }
    }

    fun sendEvent(event: SentRequest) = GlobalScope.launch(dispatcher) {
        Log.i("Socket", "Sending event ($event)")
        withCurrentConnection {
            if (socket == null) {
                throw IllegalStateException("Trying to send file before socket is initialized")
            }
            val output = socket?.getOutputStream()
            val writer = PrintWriter(output, false)

            writer.println(event.serialize())
            writer.println()
            writer.flush()
        }
    }

    fun listenForMessages() {
        val input = socket?.getInputStream()
        val reader = BufferedReader(InputStreamReader(input))
        Log.d("Socket", "Listening for messages with stream $input")

        GlobalScope.launch {
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
        GlobalScope.launch(dispatcher) {
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


    fun sendFile(context: Context, fileUri: Uri) {
        Log.i("Socket", "Sending file now")
        val file = SendableFile.fromUri(fileUri, context)

        file.inputStream.buffered().use { stream ->
            socket?.getOutputStream()?.let {
                var bytesCopied: Long = 0
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytes = stream.read(buffer)
                while (bytes >= 0) {
                    it.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    bytes = stream.read(buffer)
                    progressChangedDelegate?.invoke(((bytesCopied.toDouble() / file.size.toDouble()) * 100).toInt())
                }
            }
        }

        finishedDelegate?.invoke()
        connectionDisposed = true
        socket?.close()
    }


}

sealed class Event
class Unknown : Event()
class HandshakeEvent : Event()
sealed class FilePropositionResponse : Event() {
    class Accepted : FilePropositionResponse()
    class Denied : FilePropositionResponse()

    companion object {
        fun fromJsonObject(jsonObject: JSONObject): Event {
            return when (jsonObject.get("response")) {
                "accepted" -> Accepted()
                "denied" -> Denied()
                else -> Unknown()
            }

        }
    }
}

interface SendableFile : Serializable {
    val size: Long
    val fileName: String
    val inputStream: InputStream

    companion object {
        fun fromUri(uri: Uri, context: Context): SendableFile {
            return when {
                uri.toString().startsWith("content") -> ContentProviderFile(context, uri)
                else -> ClassicFile(context, uri)
            }
        }
    }
}

class ClassicFile(val context: Context, uri: Uri) : SendableFile {
    val file = File(uri.path)

    override val size: Long
        get() = file.length()
    override val fileName: String
        get() = file.name
    override val inputStream: InputStream
        get() = file.inputStream()

}

class ContentProviderFile(val context: Context, val uri: Uri) : SendableFile {
    override val inputStream: InputStream
        get() = context.contentResolver.openInputStream(uri)

    override val size: Long
        get() {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            return cursor.use {
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE))
                } else {
                    0
                }
            }
        }

    override val fileName: String
        get() {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            return cursor.use {
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                } else {
                    "invalid"
                }
            }
        }
}

interface SentRequest {
    fun serialize(): String
}

class FileProposition(val deviceName: String, val fileName: String, val fileLength: Long) : SentRequest {
    override fun serialize(): String {
        val jsonObject = JSONObject()
        jsonObject.put("type", "send_file")
        jsonObject.put("devicename", deviceName)
        jsonObject.put("filename", fileName)
        jsonObject.put("fileLength", fileLength)
        return jsonObject.toString(4)
    }

    override fun toString(): String {
        return "FileProposition ($deviceName, $fileName, $fileLength)"
    }
}

internal class Dispatcher : CoroutineDispatcher() {
    val singleThreadExecutor = Executors.newSingleThreadExecutor()!!

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        singleThreadExecutor.submit(block)
    }
}