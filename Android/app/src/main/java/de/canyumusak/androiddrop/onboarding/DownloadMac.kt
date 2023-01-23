package de.canyumusak.androiddrop.onboarding

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DownloadMac() {
    val string = buildAnnotatedString {
        withStyle(SpanStyle(MaterialTheme.colorScheme.secondary)) {
            append("Prepare your ")
        }
        withStyle(SpanStyle(MaterialTheme.colorScheme.primary)) {
            append("Mac")
        }
    }
    Text(
        text = string,
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.secondary
    )
}


@Preview
@Composable
private fun DownloadMacPreview() {
    OnboardingScreen(OnboardingPages.DownloadMac, {}) {}
}