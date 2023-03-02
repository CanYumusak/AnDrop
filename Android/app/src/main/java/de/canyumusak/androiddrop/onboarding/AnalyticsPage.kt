package de.canyumusak.androiddrop.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.canyumusak.androiddrop.R
import de.canyumusak.androiddrop.extension.ScaleIn
import de.canyumusak.androiddrop.extension.primaryColor
import de.canyumusak.androiddrop.extension.secondaryColor

@Composable
fun AnalyticsPage(
    nextRequested: () -> Unit,
    skipRequested: () -> Unit
) {
    OnboardingScaffold(
        animateEntry = false,
        nextText = stringResource(id = R.string.onboarding_install_action),
        nextRequested = nextRequested,
        skipRequested = skipRequested,
    ) {
        Text(text = "Analytics")
    }
}

@Composable
private fun Title() {
    val titlePart = stringResource(id = R.string.onboarding_install_title)
        .split("**")
    val string = buildAnnotatedString {
        secondaryColor(titlePart[0])
        primaryColor(titlePart[1])
        secondaryColor(titlePart[2])
    }
    Text(
        text = string,
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
    val subtitleParts = stringResource(id = R.string.onboarding_install_subtitle)
        .split("**")
    ScaleIn(delayTacts = 6) {
        Column {
            Spacer(modifier = Modifier.height(32.dp))

            val string = buildAnnotatedString {
                primaryColor(subtitleParts[1])
                secondaryColor(subtitleParts[2])
                primaryColor(subtitleParts[3])
                secondaryColor(subtitleParts[4])
            }
            Text(
                text = string,
                style = MaterialTheme.typography.displaySmall,
            )
        }
    }
}


@Preview
@Composable
private fun AnalyticsPagePreview() {
//    OnboardingScreen(rememberOnboardingState(OnboardingPage.AnalyticsPermission))
}