package com.chandsharma.kajugoals.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Graphite,
    onPrimary = Paper,
    primaryContainer = Glacier,
    onPrimaryContainer = Graphite,
    secondary = Color(0xFF426675),
    onSecondary = Paper,
    secondaryContainer = MineralMint,
    onSecondaryContainer = Graphite,
    tertiary = Color(0xFF795E55),
    onTertiary = Paper,
    tertiaryContainer = SoftClay,
    onTertiaryContainer = Graphite,
    background = Porcelain,
    onBackground = Graphite,
    surface = Paper,
    onSurface = Graphite,
    surfaceVariant = Fog,
    onSurfaceVariant = Slate,
    outline = Color(0xFF747C80),
    outlineVariant = Hairline,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE7ECEE),
    onPrimary = Graphite,
    primaryContainer = Color(0xFF33454C),
    onPrimaryContainer = Color(0xFFE5F4F8),
    secondary = Color(0xFFB8DCE6),
    onSecondary = Color(0xFF17303A),
    secondaryContainer = Color(0xFF304A40),
    onSecondaryContainer = Color(0xFFE0F0D4),
    tertiary = Color(0xFFF1C5B5),
    onTertiary = Color(0xFF422B24),
    background = Color(0xFF111617),
    onBackground = Color(0xFFE7ECEE),
    surface = Color(0xFF181E20),
    onSurface = Color(0xFFE7ECEE),
    surfaceVariant = Color(0xFF283033),
    onSurfaceVariant = Color(0xFFC2C9CC),
    outline = Color(0xFF90999D),
    outlineVariant = Color(0xFF3B4447),
)

@Composable
fun KajuGoalsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val extended = if (darkTheme) {
        KajuExtendedColors(
            glacier = Color(0xFF31505B),
            mint = Color(0xFF3D5236),
            clay = Color(0xFF62493F),
            periwinkle = Color(0xFF48465D),
            graphite = Color(0xFFE7ECEE),
        )
    } else {
        KajuExtendedColors(Glacier, MineralMint, SoftClay, Periwinkle, Graphite)
    }

    CompositionLocalProvider(LocalKajuExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = KajuTypography,
            content = content,
        )
    }
}

object KajuTheme {
    val extendedColors: KajuExtendedColors
        @Composable get() = LocalKajuExtendedColors.current
}
