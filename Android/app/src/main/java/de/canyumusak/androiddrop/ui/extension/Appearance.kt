package de.canyumusak.androiddrop.ui.extension

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalInspectionMode


fun Modifier.animateAppearanceAlpha(delay: Int = 500): Modifier = composed {
    alpha(rememberAnimatedAppearanceAlpha(delay))
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