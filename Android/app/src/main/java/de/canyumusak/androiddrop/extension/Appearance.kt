package de.canyumusak.androiddrop.extension

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.delay


fun Modifier.animateAppearanceAlpha(delay: Int = 500, condition: Boolean = true): Modifier = composed {
    if (condition) {
        alpha(rememberAnimatedAppearanceAlpha(delay))
    } else {
        this
    }
}

@Composable
fun rememberBoolean(initialValue: Boolean, targetValue: Boolean, delayMs: Long): Boolean {
    var value by remember {
        mutableStateOf(initialValue)
    }

    LaunchedEffect(true) {
        delay(delayMs)
        value = targetValue
    }

    return value
}

@Composable
private fun rememberAnimatedAppearanceAlpha(delay: Int): Float {
    val animatedAlpha = remember { Animatable(0f) }
    LaunchedEffect(true) {
        animatedAlpha.animateTo(
            targetValue = 1.0f,
            animationSpec = TweenSpec(
                durationMillis = 700,
                delay = delay,
                LinearOutSlowInEasing
            )
        )
    }

    val alpha = if (LocalInspectionMode.current) {
        1.0f
    } else {
        animatedAlpha.value
    }

    return alpha
}