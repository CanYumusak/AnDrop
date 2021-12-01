package de.canyumusak.androiddrop

import org.json.JSONObject
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.*


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
        }.encodeToString(FileProposition.serializer(), this)
    }
}

@Serializable
data class File(val fileName: String, val fileLength: Long)