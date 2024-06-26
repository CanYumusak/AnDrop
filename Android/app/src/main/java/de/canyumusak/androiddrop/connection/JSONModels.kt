package de.canyumusak.androiddrop.connection

import org.json.JSONObject
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.time.Instant


interface SentRequest {
    fun serialize(): String
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

@Serializable
data class FileProposition(val deviceName: String, val files: List<File>, val type: String = "send_file") : SentRequest {

    override fun serialize(): String {
        return Json {
            encodeDefaults = true
        }.encodeToString(serializer(), this)
    }
}

@Serializable
data class File(val fileName: String, val fileLength: Long, val creationDate: Long?)