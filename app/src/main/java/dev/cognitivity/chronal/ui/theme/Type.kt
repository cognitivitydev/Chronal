/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2026  cognitivity
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.cognitivity.chronal.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import dev.cognitivity.chronal.R

@OptIn(ExperimentalTextApi::class)
val RoundedFontFamily = FontFamily( // only appears when sdk >= 26
    Font(
        R.font.google_sans_flex_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("ROND", 100f)
        )
    )
)

private val defaultTypography = Typography()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val AppTypography = Typography(
    displayLargeEmphasized = defaultTypography.displayLargeEmphasized.copy(fontFamily = RoundedFontFamily),
    displayMediumEmphasized = defaultTypography.displayMediumEmphasized.copy(fontFamily = RoundedFontFamily),
    displaySmallEmphasized = defaultTypography.displaySmallEmphasized.copy(fontFamily = RoundedFontFamily),
    headlineLargeEmphasized = defaultTypography.headlineLargeEmphasized.copy(fontFamily = RoundedFontFamily),
    headlineMediumEmphasized = defaultTypography.headlineMediumEmphasized.copy(fontFamily = RoundedFontFamily),
    headlineSmallEmphasized = defaultTypography.headlineSmallEmphasized.copy(fontFamily = RoundedFontFamily),
    titleLargeEmphasized = defaultTypography.titleLargeEmphasized.copy(fontFamily = RoundedFontFamily),
    titleMediumEmphasized = defaultTypography.titleMediumEmphasized.copy(fontFamily = RoundedFontFamily),
    titleSmallEmphasized = defaultTypography.titleSmallEmphasized.copy(fontFamily = RoundedFontFamily),
    bodyLargeEmphasized = defaultTypography.bodyLargeEmphasized.copy(fontFamily = RoundedFontFamily),
    bodyMediumEmphasized = defaultTypography.bodyMediumEmphasized.copy(fontFamily = RoundedFontFamily),
    bodySmallEmphasized = defaultTypography.bodySmallEmphasized.copy(fontFamily = RoundedFontFamily),
    labelLargeEmphasized = defaultTypography.labelLargeEmphasized.copy(fontFamily = RoundedFontFamily),
    labelMediumEmphasized = defaultTypography.labelMediumEmphasized.copy(fontFamily = RoundedFontFamily),
    labelSmallEmphasized = defaultTypography.labelSmallEmphasized.copy(fontFamily = RoundedFontFamily)
)
