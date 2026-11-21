package com.lensclick.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LensColorScheme = lightColorScheme(
    primary = LensBlack,
    onPrimary = LensOffWhite,
    secondary = LensGold,
    onSecondary = LensWhite,
    background = LensOffWhite,
    onBackground = LensBlack,
    surface = LensWhite,
    onSurface = LensBlack,
    surfaceVariant = LensStone,
    onSurfaceVariant = LensMuted,
    outline = LensStone
)

private val LensShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun LensClickAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LensColorScheme,
        typography = Typography,
        shapes = LensShapes,
        content = content
    )
}
