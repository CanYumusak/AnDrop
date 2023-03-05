package de.canyumusak.androiddrop.onboarding

import de.canyumusak.androiddrop.analytics.Analytics
import de.canyumusak.androiddrop.analytics.trackEvent

object OnboardingEvents {

    fun checkSetupComplete(serverCount: Int) {
        Analytics.trackEvent(
            "Check_Setup_Complete",
            "serverCount" to serverCount,
        )
    }

    fun onboardingPage(onboardingPage: OnboardingPage) {
        Analytics.trackEvent(
            "Display_Onboarding",
            "page" to onboardingPage.name
        )
    }

    fun skip(onboardingPage: OnboardingPage) {
        Analytics.trackEvent(
            "Skip_Onboarding",
            "page" to onboardingPage.name
        )
    }

    fun grantPush(result: Boolean) {
        Analytics.trackEvent(
            "Grant_Onboarding_Push_Permission",
            "result" to result
        )
    }

    fun complete() {
        Analytics.trackEvent(
            "Complete_Onboarding",
        )
    }

    fun restart() {
        Analytics.trackEvent(
            "Restart_Onboarding",
        )
    }
}