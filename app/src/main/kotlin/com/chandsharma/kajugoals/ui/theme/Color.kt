package com.chandsharma.kajugoals.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Fog = Color(0xFFDCE5E8)
val Porcelain = Color(0xFFF7F8F7)
val Paper = Color(0xFFFFFFFF)
val Graphite = Color(0xFF111416)
val Slate = Color(0xFF50575B)
val Glacier = Color(0xFFB8DCE6)
val MineralMint = Color(0xFFCFE2C0)
val SoftClay = Color(0xFFE9BCAA)
val Periwinkle = Color(0xFFC9C7E2)
val Hairline = Color(0xFFD9DEDF)

@Immutable
data class KajuExtendedColors(
    val glacier: Color,
    val mint: Color,
    val clay: Color,
    val periwinkle: Color,
    val graphite: Color,
)

val LocalKajuExtendedColors = staticCompositionLocalOf {
    KajuExtendedColors(
        glacier = Glacier,
        mint = MineralMint,
        clay = SoftClay,
        periwinkle = Periwinkle,
        graphite = Graphite,
    )
}
