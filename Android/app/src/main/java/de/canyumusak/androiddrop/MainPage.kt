package de.canyumusak.androiddrop

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.canyumusak.androiddrop.landing.LandingScreen
import de.canyumusak.androiddrop.onboarding.OnboardingScreen
import de.canyumusak.androiddrop.onboarding.rememberOnboardingState
import de.canyumusak.androiddrop.tipbox.TipBoxBottomsheetScaffold
import de.canyumusak.androiddrop.theme.AnDropTheme

@Composable
fun MainPage() {
    AnDropTheme {
        val onboardingState = rememberOnboardingState()

        var showBottomSheet by remember {
            mutableStateOf(false)
        }

        Box {
            LandingScreen(
                startOnboarding = { onboardingState.restart() },
                openTipBox = { showBottomSheet = true },
            )

            TipBoxBottomsheetScaffold(
                show = showBottomSheet,
                onDismissRequest = {
                    showBottomSheet = false
                },
            )

            if (onboardingState.show.value) {
                OnboardingScreen(onboardingState)
            }
        }
    }
}
