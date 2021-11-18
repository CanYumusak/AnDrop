package de.canyumusak.androiddrop

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    private val billingViewModel by viewModels<BillingViewModel>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val tutorial = resources.getStringArray(R.array.welcome_to_androp_tutorial).toList()

        findViewById<TextView>(R.id.main_activity_tutorial).text = tutorial.withLeadingEnumeration(this, paragraphSpacing = 16)

        val smallAmountButton = findViewById<TextView>(R.id.smallAmountButton)
        val mediumAmountButton = findViewById<TextView>(R.id.mediumAmountButton)
        val bigAmountButton = findViewById<TextView>(R.id.bigAmountButton)

        smallAmountButton.setOnClickListener { billingViewModel.buyTip(this, Tip.Small) }
        mediumAmountButton.setOnClickListener { billingViewModel.buyTip(this, Tip.Medium) }
        bigAmountButton.setOnClickListener { billingViewModel.buyTip(this, Tip.Big) }

        billingViewModel.billingConnectionState.observe(this) {
            when (it) {
                BillingConnectionState.Connected -> {
                    findViewById<TextView>(R.id.textView4).isVisible = false
                }
                BillingConnectionState.Failed -> {
                    findViewById<TextView>(R.id.textView4).isVisible = true
                    smallAmountButton.isVisible = false
                    mediumAmountButton.isVisible = false
                    bigAmountButton.isVisible = false
                }
                BillingConnectionState.GettingDetails,
                BillingConnectionState.Connecting -> {
                    findViewById<TextView>(R.id.textView4).isVisible = false
                    smallAmountButton.isVisible = false
                    mediumAmountButton.isVisible = false
                    bigAmountButton.isVisible = false
                }
            }
        }

        billingViewModel.skuDetails.observe(this) {
            it.forEach { (tip, details) ->
                val textView = when (tip) {
                    Tip.Big -> bigAmountButton
                    Tip.Medium -> mediumAmountButton
                    Tip.Small -> smallAmountButton
                }

                textView.text = "${details.title}\n${details.price}"
                textView.isVisible = true
            }
        }

        billingViewModel.purchaseResult.observe(this) { event ->

            if (event.result is PurchaseResult.Success) {
                val alertDialog = AlertDialog.Builder(this).create()
                alertDialog.setTitle(R.string.tipping_jar_dialog_sucess_title)
                alertDialog.setMessage(getString(R.string.tipping_jar_dialog_sucess_message))
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.tipping_jar_dialog_sucess_button_title)) { dialog: DialogInterface, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
            } else if (event.result is PurchaseResult.Fail) {
                val alertDialog = AlertDialog.Builder(this).create()
                alertDialog.setTitle(R.string.tipping_jar_dialog_error_title)
                alertDialog.setMessage(getString(R.string.tipping_jar_dialog_error_message))
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.tipping_jar_dialog_error_button_title)) { dialog: DialogInterface, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
            }
        }

        val tippingSumView = findViewById<TextView>(R.id.textView5)

        billingViewModel.tippingSum.observe(this) { sum ->
            when (sum) {
                is TippingSum.FailedToLoad -> {
                    tippingSumView.isVisible = false
                }
                is TippingSum.Suceeded -> {
                    tippingSumView.isVisible = true
                    tippingSumView.text = getString(R.string.tipping_jar_tips_message, sum.sum)
                }
                is TippingSum.NoTips -> {
                    tippingSumView.isVisible = false
                }
            }
        }
    }
}
