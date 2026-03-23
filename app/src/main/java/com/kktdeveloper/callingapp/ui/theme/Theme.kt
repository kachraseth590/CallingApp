package com.kktdeveloper.callingapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


val GreenAccept = Color(0xFF4CAF50)
val RedDecline  = Color(0xFFF44336)
val BlueActive  = Color(0xFF1976D2)
val DarkBg      = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val CardDark    = Color(0xFF2C2C2C)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0B0)
val DialKeyBg   = Color(0xFF2C2C2C)
val DialKeyText = Color(0xFFFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary        = BlueActive,
    secondary      = GreenAccept,
    tertiary       = RedDecline,
    background     = DarkBg,
    surface        = SurfaceDark,
    onPrimary      = Color.White,
    onBackground   = TextPrimary,
    onSurface      = TextPrimary
)

@Composable
fun CallingAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content     = content
    )
}