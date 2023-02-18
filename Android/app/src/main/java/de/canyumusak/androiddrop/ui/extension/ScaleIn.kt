package de.canyumusak.androiddrop.ui.extension

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.delay


@Composable
fun ScaleIn(delayTacts: Int = 1, content: @Composable () -> Unit) {
    ScaleIn(delayTacts = delayTacts, initiallyVisible = false, content = content)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScaleIn(delayTacts: Int = 1, initiallyVisible: Boolean, content: @Composable () -> Unit) {
    val inspectorMode = LocalInspectionMode.current
    var visible by remember {
        mutableStateOf(inspectorMode || initiallyVisible)
    }
    LaunchedEffect(true) {
        delay(delayTacts * 500L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically() + scaleIn()
    ) {
        content()
    }
}
