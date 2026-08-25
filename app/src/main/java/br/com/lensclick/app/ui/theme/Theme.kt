package br.com.lensclick.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta extraída do site LensClick (tema escuro roxo/lilás)
val Bg = Color(0xFF12101A)
val Surface = Color(0xFF1C1826)
val SurfaceHigh = Color(0xFF272133)
val Primary = Color(0xFF7C3AED)
val PrimaryLight = Color(0xFFA78BFA)
val TextMain = Color(0xFFEDE9FE)
val TextDim = Color(0xFF9B93AC)
val Danger = Color(0xFFF87171)
val Success = Color(0xFF4ADE80)

private val LensClickColors = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = PrimaryLight,
    background = Bg,
    onBackground = TextMain,
    surface = Surface,
    onSurface = TextMain,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = TextDim,
    error = Danger,
)

@Composable
fun LensClickTheme(content: @Composable () -> Unit) {
    // O app segue o tema escuro da marca em qualquer modo do sistema
    MaterialTheme(
        colorScheme = LensClickColors,
        content = content,
    )
}
