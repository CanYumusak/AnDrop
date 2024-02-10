package de.canyumusak.androiddrop.sendables

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.time.Instant

class ClassicFile(val context: Context, uri: Uri) : SendableFile {

    private val file = File(uri.path)

    override val size: Long
        get() = file.length()

    override val fileName: String
        get() = file.name

    override val inputStream: InputStream
        get() = file.inputStream()

    override val creationDate: Instant?
        get() = try {
            val creationTime = Files.getAttribute(
                file.toPath(),
                "creationTime"
            ) as FileTime
            creationTime.toInstant()
        } catch (exception: IOException) {
            null
        }

}
