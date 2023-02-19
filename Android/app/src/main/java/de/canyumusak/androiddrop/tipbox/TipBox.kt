package de.canyumusak.androiddrop.tipbox

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import de.canyumusak.androiddrop.BillingConnectionState
import de.canyumusak.androiddrop.BillingViewModel
import de.canyumusak.androiddrop.Details
import de.canyumusak.androiddrop.PurchaseResult
import de.canyumusak.androiddrop.Tip
import de.canyumusak.androiddrop.theme.AnDropTheme
import de.canyumusak.androiddrop.theme.Spacings
import kotlinx.coroutines.launch

@Composable
fun TipBox(
    viewModel: BillingViewModel = viewModel(),
    dismissRequested: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        val activity = LocalContext.current as Activity
        val scope = rememberCoroutineScope()
        TipBox(
            billingConnectionState = viewModel.billingConnectionState.collectAsState().value,
            skuDetails = viewModel.skuViewDetails.collectAsState().value,
            onClick = {
                scope.launch {
                    val result = viewModel.buyTip(activity, it)
                    when (result.result) {
                        PurchaseResult.Cancelled -> {}
                        PurchaseResult.Fail -> dismissRequested()
                        PurchaseResult.Success -> dismissRequested()
                    }
                }
            }
        )
    }
}

@Composable
fun TipBox(
    billingConnectionState: BillingConnectionState,
    skuDetails: Map<Tip, Details>?,
    onClick: (Tip) -> Unit,
) {
    Column(
        modifier = Modifier.padding(Spacings.m),
        verticalArrangement = Arrangement.spacedBy(Spacings.m),
    ) {
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
@Preview
private fun TipBoxBottomsheetScaffoldPreview() {
    AnDropTheme {
        Surface {
            TipBox(
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