package de.canyumusak.androiddrop.extension

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import de.canyumusak.androiddrop.R

@Composable
fun AnnotatedString.Builder.primaryColor(content: String) {
    withStyle(SpanStyle(MaterialTheme.colorScheme.primary)) {
        append(content)
    }
}

@Composable
fun AnnotatedString.Builder.secondaryColor(content: String) {
    withStyle(SpanStyle(MaterialTheme.colorScheme.secondary)) {
        append(content)
    }
}

@Composable
fun highlightedStringResource(id: Int): AnnotatedString {
    val titlePart = stringResource(id = id)
        .split("**")

    return buildAnnotatedString {
        titlePart.forEachIndexed { index, string ->
            if (index % 2 == 0) {
                secondaryColor(string)
            } else {
                primaryColor(string)
            }
        }
    }
}
