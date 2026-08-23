package com.pyth0nkod3r.fabricfocus.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Ink = Color(0xFF101426)
private val Surface = Color(0xFF191F38)
private val Indigo = Color(0xFF6F7BF7)
private val Cyan = Color(0xFF55D7E4)
private val Mint = Color(0xFF78E0B2)
private val Text = Color(0xFFF2F4FF)
private val DarkScheme = darkColorScheme(primary=Indigo, secondary=Cyan, tertiary=Mint, background=Ink, surface=Surface, onBackground=Text, onSurface=Text)
private val LightScheme = lightColorScheme(primary=Color(0xFF4054C8), secondary=Color(0xFF007E8B), tertiary=Color(0xFF147A51), background=Color(0xFFF9F9FF), surface=Color.White)

@Composable fun FabricFocusTheme(dark: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, typography = Typography(), content = content)
}
