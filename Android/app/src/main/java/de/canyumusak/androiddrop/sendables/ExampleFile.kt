package de.canyumusak.androiddrop.sendables

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.time.Instant

class ExampleFile(val context: Context) : SendableFile {

    private val exampleFile = "examplepic.jpg"

    override val size: Long
        get() = context.assets.openFd(exampleFile).length

    override val fileName: String
        get() = "Hello Androp.jpg"

    override val inputStream: InputStream
        get() {
            return context.assets.open(exampleFile)
        }

    override val creationDate : Instant = Instant.now()

    companion object {
        val URI = Uri.Builder()
            .scheme("androp")
            .authority("example")
            .build()
    }

}
