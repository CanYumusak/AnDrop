package de.canyumusak.androiddrop.tipbox

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipBoxBottomsheetScaffold(show: Boolean = false, onDismissRequest: () -> Unit) {
    val sheetState = rememberSheetState()

    LaunchedEffect(show) {
        if (show) {
            SheetValue.Expanded
        } else {
            SheetValue.Hidden
        }
    }

    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
        ) {
            TipBox(
                dismissRequested = onDismissRequest
            )
        }
    }
}
