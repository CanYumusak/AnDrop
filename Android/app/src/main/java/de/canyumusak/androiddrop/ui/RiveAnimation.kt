package de.canyumusak.androiddrop.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView

@Composable
fun RiveAnimation(resource: Int, height: Dp) {
    if (LocalInspectionMode.current) {
        Box(modifier = Modifier.height(height))
    } else {
        Box(modifier = Modifier
            .height(height)
            .fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            AndroidView(
                modifier = Modifier.height(height),
                factory = { RiveAnimationView(it) },
                update = {
                    it.setRiveResource(resource)
                }
            )
        }
    }
}