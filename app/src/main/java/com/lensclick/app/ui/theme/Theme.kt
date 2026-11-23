package com.lensclick.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LensColorScheme = lightColorScheme(
    primary = LensCoffee,
    onPrimary = LensOnCoffee,
    primaryContainer = LensSand,
    onPrimaryContainer = LensInk,
    inversePrimary = LensSand,
    secondary = LensCoffee,
    onSecondary = LensOnCoffee,
    secondaryContainer = LensSand,
    onSecondaryContainer = LensInk,
    tertiary = LensCoffee,
    onTertiary = LensOnCoffee,
    tertiaryContainer = LensSand,
    onTertiaryContainer = LensInk,
    background = LensPaper,
    onBackground = LensInk,
    surface = LensSurface,
    onSurface = LensInk,
    surfaceVariant = LensSand,
    onSurfaceVariant = LensMuted,
    inverseSurface = LensInk,
    inverseOnSurface = LensPaper,
    outline = LensFieldBorder,
    outlineVariant = LensLine
)

private val LensShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun LensClickAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LensColorScheme,
        typography = Typography,
        shapes = LensShapes,
        content = content
    )
}
