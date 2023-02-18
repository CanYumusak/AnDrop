package de.canyumusak.androiddrop

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.canyumusak.androiddrop.landing.LandingScreen
import de.canyumusak.androiddrop.onboarding.OnboardingScreen
import de.canyumusak.androiddrop.onboarding.rememberOnboardingState
import de.canyumusak.androiddrop.theme.AnDropTheme

@Composable
fun MainPage() {
    AnDropTheme {
        val navController = rememberNavController()
        val onboardingState = rememberOnboardingState()

        Box {
            NavHost(navController = navController, startDestination = "landing") {
                composable("landing") {
                    LandingScreen {
                        onboardingState.restart()
                    }
                }
            }

            if (onboardingState.show.value) {
                OnboardingScreen(onboardingState)
            }
        }
    }
}
