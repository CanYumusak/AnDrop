package de.canyumusak.androiddrop.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import de.canyumusak.androiddrop.theme.AnDropTheme
import de.canyumusak.androiddrop.theme.Spacings
import de.canyumusak.androiddrop.ui.extension.animateAppearanceAlpha

@Composable
fun OnboardingScreen() {
    AnDropTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(Spacings.l)
                    .wrapContentHeight()
                    .animateAppearanceAlpha(500),
                verticalArrangement = Arrangement.spacedBy(Spacings.l),
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .animateAppearanceAlpha(2000)
                ) {
                    Spacer(modifier = Modifier.weight(1.0f))
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                Spacer(modifier = Modifier.weight(1.0f))

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

                Spacer(modifier = Modifier.weight(1.0f))

                Button(
                    modifier = Modifier
                        .animateAppearanceAlpha(2000)
                        .fillMaxWidth(),
                    onClick = { /*TODO*/ }
                ) {
                    Text(
                        text = "Let's get started",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen()
}