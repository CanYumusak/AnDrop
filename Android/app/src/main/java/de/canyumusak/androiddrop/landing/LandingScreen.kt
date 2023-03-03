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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
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
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(Spacings.l),
                verticalArrangement = Arrangement.spacedBy(Spacings.l),
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = stringResource(id = R.string.landing_made_in_munich),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Paragraph(
                    title = stringResource(id = R.string.landing_support_androp_title),
                    paragraph = stringResource(id = R.string.landing_support_androp_description),
                    buttonText = stringResource(id = R.string.landing_support_androp_action),
                    painter = rememberVectorPainter(image = Icons.Default.ShoppingCart),
                    important = true,
                    action = openTipBox
                )

                Text(
                    text = stringResource(id = R.string.landing_support_support_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                val context = LocalContext.current
                Paragraph(
                    title = stringResource(id = R.string.landing_support_first_file_title),
                    paragraph = stringResource(id = R.string.landing_support_first_file_description),
                    buttonText = stringResource(id = R.string.landing_support_first_file_action),
                    painter = painterResource(R.drawable.ic_share_20),
                    important = false,
                    action = {
                        LandingShare.shareDefaultFile(context)
                    }
                )

                Paragraph(
                    title = stringResource(id = R.string.landing_support_reonboard_title),
                    paragraph = stringResource(id = R.string.landing_support_reonboard_description),
                    buttonText = stringResource(id = R.string.landing_support_reonboard_action),
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
            false -> MaterialTheme.typography.titleMedium
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