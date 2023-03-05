package de.canyumusak.androiddrop.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import de.canyumusak.androiddrop.AnDropClient
import de.canyumusak.androiddrop.DiscoveryViewModel
import de.canyumusak.androiddrop.R
import de.canyumusak.androiddrop.WifiState
import de.canyumusak.androiddrop.theme.AnDropTheme
import de.canyumusak.androiddrop.ui.ScanScreen
import de.canyumusak.androiddrop.extension.ScaleIn
import de.canyumusak.androiddrop.extension.highlightedStringResource
import de.canyumusak.androiddrop.extension.rememberBoolean

@Composable
fun CheckSetupPage(
    nextRequested: () -> Unit,
    skipRequested: () -> Unit,
    viewModel: DiscoveryViewModel = viewModel(),
) {
    val list by viewModel.clients.collectAsState()
    val wifiState by viewModel.wifiState.collectAsState()
    DisposableEffect(viewModel) {
        viewModel.discoverClients()
        onDispose { viewModel.endDiscovery() }
    }
    CheckSetupPage(
        list = list,
        wifiState = wifiState,
        nextRequested = {
            OnboardingEvents.checkSetupComplete(list.size)
            nextRequested()
        },
        skipRequested = skipRequested
    )
}

@Composable
fun CheckSetupPage(
    list: List<AnDropClient>,
    wifiState: WifiState,
    nextRequested: () -> Unit,
    skipRequested: () -> Unit
) {
    val showFirstHint = rememberBoolean(
        initialValue = false,
        targetValue = true,
        delayMs = 10_000L
    )
    val buttonLabel = when {
        list.isNotEmpty() -> stringResource(id = R.string.onboarding_check_confirm_computer)
        else -> stringResource(id = R.string.onboarding_check_continue_anyway)
    }

    OnboardingScaffold(
        animateEntry = false,
        nextText = buttonLabel,
        nextRequested = nextRequested,
        skipRequested = skipRequested
    ) {
        Title()
        ScaleIn(delayTacts = 2) {
            ScanScreen(
                list = list,
                permissionRequested = {},
                wifiDisabled = wifiState == WifiState.Disabled,
                permissionMissing = false,
                scanForDemoPurposes = true,
                unsupportedFileType = false,
                clientSelected = {},
                modifier = Modifier,
            )
        }
        Hint(showFirstHint, list)
    }
}

@Composable
private fun Title() {
    Text(
        text = highlightedStringResource(R.string.onboarding_check_title),
        style = MaterialTheme.typography.displaySmall,
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Hint(showFirstHint: Boolean, list: List<AnDropClient>) {
    AnimatedVisibility(
        visible = showFirstHint && list.isEmpty(),
        enter = fadeIn() + expandVertically() + scaleIn(),
        exit = fadeOut() + shrinkVertically() + scaleOut(),
    ) {
        Text(
            text = stringResource(R.string.onboarding_check_hint),
            style = MaterialTheme.typography.headlineSmall,
        )
    }

    AnimatedVisibility(
        visible = list.isNotEmpty(),
        enter = fadeIn() + expandVertically() + scaleIn(),
        exit = fadeOut() + shrinkVertically() + scaleOut(),
    ) {
        val text: AnnotatedString = when (list.size) {
            1 -> highlightedStringResource(R.string.onboarding_check_single_result_question)
            else -> highlightedStringResource(R.string.onboarding_check_multiple_result_question)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}


@Preview
@Composable
private fun CheckSetupPagePreview() {
    AnDropTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            CheckSetupPage(
                list = listOf(
                    AnDropClient("Can's MacBook Pro"),
                    AnDropClient("Adrian's MacBook Pro"),
                ),
                wifiState = WifiState.Disabled,
                nextRequested = {},
                skipRequested = {},
            )
        }
    }
}