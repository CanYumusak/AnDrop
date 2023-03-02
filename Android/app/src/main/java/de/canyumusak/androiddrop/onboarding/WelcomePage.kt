package de.canyumusak.androiddrop.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.canyumusak.androiddrop.R
import de.canyumusak.androiddrop.extension.ScaleIn
import de.canyumusak.androiddrop.extension.highlightedStringResource

@Composable
fun WelcomePage(
    nextRequested: () -> Unit,
    skipRequested: () -> Unit
) {
    OnboardingScaffold(
        animateEntry = true,
        nextText = stringResource(id = R.string.onboarding_welcome_action),
        nextRequested = nextRequested,
        skipRequested = skipRequested
    ) {
        WelcomeText()
        Subtitle()
    }
}

@Composable
private fun WelcomeText() {
    Text(
        text = highlightedStringResource(id = R.string.onboarding_welcome_greeting),
        style = MaterialTheme.typography.displayLarge,
    )
}

@Composable
private fun Subtitle() {
    ScaleIn(delayTacts = 4) {
        Column {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = highlightedStringResource(id = R.string.onboarding_welcome_subtitle),
                style = MaterialTheme.typography.displaySmall,
            )
        }
    }
}


@Preview
@Composable
private fun WelcomePagePreview() {
    OnboardingScreen(rememberOnboardingState())
}