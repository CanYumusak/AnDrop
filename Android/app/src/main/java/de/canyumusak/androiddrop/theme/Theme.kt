package de.canyumusak.androiddrop.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun darkColorPalette(context: Context): ColorScheme {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else {
        darkColorScheme(
            primary = Color.Gray,
            secondary = Color.White,
            background = Color.Black,
        )
    }
}

private fun lightColorPalette(context: Context): ColorScheme {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicLightColorScheme(context)
    } else {
        lightColorScheme(
            primary = Color.Gray,
            secondary = Color.Black,
            background = Color.White,
        )
    }
}

@Composable
fun AnDropTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        darkColorPalette(LocalContext.current)
    } else {
        lightColorPalette(LocalContext.current)
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
    ) {
        CompositionLocalProvider(
            LocalRippleTheme provides AnDropRippleTheme,
            LocalContentColor provides MaterialTheme.colorScheme.primary,
            LocalIndication provides rememberRipple(),
        ) {
            content()
        }
    }
}

@Immutable
private object AnDropRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = MaterialTheme.colorScheme.primary

    @Composable
    override fun rippleAlpha() = DefaultRippleAlpha
}

private val DefaultRippleAlpha = RippleAlpha(
    pressedAlpha = State.PressedStateLayerOpacity,
    focusedAlpha = State.FocusStateLayerOpacity,
    draggedAlpha = State.DraggedStateLayerOpacity,
    hoveredAlpha = State.HoverStateLayerOpacity
)

internal object State {
    const val DraggedStateLayerOpacity = 0.16f
    const val FocusStateLayerOpacity = 0.12f
    const val HoverStateLayerOpacity = 0.08f
    const val PressedStateLayerOpacity = 0.12f
}