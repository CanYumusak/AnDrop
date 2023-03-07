package de.canyumusak.androiddrop.sendables

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream

class ClassicFile(val context: Context, uri: Uri) : SendableFile {

    private val file = File(uri.path)

    override val size: Long
        get() = file.length()

    override val fileName: String
        get() = file.name

    override val inputStream: InputStream
        get() = file.inputStream()

}
