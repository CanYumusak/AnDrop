package de.canyumusak.androiddrop.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.canyumusak.androiddrop.ui.extension.ScaleIn

@Composable
fun WelcomePage(
    nextRequested: () -> Unit,
    skipRequested: () -> Unit
) {
    OnboardingScaffold(
        animateEntry = true,
        nextText = "Start Setup",
        nextRequested = nextRequested,
        skipRequested = skipRequested
    ) {
        WelcomeText()
        Subtitle()
    }
}

@Composable
private fun WelcomeText() {
    val string = buildAnnotatedString {
        withStyle(SpanStyle(MaterialTheme.colorScheme.secondary)) {
            append("Welcome to\n")
        }
        withStyle(SpanStyle(MaterialTheme.colorScheme.primary)) {
            append("AnDrop")
        }
    }
    Text(
        text = string,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.secondary
    )
}

@Composable
private fun Subtitle() {
    ScaleIn(delayTacts = 4) {
        Column {
            Spacer(modifier = Modifier.height(32.dp))
            val string = buildAnnotatedString {
                withStyle(SpanStyle(MaterialTheme.colorScheme.secondary)) {
                    append("The ")
                }
                withStyle(SpanStyle(MaterialTheme.colorScheme.primary)) {
                    append("easiest ")
                }
                withStyle(SpanStyle(MaterialTheme.colorScheme.secondary)) {
                    append("way to send files")
                }
            }
            Text(
                text = string,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}


@Preview
@Composable
private fun WelcomePagePreview() {
    OnboardingScreen(rememberOnboardingState())
}