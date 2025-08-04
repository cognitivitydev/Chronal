package dev.cognitivity.chronal.ui.theme

import androidx.glance.material3.ColorProviders

object AquaGlanceTheme {
    val LightColors = lightScheme
    val DarkColors = darkScheme

    val colors = ColorProviders(
        light = LightColors,
        dark = DarkColors
    )
}