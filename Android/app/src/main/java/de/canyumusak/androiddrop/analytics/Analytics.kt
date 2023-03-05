package de.canyumusak.androiddrop.analytics

import android.content.Context
import android.os.Bundle
import androidx.datastore.preferences.core.edit
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import de.canyumusak.androiddrop.extension.PreferenceKeys
import de.canyumusak.androiddrop.extension.dataStore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object Analytics {

    private val hasConsent = MutableStateFlow(false)

    fun track(event: BaseEvent) {
        val firebase = Firebase.analytics
        when (event) {
            is Event -> {
                firebase.logEvent(event.name, event.payload.asBundle())
            }

            is TipEvent -> {
                firebase.logEvent("Revenue") {
                    param("id", event.id)
                    param("price", event.price)
                }
            }
        }
    }

    fun giveConsent(consent: Boolean) {
        hasConsent.value = consent
    }

    fun hasConsent(): Boolean {
        return hasConsent.value
    }

    suspend fun setup(context: Context) {
        FirebaseApp.initializeApp(context)
        hasConsent.value = context.dataStore.data.first()[PreferenceKeys.consentToAnalytics] ?: false

        coroutineScope {
            launch {
                hasConsent.collect { consent ->
                    Firebase.analytics.setAnalyticsCollectionEnabled(consent)
                    context.dataStore.edit {
                        it[PreferenceKeys.consentToAnalytics] = consent
                    }
                }
            }
        }
    }
}

fun Analytics.trackEvent(
    name: String,
    vararg pair: Pair<String, Any?>
) {
    track(
        Event(
            name,
            pair.toMap(),
        )
    )
}

private fun Map<String, Any?>.asBundle(): Bundle {
    val bundle = Bundle()
    forEach {
        when (val value = it.value) {
            is String -> {
                bundle.putString(it.key, value)
            }

            is Int -> {
                bundle.putInt(it.key, value)
            }

            else -> {
                bundle.putString(it.key, value.toString())
            }
        }
    }

    return bundle
}