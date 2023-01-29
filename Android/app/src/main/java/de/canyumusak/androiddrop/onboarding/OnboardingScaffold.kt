package de.canyumusak.androiddrop.onboarding

import androidx.compose.foundation.layout.Arrangement
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
    var modifier = Modifier
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(Spacings.l)
        .wrapContentHeight()
    modifier = if (animateEntry) modifier.animateAppearanceAlpha(500) else modifier
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacings.l),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateAppearanceAlpha(1500)
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
        var buttonModifier = Modifier.fillMaxWidth()
        buttonModifier = if (animateEntry) buttonModifier.animateAppearanceAlpha(500) else buttonModifier
        Button(
            modifier = buttonModifier,
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