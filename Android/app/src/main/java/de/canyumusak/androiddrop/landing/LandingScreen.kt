package de.canyumusak.androiddrop.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.canyumusak.androiddrop.R
import de.canyumusak.androiddrop.theme.AnDropTheme
import de.canyumusak.androiddrop.theme.Spacings

@Composable
fun LandingScreen(startOnboarding: () -> Unit) {
    AnDropTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .scrollable(rememberScrollState(), Orientation.Vertical),
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(Spacings.l),
                verticalArrangement = Arrangement.spacedBy(Spacings.l),
            ) {

                Text(
                    text = "AnDrop",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                val context = LocalContext.current
                Paragraph(
                    title = "Share your first file",
                    paragraph = "AnDrop is a share menu app and thus works out of the share menu.",
                    buttonText = "Send Example",
                    painter = painterResource(R.drawable.ic_share_20),
                    action = {
                        LandingShare.shareDefaultFile(context)
                    }
                )

                Paragraph(
                    title = "I can't make it work",
                    paragraph = "Make sure to install AnDrop on your Mac as well. We recommend going through the onboarding again.",
                    buttonText = "Start Onboarding",
                    painter = rememberVectorPainter(image = Icons.Default.PlayArrow),
                    action = {
                        startOnboarding()
                    }
                )

                Paragraph(
                    title = "Support AnDrop",
                    paragraph = "AnDrop is free to use. However the developers rely on your help to keep it that way. Please consider leaving a tip if you find it useful.",
                    buttonText = "To the Tip Box",
                    painter = rememberVectorPainter(image = Icons.Default.ShoppingCart),
                    action = {
                    }
                )
            }
        }
    }
}

@Composable
private fun Paragraph(
    title: String,
    paragraph: String,
    buttonText: String,
    painter: Painter,
    action: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacings.m),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = paragraph,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        OutlinedButton(
            onClick = action,
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(LocalContentColor.current),
            )

            Spacer(modifier = Modifier.size(Spacings.xs))
            Text(text = buttonText)
        }
    }
}

@Preview
@Composable
fun LandingScreenPreview() {
    LandingScreen {

    }
}