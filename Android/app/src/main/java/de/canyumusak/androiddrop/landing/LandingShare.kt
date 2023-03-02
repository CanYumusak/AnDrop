package de.canyumusak.androiddrop.landing

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import de.canyumusak.androiddrop.TransferActivity

object LandingShare {
    fun shareDefaultFile(context: Context) {
        val andropIntent = Intent(context, TransferActivity::class.java)
        andropIntent.action = TransferActivity.INTENT_SEND_EXAMPLE
        val intentChooser = Intent.createChooser(andropIntent, "Choose to send with AnDrop")
        intentChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>())

        context.startActivity(intentChooser)
    }
}