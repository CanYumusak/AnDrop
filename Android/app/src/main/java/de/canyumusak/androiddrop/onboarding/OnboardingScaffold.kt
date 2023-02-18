package de.canyumusak.androiddrop.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.canyumusak.androiddrop.theme.Spacings
import de.canyumusak.androiddrop.ui.extension.animateAppearanceAlpha

@Composable
fun OnboardingScaffold(
    animateEntry: Boolean = true,
    nextText: String,
    nextRequested: () -> Unit,
    skipRequested: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(Spacings.l)
            .wrapContentHeight()
            .animateAppearanceAlpha(500, animateEntry),
        verticalArrangement = Arrangement.spacedBy(Spacings.l),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateAppearanceAlpha(1500, animateEntry)
        ) {
            Spacer(modifier = Modifier.weight(1.0f))
            OutlinedButton(onClick = {
                skipRequested()
            }) {
                Text(
                    text = "Skip Setup",
                )

            }
        }
        Spacer(modifier = Modifier.weight(1.0f))
        content()
        Spacer(modifier = Modifier.weight(1.0f))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .animateAppearanceAlpha(500, animateEntry),
            onClick = {
                nextRequested()
            }
        ) {
            Text(
                text = nextText,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}