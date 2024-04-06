package de.canyumusak.androiddrop.inappreview

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory
import de.canyumusak.androiddrop.BuildConfig

object InAppReviewManager {
    private const val REVIEW_PREFERENCE_KEY = "inappreview"
    private const val TEST_INAPP_REVIEW = true

    fun fileTransferSucceeded(context: Context) {
        val sharedPref = context.getSharedPreferences(REVIEW_PREFERENCE_KEY, Context.MODE_PRIVATE)
        val lastCount = sharedPref.getLong("transferCount", 0)
        sharedPref.edit().putLong("last_review_request", lastCount + 1).apply()
    }

    suspend fun requestReview(activity: Activity) {
        if (shouldRequestReview(activity)) {
            requestReviewFlow(activity)
        }
    }

    private suspend fun requestReviewFlow(activity: Activity) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            val reviewInfo = request.await()
            manager.launchReviewFlow(activity, reviewInfo).await()
            resetReviewRequest(activity)
            Log.i("InAppReviewManager", "Review request successful")
        } catch (exception: Exception) {
            Log.e(
                "InAppReviewManager",
                "Failed to request review flow",
                exception
            )
        }
    }

    private fun shouldRequestReview(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(REVIEW_PREFERENCE_KEY, Context.MODE_PRIVATE)
        val lastCount = sharedPref.getLong("last_review_request", 0)
        return lastCount >= 5 || (BuildConfig.DEBUG && TEST_INAPP_REVIEW)
    }

    private fun resetReviewRequest(context: Context) {
        val sharedPref = context.getSharedPreferences(REVIEW_PREFERENCE_KEY, Context.MODE_PRIVATE)
        sharedPref.edit().putLong("last_review_request", 0).apply()
    }
}