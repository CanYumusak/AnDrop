package de.canyumusak.androiddrop.sendables

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.InputStream
import java.time.Instant

class ContentProviderFile(val context: Context, val uri: Uri) : SendableFile {

    override val inputStream: InputStream
        get() = context.contentResolver.openInputStream(uri)!!

    override val creationDate: Instant?
        get() {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val result = cursor.use {
                val columnIndex = cursor?.getColumnIndex("datetaken") ?: -1
                if (cursor != null && cursor.moveToFirst() && columnIndex != -1) {
                    cursor.getLong(columnIndex)
                } else {
                    Instant.now().toEpochMilli()
                }
            }
            return Instant.ofEpochMilli(result)
        }

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
                if (cursor?.moveToFirst() == true) {
                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                } else {
                    "invalid"
                }
            }
        }
}