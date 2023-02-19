package de.canyumusak.androiddrop

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { MainPage() }
    }

    private fun setupOldScreens() {
//
//        billingViewModel.purchaseResult.observe(this) { event ->
//
//            if (event.result is PurchaseResult.Success) {
//                val alertDialog = AlertDialog.Builder(this).create()
//                alertDialog.setTitle(R.string.tipping_jar_dialog_sucess_title)
//                alertDialog.setMessage(getString(R.string.tipping_jar_dialog_sucess_message))
//                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.tipping_jar_dialog_sucess_button_title)) { dialog: DialogInterface, _ ->
//                    dialog.dismiss()
//                }
//                alertDialog.show()
//            } else if (event.result is PurchaseResult.Fail) {
//                val alertDialog = AlertDialog.Builder(this).create()
//                alertDialog.setTitle(R.string.tipping_jar_dialog_error_title)
//                alertDialog.setMessage(getString(R.string.tipping_jar_dialog_error_message))
//                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.tipping_jar_dialog_error_button_title)) { dialog: DialogInterface, _ ->
//                    dialog.dismiss()
//                }
//                alertDialog.show()
//            }
//        }
    }
}
