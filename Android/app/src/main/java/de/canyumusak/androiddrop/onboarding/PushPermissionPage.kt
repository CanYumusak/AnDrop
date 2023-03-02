package de.canyumusak.androiddrop.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.canyumusak.androiddrop.R
import de.canyumusak.androiddrop.extension.highlightedStringResource
import de.canyumusak.androiddrop.theme.AnDropTheme

@Composable
fun PushPermissionPage(
    nextRequested: () -> Unit,
    skipRequested: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        nextRequested()
    }
    OnboardingScaffold(
        animateEntry = false,
        nextText = stringResource(id = R.string.onboarding_push_permission_action),
        skipText = stringResource(id = R.string.onboarding_push_permission_skip),
        nextRequested = {
            if (Build.VERSION.SDK_INT >= 33) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                nextRequested()
            }
        },
        allowSkip = true,
        skipRequested = skipRequested,
    ) {
        Text(
            text = highlightedStringResource(R.string.onboarding_push_permission_title),
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = highlightedStringResource(R.string.onboarding_push_permission_description),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = highlightedStringResource(R.string.onboarding_push_permission_hint),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview
@Composable
private fun PushPermissionPagePreview() {
    AnDropTheme {
        OnboardingScreen(rememberOnboardingState(OnboardingPage.PushPermission))
    }
}