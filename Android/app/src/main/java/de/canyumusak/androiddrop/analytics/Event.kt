package de.canyumusak.androiddrop.analytics

sealed interface BaseEvent

data class Event(
    val name: String,
    val payload: Map<String, Any?> = mapOf()
) : BaseEvent

data class TipEvent(
    val id: String,
    val price: Double,
) : BaseEvent
