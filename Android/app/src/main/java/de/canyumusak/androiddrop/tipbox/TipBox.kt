package de.canyumusak.androiddrop.tipbox

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import app.rive.runtime.kotlin.RiveAnimationView
import de.canyumusak.androiddrop.BillingConnectionState
import de.canyumusak.androiddrop.BillingViewModel
import de.canyumusak.androiddrop.Details
import de.canyumusak.androiddrop.PurchaseResult
import de.canyumusak.androiddrop.R
import de.canyumusak.androiddrop.Tip
import de.canyumusak.androiddrop.theme.AnDropTheme
import de.canyumusak.androiddrop.theme.Spacings
import de.canyumusak.androiddrop.ui.RiveAnimation
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TipBox(
    viewModel: BillingViewModel = viewModel(),
    dismissRequested: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        var tipBoxState by remember { mutableStateOf(TipBoxState.TipBox) }
        val activity = LocalContext.current as Activity
        val scope = rememberCoroutineScope()

        AnimatedContent(targetState = tipBoxState) { state ->
            TipBox(
                state = state,
                billingConnectionState = viewModel.billingConnectionState.collectAsState().value,
                skuDetails = viewModel.skuViewDetails.collectAsState().value,
                onClick = {
                    scope.launch {
                        val event = viewModel.buyTip(activity, it)
                        when (event.result) {
                            PurchaseResult.Cancelled -> {}
                            PurchaseResult.Fail -> {}
                            PurchaseResult.Success -> {
                                tipBoxState = TipBoxState.ThankYou
                            }
                        }
                    }
                }
            )
        }
    }

}

@Composable
fun TipBox(
    state: TipBoxState,
    billingConnectionState: BillingConnectionState,
    skuDetails: Map<Tip, Details>?,
    onClick: (Tip) -> Unit,
) {
    when (state) {
        TipBoxState.TipBox -> {
            TipBox(
                billingConnectionState = billingConnectionState,
                skuDetails = skuDetails,
                onClick = onClick
            )
        }

        TipBoxState.ThankYou -> {
            ThankYou()
        }
    }
}

@Composable
fun TipBox(
    billingConnectionState: BillingConnectionState,
    skuDetails: Map<Tip, Details>?,
    onClick: (Tip) -> Unit,
) {
    when (billingConnectionState) {
        BillingConnectionState.Connected -> {
            TipBoxList(skuDetails, onClick)
        }

        BillingConnectionState.Failed -> {
            Failed()
        }

        BillingConnectionState.Connecting,
        BillingConnectionState.GettingDetails -> {
            RiveAnimation(R.raw.loading, 100.dp)
        }
    }
}

@Composable
private fun Failed() {
    Column(
        modifier = Modifier.padding(Spacings.m),
        verticalArrangement = Arrangement.spacedBy(Spacings.m),
    ) {
        RiveAnimation(R.raw.failed, 100.dp)
        Text(
            text = "Failed to reach the Google Play Billing. But we highly appreciate the gesture!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun TipBoxList(skuDetails: Map<Tip, Details>?, onClick: (Tip) -> Unit) {
    Column(
        modifier = Modifier.padding(Spacings.m),
        verticalArrangement = Arrangement.spacedBy(Spacings.m),
    ) {

        Text(
            text = "Tip Box",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Tip.values()
            .reversed()
            .forEach {
                skuDetails?.get(it)?.let { details ->
                    TipEntry(
                        tip = it,
                        details = details,
                        onClick = { onClick(it) },
                    )
                }
            }
    }
}

@Composable
private fun ThankYou() {
    Column(
        modifier = Modifier.padding(Spacings.m),
        verticalArrangement = Arrangement.spacedBy(Spacings.m),
    ) {
        RiveAnimation(R.raw.heart_animation, 100.dp)

        Text(
            text = "You are amazing",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "Thank you so much for your valuable contribution to the development of AnDrop. This helps covering some bills!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

enum class TipBoxState {
    TipBox,
    ThankYou
}

@Composable
@Preview
private fun TipBoxPreview() {
    AnDropTheme {
        Surface {
            TipBox(
                TipBoxState.TipBox,
                BillingConnectionState.Connecting,
                previewDetails(),
            ) { }
        }
    }
}


@Composable
@Preview
private fun TipBoxThankYouPreview() {
    AnDropTheme {
        Surface {
            TipBox(
                TipBoxState.ThankYou,
                BillingConnectionState.Connecting,
                previewDetails(),
            ) { }
        }
    }
}

private fun previewDetails(): Map<Tip, Details> {
    return mapOf(
        Tip.Big to Details("Massive Tip", "10,00"),
        Tip.Medium to Details("Medium Tip", "5,00"),
        Tip.Small to Details("Small Tip", "1,00"),
    )
}