package de.canyumusak.androiddrop.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import de.canyumusak.androiddrop.R

private val fontFamily = FontFamily(
    Font(R.font.product_sans_light, FontWeight.Light),
    Font(R.font.product_sans_regular, FontWeight.W400),
    Font(R.font.product_sans_regular, FontWeight.Normal),
    Font(R.font.product_sans_medium, FontWeight.Medium),
    Font(R.font.product_sans_bold, FontWeight.Bold),
    Font(R.font.product_sans_black, FontWeight.Black),
)

private val defaultTextStyle = TextStyle(
    fontFamily = fontFamily,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    ),
    platformStyle = PlatformTextStyle(
        includeFontPadding = false
    )
)


val Typography = Typography(
    displayLarge = defaultTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 50.sp,
        lineHeight = 80.0.sp,
        letterSpacing = 2.0.sp,
    ),
    displayMedium = defaultTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 45.sp,
        lineHeight = 52.0.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = defaultTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 36.sp,
        lineHeight = 44.0.sp,
        letterSpacing = 0.0.sp,
    ),
    headlineLarge = defaultTextStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 40.0.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = defaultTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.0.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = defaultTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.0.sp,
        letterSpacing = 0.0.sp,
    ),
    titleLarge = defaultTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.0.sp,
        letterSpacing = 0.0.sp,
    ),
    titleMedium = defaultTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 22.0.sp,
        letterSpacing = 0.2.sp,
    ),
    titleSmall = defaultTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.0.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = defaultTextStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.0.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = defaultTextStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.0.sp,
        letterSpacing = 0.2.sp,
    ),
    bodySmall = defaultTextStyle.copy(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = defaultTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.0.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = defaultTextStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.0.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = defaultTextStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)