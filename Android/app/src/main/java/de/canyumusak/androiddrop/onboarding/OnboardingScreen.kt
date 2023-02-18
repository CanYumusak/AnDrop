package de.canyumusak.androiddrop.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.canyumusak.androiddrop.theme.AnDropTheme
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onboardingState: OnboardingState,
) {
    val scope = rememberCoroutineScope()
    val currentPage by onboardingState.currentPage
    val nextRequested: () -> Unit = { scope.launch { onboardingState.next() } }
    val skipRequested: () -> Unit = { scope.launch { onboardingState.skip() } }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        when (currentPage) {
            OnboardingPage.Welcome -> WelcomePage(nextRequested, skipRequested)
            OnboardingPage.Install -> InstallPage(nextRequested, skipRequested)
            OnboardingPage.CheckSetup -> CheckSetupPage(nextRequested, skipRequested)
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    AnDropTheme {
        OnboardingScreen(
            rememberOnboardingState(),
        )
    }
}