package de.canyumusak.androiddrop.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.canyumusak.androiddrop.ui.extension.ScaleIn
import de.canyumusak.androiddrop.ui.extension.primaryColor
import de.canyumusak.androiddrop.ui.extension.secondaryColor


@Composable
fun InstallPage(
    nextRequested: () -> Unit,
    skipRequested: () -> Unit
) {
    OnboardingScaffold(
        animateEntry = false,
        nextText = "AnDrop is running",
        nextRequested = nextRequested,
        skipRequested = skipRequested
    ) {
        Title()
        Subtitle()
    }
}

@Composable
private fun Title() {
    val string = buildAnnotatedString {
        secondaryColor("On your ")
        primaryColor("Mac")
        secondaryColor(" visit:")
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
    ScaleIn(delayTacts = 6) {
        Column {
            Spacer(modifier = Modifier.height(32.dp))

            val string = buildAnnotatedString {
                primaryColor("Install ")
                secondaryColor("and ")
                primaryColor("open ")
                secondaryColor("it there")
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
private fun InstallPagePreview() {
    OnboardingScreen(rememberOnboardingState(OnboardingPage.Install))
}