package de.canyumusak.androiddrop.sendables

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.io.Serializable

sealed interface SendableFile : Serializable {

    val size: Long
    val fileName: String
    val inputStream: InputStream

    companion object {
        fun fromUri(uri: Uri, context: Context): SendableFile {
            return when {
                uri.toString().startsWith("content") -> ContentProviderFile(context, uri)
                uri == ExampleFile.URI -> ExampleFile(context)
                else -> ClassicFile(context, uri)
            }
        }

        fun fromUris(uris: List<Uri>, context: Context): List<SendableFile> {
            return uris.map { fromUri(it, context) }
        }
    }
}