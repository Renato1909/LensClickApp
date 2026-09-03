package com.example.lensclickapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LensColorScheme = lightColorScheme(
    primary = LensBlack,
    onPrimary = LensOffWhite,
    background = LensOffWhite,
    onBackground = LensBlack,
    surface = LensWhite,
    onSurface = LensBlack,
    surfaceVariant = LensStone,
    onSurfaceVariant = LensMuted,
    outline = LensStone
)

@Composable
fun LensClickAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LensColorScheme,
        typography = Typography,
        content = content
    )
}
