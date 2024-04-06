package de.canyumusak.androiddrop.inappreview

import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <TResult> Task<TResult>.await() = suspendCoroutine<TResult> { continuation ->
    addOnCompleteListener {
        if (it.isSuccessful) {
            continuation.resume(it.result)
        } else {
            continuation.resumeWithException(it.exception!!)
        }
    }
}