package org.overengineer.talelistener.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color


val TLOrange = Color(0xFFFF6F3F)
val Dark = Color(0xFF1C1B1F)
val LightBackground = Color(0xFFFAFAFA)

val DarkColorScheme = darkColorScheme(
    primary = TLOrange,
    onPrimary = Dark
)

val LightColorScheme = lightColorScheme(
    primary = TLOrange,
    secondary = Dark,
    tertiary = TLOrange,
    background = LightBackground,
    surface = LightBackground,
    surfaceContainer = Color(0xFFEEEEEE),
)
