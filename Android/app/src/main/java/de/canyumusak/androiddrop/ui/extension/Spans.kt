package de.canyumusak.androiddrop.ui.extension

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle

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