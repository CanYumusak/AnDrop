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
fun InstallPage(
    nextRequested: () -> Unit,
    skipRequested: () -> Unit
) {
    OnboardingScaffold(
        animateEntry = false,
        nextText = stringResource(id = R.string.onboarding_install_action),
        nextRequested = nextRequested,
        skipRequested = skipRequested
    ) {
        Title()
        Subtitle()
    }
}

@Composable
private fun Title() {
    Text(
        text = highlightedStringResource(id = R.string.onboarding_install_title),
        style = MaterialTheme.typography.displayLarge,
    )
    ScaleIn(delayTacts = 2) {
        Text(
            text = "AnDrop.app",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun Subtitle() {
    ScaleIn(delayTacts = 6) {
        Column {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = highlightedStringResource(id = R.string.onboarding_install_subtitle),
                style = MaterialTheme.typography.displaySmall,
            )
        }
    }
}


@Preview
@Composable
private fun InstallPagePreview() {
    OnboardingScreen(rememberOnboardingState(OnboardingPage.Install))
}