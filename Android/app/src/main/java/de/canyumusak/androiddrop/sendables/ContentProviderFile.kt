package de.canyumusak.androiddrop.sendables

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.InputStream

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
                if (cursor?.moveToFirst() == true) {
                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                } else {
                    "invalid"
                }
            }
        }
}