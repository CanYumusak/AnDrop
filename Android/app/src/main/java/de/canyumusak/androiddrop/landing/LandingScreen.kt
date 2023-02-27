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
fun LandingScreen(
    startOnboarding: () -> Unit,
    openTipBox: () -> Unit,
) {
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
                Column(
                ) {
                    Text(
                        text = "AnDrop",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Made in Munich with Love",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Paragraph(
                    title = "Support AnDrop",
                    paragraph = "AnDrop is free. However the developers rely on your help to keep it that way. Please consider leaving a tip if you find it useful.",
                    buttonText = "To the Tip Box",
                    painter = rememberVectorPainter(image = Icons.Default.ShoppingCart),
                    important = true,
                    action = openTipBox
                )

                Text(
                    text = "Troubleshoot",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                val context = LocalContext.current
                Paragraph(
                    title = "Share your first file",
                    paragraph = "AnDrop is a share menu app and thus works out of the share menu.",
                    buttonText = "Send Example",
                    painter = painterResource(R.drawable.ic_share_20),
                    important = false,
                    action = {
                        LandingShare.shareDefaultFile(context)
                    }
                )

                Paragraph(
                    title = "I can't make it work",
                    paragraph = "Make sure to install AnDrop on your Mac as well. We recommend going through the onboarding again.",
                    buttonText = "Start Onboarding",
                    painter = rememberVectorPainter(image = Icons.Default.PlayArrow),
                    important = false,
                    action = startOnboarding
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
    important: Boolean,
    action: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacings.s),
    ) {
        val titleStyle = when (important) {
            true -> MaterialTheme.typography.headlineSmall
            false -> MaterialTheme.typography.titleLarge
        }
        Text(
            text = title,
            style = titleStyle,
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
    LandingScreen(
        {}, {}
    )
}