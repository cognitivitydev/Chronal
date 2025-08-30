/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2025  cognitivity
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

package dev.cognitivity.chronal.ui.theme.colors

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.cognitivity.chronal.ColorScheme

private val primaryLight = Color(0xFF804D7A)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFFFD7F5)
private val onPrimaryContainerLight = Color(0xFF653661)
private val secondaryLight = Color(0xFF6E5869)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFF7DAEF)
private val onSecondaryContainerLight = Color(0xFF554151)
private val tertiaryLight = Color(0xFF825345)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFFFDBD1)
private val onTertiaryContainerLight = Color(0xFF663C2F)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFFFF7F9)
private val onBackgroundLight = Color(0xFF201A1E)
private val surfaceLight = Color(0xFFFFF7F9)
private val onSurfaceLight = Color(0xFF201A1E)
private val surfaceVariantLight = Color(0xFFEEDEE7)
private val onSurfaceVariantLight = Color(0xFF4E444B)
private val outlineLight = Color(0xFF80747C)
private val outlineVariantLight = Color(0xFFD1C2CB)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF352E33)
private val inverseOnSurfaceLight = Color(0xFFFAEDF4)
private val inversePrimaryLight = Color(0xFFF1B3E6)
private val surfaceDimLight = Color(0xFFE3D7DD)
private val surfaceBrightLight = Color(0xFFFFF7F9)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFFDF0F7)
private val surfaceContainerLight = Color(0xFFF7EBF1)
private val surfaceContainerHighLight = Color(0xFFF1E5EB)
private val surfaceContainerHighestLight = Color(0xFFECDFE5)

private val primaryLightMediumContrast = Color(0xFF53254F)
private val onPrimaryLightMediumContrast = Color(0xFFFFFFFF)
private val primaryContainerLightMediumContrast = Color(0xFF905B89)
private val onPrimaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val secondaryLightMediumContrast = Color(0xFF433040)
private val onSecondaryLightMediumContrast = Color(0xFFFFFFFF)
private val secondaryContainerLightMediumContrast = Color(0xFF7D6678)
private val onSecondaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val tertiaryLightMediumContrast = Color(0xFF532C20)
private val onTertiaryLightMediumContrast = Color(0xFFFFFFFF)
private val tertiaryContainerLightMediumContrast = Color(0xFF926153)
private val onTertiaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val errorLightMediumContrast = Color(0xFF740006)
private val onErrorLightMediumContrast = Color(0xFFFFFFFF)
private val errorContainerLightMediumContrast = Color(0xFFCF2C27)
private val onErrorContainerLightMediumContrast = Color(0xFFFFFFFF)
private val backgroundLightMediumContrast = Color(0xFFFFF7F9)
private val onBackgroundLightMediumContrast = Color(0xFF201A1E)
private val surfaceLightMediumContrast = Color(0xFFFFF7F9)
private val onSurfaceLightMediumContrast = Color(0xFF150F14)
private val surfaceVariantLightMediumContrast = Color(0xFFEEDEE7)
private val onSurfaceVariantLightMediumContrast = Color(0xFF3D333A)
private val outlineLightMediumContrast = Color(0xFF5A4F57)
private val outlineVariantLightMediumContrast = Color(0xFF756A71)
private val scrimLightMediumContrast = Color(0xFF000000)
private val inverseSurfaceLightMediumContrast = Color(0xFF352E33)
private val inverseOnSurfaceLightMediumContrast = Color(0xFFFAEDF4)
private val inversePrimaryLightMediumContrast = Color(0xFFF1B3E6)
private val surfaceDimLightMediumContrast = Color(0xFFCFC3C9)
private val surfaceBrightLightMediumContrast = Color(0xFFFFF7F9)
private val surfaceContainerLowestLightMediumContrast = Color(0xFFFFFFFF)
private val surfaceContainerLowLightMediumContrast = Color(0xFFFDF0F7)
private val surfaceContainerLightMediumContrast = Color(0xFFF1E5EB)
private val surfaceContainerHighLightMediumContrast = Color(0xFFE6DAE0)
private val surfaceContainerHighestLightMediumContrast = Color(0xFFDACED5)

private val primaryLightHighContrast = Color(0xFF471B44)
private val onPrimaryLightHighContrast = Color(0xFFFFFFFF)
private val primaryContainerLightHighContrast = Color(0xFF683863)
private val onPrimaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val secondaryLightHighContrast = Color(0xFF382635)
private val onSecondaryLightHighContrast = Color(0xFFFFFFFF)
private val secondaryContainerLightHighContrast = Color(0xFF574353)
private val onSecondaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val tertiaryLightHighContrast = Color(0xFF472217)
private val onTertiaryLightHighContrast = Color(0xFFFFFFFF)
private val tertiaryContainerLightHighContrast = Color(0xFF693E31)
private val onTertiaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val errorLightHighContrast = Color(0xFF600004)
private val onErrorLightHighContrast = Color(0xFFFFFFFF)
private val errorContainerLightHighContrast = Color(0xFF98000A)
private val onErrorContainerLightHighContrast = Color(0xFFFFFFFF)
private val backgroundLightHighContrast = Color(0xFFFFF7F9)
private val onBackgroundLightHighContrast = Color(0xFF201A1E)
private val surfaceLightHighContrast = Color(0xFFFFF7F9)
private val onSurfaceLightHighContrast = Color(0xFF000000)
private val surfaceVariantLightHighContrast = Color(0xFFEEDEE7)
private val onSurfaceVariantLightHighContrast = Color(0xFF000000)
private val outlineLightHighContrast = Color(0xFF322930)
private val outlineVariantLightHighContrast = Color(0xFF50464D)
private val scrimLightHighContrast = Color(0xFF000000)
private val inverseSurfaceLightHighContrast = Color(0xFF352E33)
private val inverseOnSurfaceLightHighContrast = Color(0xFFFFFFFF)
private val inversePrimaryLightHighContrast = Color(0xFFF1B3E6)
private val surfaceDimLightHighContrast = Color(0xFFC1B6BC)
private val surfaceBrightLightHighContrast = Color(0xFFFFF7F9)
private val surfaceContainerLowestLightHighContrast = Color(0xFFFFFFFF)
private val surfaceContainerLowLightHighContrast = Color(0xFFFAEDF4)
private val surfaceContainerLightHighContrast = Color(0xFFECDFE5)
private val surfaceContainerHighLightHighContrast = Color(0xFFDDD1D7)
private val surfaceContainerHighestLightHighContrast = Color(0xFFCFC3C9)

private val primaryDark = Color(0xFFF1B3E6)
private val onPrimaryDark = Color(0xFF4C1F49)
private val primaryContainerDark = Color(0xFF653661)
private val onPrimaryContainerDark = Color(0xFFFFD7F5)
private val secondaryDark = Color(0xFFDABFD2)
private val onSecondaryDark = Color(0xFF3D2B3A)
private val secondaryContainerDark = Color(0xFF554151)
private val onSecondaryContainerDark = Color(0xFFF7DAEF)
private val tertiaryDark = Color(0xFFF5B8A7)
private val onTertiaryDark = Color(0xFF4C261B)
private val tertiaryContainerDark = Color(0xFF663C2F)
private val onTertiaryContainerDark = Color(0xFFFFDBD1)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF171216)
private val onBackgroundDark = Color(0xFFECDFE5)
private val surfaceDark = Color(0xFF171216)
private val onSurfaceDark = Color(0xFFECDFE5)
private val surfaceVariantDark = Color(0xFF4E444B)
private val onSurfaceVariantDark = Color(0xFFD1C2CB)
private val outlineDark = Color(0xFF9A8D95)
private val outlineVariantDark = Color(0xFF4E444B)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFECDFE5)
private val inverseOnSurfaceDark = Color(0xFF352E33)
private val inversePrimaryDark = Color(0xFF804D7A)
private val surfaceDimDark = Color(0xFF171216)
private val surfaceBrightDark = Color(0xFF3E373C)
private val surfaceContainerLowestDark = Color(0xFF120D11)
private val surfaceContainerLowDark = Color(0xFF201A1E)
private val surfaceContainerDark = Color(0xFF241E22)
private val surfaceContainerHighDark = Color(0xFF2F282D)
private val surfaceContainerHighestDark = Color(0xFF3A3338)

private val primaryDarkMediumContrast = Color(0xFFFFCDF4)
private val onPrimaryDarkMediumContrast = Color(0xFF3F143D)
private val primaryContainerDarkMediumContrast = Color(0xFFB77EAE)
private val onPrimaryContainerDarkMediumContrast = Color(0xFF000000)
private val secondaryDarkMediumContrast = Color(0xFFF1D4E8)
private val onSecondaryDarkMediumContrast = Color(0xFF31202F)
private val secondaryContainerDarkMediumContrast = Color(0xFFA2899C)
private val onSecondaryContainerDarkMediumContrast = Color(0xFF000000)
private val tertiaryDarkMediumContrast = Color(0xFFFFD2C6)
private val onTertiaryDarkMediumContrast = Color(0xFF3F1B11)
private val tertiaryContainerDarkMediumContrast = Color(0xFFBA8474)
private val onTertiaryContainerDarkMediumContrast = Color(0xFF000000)
private val errorDarkMediumContrast = Color(0xFFFFD2CC)
private val onErrorDarkMediumContrast = Color(0xFF540003)
private val errorContainerDarkMediumContrast = Color(0xFFFF5449)
private val onErrorContainerDarkMediumContrast = Color(0xFF000000)
private val backgroundDarkMediumContrast = Color(0xFF171216)
private val onBackgroundDarkMediumContrast = Color(0xFFECDFE5)
private val surfaceDarkMediumContrast = Color(0xFF171216)
private val onSurfaceDarkMediumContrast = Color(0xFFFFFFFF)
private val surfaceVariantDarkMediumContrast = Color(0xFF4E444B)
private val onSurfaceVariantDarkMediumContrast = Color(0xFFE7D8E1)
private val outlineDarkMediumContrast = Color(0xFFBCAEB7)
private val outlineVariantDarkMediumContrast = Color(0xFF9A8D95)
private val scrimDarkMediumContrast = Color(0xFF000000)
private val inverseSurfaceDarkMediumContrast = Color(0xFFECDFE5)
private val inverseOnSurfaceDarkMediumContrast = Color(0xFF2F282D)
private val inversePrimaryDarkMediumContrast = Color(0xFF673762)
private val surfaceDimDarkMediumContrast = Color(0xFF171216)
private val surfaceBrightDarkMediumContrast = Color(0xFF4A4247)
private val surfaceContainerLowestDarkMediumContrast = Color(0xFF0B060A)
private val surfaceContainerLowDarkMediumContrast = Color(0xFF221C20)
private val surfaceContainerDarkMediumContrast = Color(0xFF2D262B)
private val surfaceContainerHighDarkMediumContrast = Color(0xFF383135)
private val surfaceContainerHighestDarkMediumContrast = Color(0xFF433C40)

private val primaryDarkHighContrast = Color(0xFFFFEAF7)
private val onPrimaryDarkHighContrast = Color(0xFF000000)
private val primaryContainerDarkHighContrast = Color(0xFFEDAFE2)
private val onPrimaryContainerDarkHighContrast = Color(0xFF1C001C)
private val secondaryDarkHighContrast = Color(0xFFFFEAF7)
private val onSecondaryDarkHighContrast = Color(0xFF000000)
private val secondaryContainerDarkHighContrast = Color(0xFFD6BBCE)
private val onSecondaryContainerDarkHighContrast = Color(0xFF150613)
private val tertiaryDarkHighContrast = Color(0xFFFFECE7)
private val onTertiaryDarkHighContrast = Color(0xFF000000)
private val tertiaryContainerDarkHighContrast = Color(0xFFF1B4A3)
private val onTertiaryContainerDarkHighContrast = Color(0xFF1D0300)
private val errorDarkHighContrast = Color(0xFFFFECE9)
private val onErrorDarkHighContrast = Color(0xFF000000)
private val errorContainerDarkHighContrast = Color(0xFFFFAEA4)
private val onErrorContainerDarkHighContrast = Color(0xFF220001)
private val backgroundDarkHighContrast = Color(0xFF171216)
private val onBackgroundDarkHighContrast = Color(0xFFECDFE5)
private val surfaceDarkHighContrast = Color(0xFF171216)
private val onSurfaceDarkHighContrast = Color(0xFFFFFFFF)
private val surfaceVariantDarkHighContrast = Color(0xFF4E444B)
private val onSurfaceVariantDarkHighContrast = Color(0xFFFFFFFF)
private val outlineDarkHighContrast = Color(0xFFFCECF5)
private val outlineVariantDarkHighContrast = Color(0xFFCDBFC7)
private val scrimDarkHighContrast = Color(0xFF000000)
private val inverseSurfaceDarkHighContrast = Color(0xFFECDFE5)
private val inverseOnSurfaceDarkHighContrast = Color(0xFF000000)
private val inversePrimaryDarkHighContrast = Color(0xFF673762)
private val surfaceDimDarkHighContrast = Color(0xFF171216)
private val surfaceBrightDarkHighContrast = Color(0xFF564E53)
private val surfaceContainerLowestDarkHighContrast = Color(0xFF000000)
private val surfaceContainerLowDarkHighContrast = Color(0xFF241E22)
private val surfaceContainerDarkHighContrast = Color(0xFF352E33)
private val surfaceContainerHighDarkHighContrast = Color(0xFF41393E)
private val surfaceContainerHighestDarkHighContrast = Color(0xFF4C454A)

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val mediumContrastLightColorScheme = lightColorScheme(
    primary = primaryLightMediumContrast,
    onPrimary = onPrimaryLightMediumContrast,
    primaryContainer = primaryContainerLightMediumContrast,
    onPrimaryContainer = onPrimaryContainerLightMediumContrast,
    secondary = secondaryLightMediumContrast,
    onSecondary = onSecondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLightMediumContrast,
    onSecondaryContainer = onSecondaryContainerLightMediumContrast,
    tertiary = tertiaryLightMediumContrast,
    onTertiary = onTertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLightMediumContrast,
    onTertiaryContainer = onTertiaryContainerLightMediumContrast,
    error = errorLightMediumContrast,
    onError = onErrorLightMediumContrast,
    errorContainer = errorContainerLightMediumContrast,
    onErrorContainer = onErrorContainerLightMediumContrast,
    background = backgroundLightMediumContrast,
    onBackground = onBackgroundLightMediumContrast,
    surface = surfaceLightMediumContrast,
    onSurface = onSurfaceLightMediumContrast,
    surfaceVariant = surfaceVariantLightMediumContrast,
    onSurfaceVariant = onSurfaceVariantLightMediumContrast,
    outline = outlineLightMediumContrast,
    outlineVariant = outlineVariantLightMediumContrast,
    scrim = scrimLightMediumContrast,
    inverseSurface = inverseSurfaceLightMediumContrast,
    inverseOnSurface = inverseOnSurfaceLightMediumContrast,
    inversePrimary = inversePrimaryLightMediumContrast,
    surfaceDim = surfaceDimLightMediumContrast,
    surfaceBright = surfaceBrightLightMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = surfaceContainerLowLightMediumContrast,
    surfaceContainer = surfaceContainerLightMediumContrast,
    surfaceContainerHigh = surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestLightMediumContrast,
)

private val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
    surfaceDim = surfaceDimLightHighContrast,
    surfaceBright = surfaceBrightLightHighContrast,
    surfaceContainerLowest = surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = surfaceContainerLowLightHighContrast,
    surfaceContainer = surfaceContainerLightHighContrast,
    surfaceContainerHigh = surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = surfaceContainerHighestLightHighContrast,
)

private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkMediumContrast,
    onPrimary = onPrimaryDarkMediumContrast,
    primaryContainer = primaryContainerDarkMediumContrast,
    onPrimaryContainer = onPrimaryContainerDarkMediumContrast,
    secondary = secondaryDarkMediumContrast,
    onSecondary = onSecondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDarkMediumContrast,
    onSecondaryContainer = onSecondaryContainerDarkMediumContrast,
    tertiary = tertiaryDarkMediumContrast,
    onTertiary = onTertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = onTertiaryContainerDarkMediumContrast,
    error = errorDarkMediumContrast,
    onError = onErrorDarkMediumContrast,
    errorContainer = errorContainerDarkMediumContrast,
    onErrorContainer = onErrorContainerDarkMediumContrast,
    background = backgroundDarkMediumContrast,
    onBackground = onBackgroundDarkMediumContrast,
    surface = surfaceDarkMediumContrast,
    onSurface = onSurfaceDarkMediumContrast,
    surfaceVariant = surfaceVariantDarkMediumContrast,
    onSurfaceVariant = onSurfaceVariantDarkMediumContrast,
    outline = outlineDarkMediumContrast,
    outlineVariant = outlineVariantDarkMediumContrast,
    scrim = scrimDarkMediumContrast,
    inverseSurface = inverseSurfaceDarkMediumContrast,
    inverseOnSurface = inverseOnSurfaceDarkMediumContrast,
    inversePrimary = inversePrimaryDarkMediumContrast,
    surfaceDim = surfaceDimDarkMediumContrast,
    surfaceBright = surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = surfaceContainerLowDarkMediumContrast,
    surfaceContainer = surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkMediumContrast,
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
    surfaceDim = surfaceDimDarkHighContrast,
    surfaceBright = surfaceBrightDarkHighContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = surfaceContainerLowDarkHighContrast,
    surfaceContainer = surfaceContainerDarkHighContrast,
    surfaceContainerHigh = surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkHighContrast,
)

@Composable
fun PurpleTheme(
    darkTheme: Boolean,
    contrast: ColorScheme.Contrast = ColorScheme.Contrast.LOW,
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        darkTheme -> {
            when (contrast) {
                ColorScheme.Contrast.MEDIUM -> mediumContrastDarkColorScheme
                ColorScheme.Contrast.HIGH -> highContrastDarkColorScheme
                else -> darkScheme
            }
        }
        else -> {
            when (contrast) {
                ColorScheme.Contrast.MEDIUM -> mediumContrastLightColorScheme
                ColorScheme.Contrast.HIGH -> highContrastLightColorScheme
                else -> lightScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}