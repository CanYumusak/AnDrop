package de.canyumusak.androiddrop.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.canyumusak.androiddrop.R
import de.canyumusak.androiddrop.theme.Spacings
import de.canyumusak.androiddrop.extension.animateAppearanceAlpha

@Composable
fun OnboardingScaffold(
    animateEntry: Boolean = true,
    nextText: String,
    nextRequested: () -> Unit,
    skipRequested: () -> Unit,
    content: @Composable () -> Unit
) {
    OnboardingScaffold(
        animateEntry = animateEntry,
        nextText = nextText,
        skipText = stringResource(R.string.onboarding_skip),
        nextRequested = nextRequested,
        allowSkip = true,
        skipRequested = skipRequested,
        content = content,
    )
}

@Composable
fun OnboardingScaffold(
    animateEntry: Boolean = true,
    nextText: String,
    skipText: String,
    nextRequested: () -> Unit,
    allowSkip: Boolean,
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
            if (allowSkip) {
                OutlinedButton(onClick = {
                    skipRequested()
                }) {
                    Text(
                        text = skipText,
                    )

                }
            } else {
                Spacer(modifier = Modifier.height(ButtonDefaults.MinHeight))
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