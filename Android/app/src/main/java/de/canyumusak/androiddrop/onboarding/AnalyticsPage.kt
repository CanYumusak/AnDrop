package de.canyumusak.androiddrop.onboarding

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.canyumusak.androiddrop.R
import de.canyumusak.androiddrop.analytics.Analytics
import de.canyumusak.androiddrop.extension.highlightedStringResource

@Composable
fun AnalyticsPage(
    nextRequested: () -> Unit,
    skipRequested: () -> Unit,
) {
    OnboardingScaffold(
        animateEntry = false,
        nextText = stringResource(id = R.string.onboarding_analytics_action),
        nextRequested = {
            Analytics.giveConsent(true)
            nextRequested()
        },
        skipRequested = {
            Analytics.giveConsent(false)
            skipRequested()
        },
        skipText = stringResource(R.string.onboarding_analytics_skip),
    ) {
        Text(
            text = highlightedStringResource(id = R.string.onboarding_analytics_title),
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = highlightedStringResource(id = R.string.onboarding_analytics_description),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Preview
@Composable
private fun AnalyticsPagePreview() {
    OnboardingScreen(rememberOnboardingState(OnboardingPage.AnalyticsPermission))
}