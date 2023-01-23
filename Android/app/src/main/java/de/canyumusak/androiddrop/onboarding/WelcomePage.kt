package de.canyumusak.androiddrop.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.canyumusak.androiddrop.theme.Spacings
import de.canyumusak.androiddrop.ui.extension.animateAppearanceAlpha
import kotlinx.coroutines.delay


@Composable
fun WelcomePage() {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(Spacings.l)
            .wrapContentHeight()
            .animateAppearanceAlpha(500),
        verticalArrangement = Arrangement.spacedBy(Spacings.l),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateAppearanceAlpha(1500)
        ) {
            Spacer(modifier = Modifier.weight(1.0f))
            OutlinedButton(onClick = {
                //                    skipRequested()
            }) {
                Text(
                    text = "Skip Setup",
                )

            }
        }
        Spacer(modifier = Modifier.weight(1.0f))
        WelcomeText()
        PrepareMac()
        Spacer(modifier = Modifier.weight(1.0f))
        Button(
            modifier = Modifier
                .animateAppearanceAlpha(1500)
                .fillMaxWidth(),
            onClick = {
//                nextRequested()
            }
        ) {
            Text(
                text = "Start Setup",
                style = MaterialTheme.typography.labelLarge
            )
        }
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
private fun PrepareMac() {
    val inspectorMode = LocalInspectionMode.current
    var visible by remember {
        mutableStateOf(inspectorMode)
    }
    LaunchedEffect(true) {
        delay(2000)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically)
    ) {
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
    OnboardingScreen(OnboardingPages.Welcome, {}) {}
}