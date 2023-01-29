package de.canyumusak.androiddrop.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.canyumusak.androiddrop.theme.AnDropTheme

@Composable
fun OnboardingScreen(
    onboardingPages: OnboardingPages,
    nextRequested: () -> Unit,
    skipRequested: () -> Unit
) {
    AnDropTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            when (onboardingPages) {
                OnboardingPages.Welcome -> WelcomePage(nextRequested, skipRequested)
                OnboardingPages.Install -> InstallPage(nextRequested, skipRequested)
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(
        OnboardingPages.Welcome,
        {},
    ) {}
}