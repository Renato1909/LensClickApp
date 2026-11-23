package com.lensclick.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lensclick.app.R

private val EditorialSerif = FontFamily(
    Font(R.font.playfair_regular),
    Font(R.font.playfair_italic, style = FontStyle.Italic)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontSize = 41.sp,
        lineHeight = 47.sp,
        letterSpacing = (-1.4).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.65).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = EditorialSerif,
        fontSize = 27.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.4).sp
    ),
    titleLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontSize = 23.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.15.sp
    )
)
