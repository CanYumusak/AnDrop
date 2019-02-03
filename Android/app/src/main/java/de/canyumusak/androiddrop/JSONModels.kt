package de.canyumusak.androiddrop

import org.json.JSONObject
import kotlinx.serialization.*
import kotlinx.serialization.json.Json


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
data class FileProposition(val deviceName: String, val fileName: String, val fileLength: Long) : SentRequest {
    override fun serialize(): String {
        return Json.stringify(FileProposition.serializer(), this)
    }
}