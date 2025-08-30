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
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders
import dev.cognitivity.chronal.ColorScheme

private val primaryLight = Color(0xFF006A6A)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFF9CF1F0)
private val onPrimaryContainerLight = Color(0xFF004F4F)
private val secondaryLight = Color(0xFF4A6363)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFCCE8E7)
private val onSecondaryContainerLight = Color(0xFF324B4B)
private val tertiaryLight = Color(0xFF4B607C)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFD3E4FF)
private val onTertiaryContainerLight = Color(0xFF334863)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFF4FBFA)
private val onBackgroundLight = Color(0xFF161D1D)
private val surfaceLight = Color(0xFFF4FBFA)
private val onSurfaceLight = Color(0xFF161D1D)
private val surfaceVariantLight = Color(0xFFDAE5E4)
private val onSurfaceVariantLight = Color(0xFF3F4948)
private val outlineLight = Color(0xFF6F7979)
private val outlineVariantLight = Color(0xFFBEC9C8)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF2B3231)
private val inverseOnSurfaceLight = Color(0xFFECF2F1)
private val inversePrimaryLight = Color(0xFF80D5D4)
private val surfaceDimLight = Color(0xFFD5DBDA)
private val surfaceBrightLight = Color(0xFFF4FBFA)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFEFF5F4)
private val surfaceContainerLight = Color(0xFFE9EFEE)
private val surfaceContainerHighLight = Color(0xFFE3E9E9)
private val surfaceContainerHighestLight = Color(0xFFDDE4E3)

private val primaryLightMediumContrast = Color(0xFF003D3D)
private val onPrimaryLightMediumContrast = Color(0xFFFFFFFF)
private val primaryContainerLightMediumContrast = Color(0xFF167979)
private val onPrimaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val secondaryLightMediumContrast = Color(0xFF213A3A)
private val onSecondaryLightMediumContrast = Color(0xFFFFFFFF)
private val secondaryContainerLightMediumContrast = Color(0xFF587271)
private val onSecondaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val tertiaryLightMediumContrast = Color(0xFF223751)
private val onTertiaryLightMediumContrast = Color(0xFFFFFFFF)
private val tertiaryContainerLightMediumContrast = Color(0xFF5A6E8B)
private val onTertiaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val errorLightMediumContrast = Color(0xFF740006)
private val onErrorLightMediumContrast = Color(0xFFFFFFFF)
private val errorContainerLightMediumContrast = Color(0xFFCF2C27)
private val onErrorContainerLightMediumContrast = Color(0xFFFFFFFF)
private val backgroundLightMediumContrast = Color(0xFFF4FBFA)
private val onBackgroundLightMediumContrast = Color(0xFF161D1D)
private val surfaceLightMediumContrast = Color(0xFFF4FBFA)
private val onSurfaceLightMediumContrast = Color(0xFF0C1212)
private val surfaceVariantLightMediumContrast = Color(0xFFDAE5E4)
private val onSurfaceVariantLightMediumContrast = Color(0xFF2E3838)
private val outlineLightMediumContrast = Color(0xFF4A5454)
private val outlineVariantLightMediumContrast = Color(0xFF656F6F)
private val scrimLightMediumContrast = Color(0xFF000000)
private val inverseSurfaceLightMediumContrast = Color(0xFF2B3231)
private val inverseOnSurfaceLightMediumContrast = Color(0xFFECF2F1)
private val inversePrimaryLightMediumContrast = Color(0xFF80D5D4)
private val surfaceDimLightMediumContrast = Color(0xFFC1C8C7)
private val surfaceBrightLightMediumContrast = Color(0xFFF4FBFA)
private val surfaceContainerLowestLightMediumContrast = Color(0xFFFFFFFF)
private val surfaceContainerLowLightMediumContrast = Color(0xFFEFF5F4)
private val surfaceContainerLightMediumContrast = Color(0xFFE3E9E9)
private val surfaceContainerHighLightMediumContrast = Color(0xFFD8DEDD)
private val surfaceContainerHighestLightMediumContrast = Color(0xFFCCD3D2)

private val primaryLightHighContrast = Color(0xFF003232)
private val onPrimaryLightHighContrast = Color(0xFFFFFFFF)
private val primaryContainerLightHighContrast = Color(0xFF005252)
private val onPrimaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val secondaryLightHighContrast = Color(0xFF173030)
private val onSecondaryLightHighContrast = Color(0xFFFFFFFF)
private val secondaryContainerLightHighContrast = Color(0xFF344E4D)
private val onSecondaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val tertiaryLightHighContrast = Color(0xFF172D47)
private val onTertiaryLightHighContrast = Color(0xFFFFFFFF)
private val tertiaryContainerLightHighContrast = Color(0xFF364A65)
private val onTertiaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val errorLightHighContrast = Color(0xFF600004)
private val onErrorLightHighContrast = Color(0xFFFFFFFF)
private val errorContainerLightHighContrast = Color(0xFF98000A)
private val onErrorContainerLightHighContrast = Color(0xFFFFFFFF)
private val backgroundLightHighContrast = Color(0xFFF4FBFA)
private val onBackgroundLightHighContrast = Color(0xFF161D1D)
private val surfaceLightHighContrast = Color(0xFFF4FBFA)
private val onSurfaceLightHighContrast = Color(0xFF000000)
private val surfaceVariantLightHighContrast = Color(0xFFDAE5E4)
private val onSurfaceVariantLightHighContrast = Color(0xFF000000)
private val outlineLightHighContrast = Color(0xFF242E2E)
private val outlineVariantLightHighContrast = Color(0xFF414B4B)
private val scrimLightHighContrast = Color(0xFF000000)
private val inverseSurfaceLightHighContrast = Color(0xFF2B3231)
private val inverseOnSurfaceLightHighContrast = Color(0xFFFFFFFF)
private val inversePrimaryLightHighContrast = Color(0xFF80D5D4)
private val surfaceDimLightHighContrast = Color(0xFFB4BAB9)
private val surfaceBrightLightHighContrast = Color(0xFFF4FBFA)
private val surfaceContainerLowestLightHighContrast = Color(0xFFFFFFFF)
private val surfaceContainerLowLightHighContrast = Color(0xFFECF2F1)
private val surfaceContainerLightHighContrast = Color(0xFFDDE4E3)
private val surfaceContainerHighLightHighContrast = Color(0xFFCFD6D5)
private val surfaceContainerHighestLightHighContrast = Color(0xFFC1C8C7)

private val primaryDark = Color(0xFF80D5D4)
private val onPrimaryDark = Color(0xFF003737)
private val primaryContainerDark = Color(0xFF004F4F)
private val onPrimaryContainerDark = Color(0xFF9CF1F0)
private val secondaryDark = Color(0xFFB0CCCB)
private val onSecondaryDark = Color(0xFF1B3534)
private val secondaryContainerDark = Color(0xFF324B4B)
private val onSecondaryContainerDark = Color(0xFFCCE8E7)
private val tertiaryDark = Color(0xFFB3C8E8)
private val onTertiaryDark = Color(0xFF1C314B)
private val tertiaryContainerDark = Color(0xFF334863)
private val onTertiaryContainerDark = Color(0xFFD3E4FF)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF0E1514)
private val onBackgroundDark = Color(0xFFDDE4E3)
private val surfaceDark = Color(0xFF0E1514)
private val onSurfaceDark = Color(0xFFDDE4E3)
private val surfaceVariantDark = Color(0xFF3F4948)
private val onSurfaceVariantDark = Color(0xFFBEC9C8)
private val outlineDark = Color(0xFF889392)
private val outlineVariantDark = Color(0xFF3F4948)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFDDE4E3)
private val inverseOnSurfaceDark = Color(0xFF2B3231)
private val inversePrimaryDark = Color(0xFF006A6A)
private val surfaceDimDark = Color(0xFF0E1514)
private val surfaceBrightDark = Color(0xFF343A3A)
private val surfaceContainerLowestDark = Color(0xFF090F0F)
private val surfaceContainerLowDark = Color(0xFF161D1D)
private val surfaceContainerDark = Color(0xFF1A2121)
private val surfaceContainerHighDark = Color(0xFF252B2B)
private val surfaceContainerHighestDark = Color(0xFF2F3636)

private val primaryDarkMediumContrast = Color(0xFF96EBEA)
private val onPrimaryDarkMediumContrast = Color(0xFF002B2B)
private val primaryContainerDarkMediumContrast = Color(0xFF479E9D)
private val onPrimaryContainerDarkMediumContrast = Color(0xFF000000)
private val secondaryDarkMediumContrast = Color(0xFFC6E2E1)
private val onSecondaryDarkMediumContrast = Color(0xFF102A29)
private val secondaryContainerDarkMediumContrast = Color(0xFF7B9695)
private val onSecondaryContainerDarkMediumContrast = Color(0xFF000000)
private val tertiaryDarkMediumContrast = Color(0xFFC8DEFF)
private val onTertiaryDarkMediumContrast = Color(0xFF102740)
private val tertiaryContainerDarkMediumContrast = Color(0xFF7D92B1)
private val onTertiaryContainerDarkMediumContrast = Color(0xFF000000)
private val errorDarkMediumContrast = Color(0xFFFFD2CC)
private val onErrorDarkMediumContrast = Color(0xFF540003)
private val errorContainerDarkMediumContrast = Color(0xFFFF5449)
private val onErrorContainerDarkMediumContrast = Color(0xFF000000)
private val backgroundDarkMediumContrast = Color(0xFF0E1514)
private val onBackgroundDarkMediumContrast = Color(0xFFDDE4E3)
private val surfaceDarkMediumContrast = Color(0xFF0E1514)
private val onSurfaceDarkMediumContrast = Color(0xFFFFFFFF)
private val surfaceVariantDarkMediumContrast = Color(0xFF3F4948)
private val onSurfaceVariantDarkMediumContrast = Color(0xFFD4DEDE)
private val outlineDarkMediumContrast = Color(0xFFAAB4B3)
private val outlineVariantDarkMediumContrast = Color(0xFF889292)
private val scrimDarkMediumContrast = Color(0xFF000000)
private val inverseSurfaceDarkMediumContrast = Color(0xFFDDE4E3)
private val inverseOnSurfaceDarkMediumContrast = Color(0xFF252B2B)
private val inversePrimaryDarkMediumContrast = Color(0xFF005151)
private val surfaceDimDarkMediumContrast = Color(0xFF0E1514)
private val surfaceBrightDarkMediumContrast = Color(0xFF3F4645)
private val surfaceContainerLowestDarkMediumContrast = Color(0xFF040808)
private val surfaceContainerLowDarkMediumContrast = Color(0xFF181F1F)
private val surfaceContainerDarkMediumContrast = Color(0xFF232929)
private val surfaceContainerHighDarkMediumContrast = Color(0xFF2D3434)
private val surfaceContainerHighestDarkMediumContrast = Color(0xFF383F3F)

private val primaryDarkHighContrast = Color(0xFFAAFFFE)
private val onPrimaryDarkHighContrast = Color(0xFF000000)
private val primaryContainerDarkHighContrast = Color(0xFF7CD1D0)
private val onPrimaryContainerDarkHighContrast = Color(0xFF000E0E)
private val secondaryDarkHighContrast = Color(0xFFD9F6F5)
private val onSecondaryDarkHighContrast = Color(0xFF000000)
private val secondaryContainerDarkHighContrast = Color(0xFFADC8C7)
private val onSecondaryContainerDarkHighContrast = Color(0xFF000E0E)
private val tertiaryDarkHighContrast = Color(0xFFE9F0FF)
private val onTertiaryDarkHighContrast = Color(0xFF000000)
private val tertiaryContainerDarkHighContrast = Color(0xFFAFC4E4)
private val onTertiaryContainerDarkHighContrast = Color(0xFF000C1C)
private val errorDarkHighContrast = Color(0xFFFFECE9)
private val onErrorDarkHighContrast = Color(0xFF000000)
private val errorContainerDarkHighContrast = Color(0xFFFFAEA4)
private val onErrorContainerDarkHighContrast = Color(0xFF220001)
private val backgroundDarkHighContrast = Color(0xFF0E1514)
private val onBackgroundDarkHighContrast = Color(0xFFDDE4E3)
private val surfaceDarkHighContrast = Color(0xFF0E1514)
private val onSurfaceDarkHighContrast = Color(0xFFFFFFFF)
private val surfaceVariantDarkHighContrast = Color(0xFF3F4948)
private val onSurfaceVariantDarkHighContrast = Color(0xFFFFFFFF)
private val outlineDarkHighContrast = Color(0xFFE8F2F1)
private val outlineVariantDarkHighContrast = Color(0xFFBAC5C4)
private val scrimDarkHighContrast = Color(0xFF000000)
private val inverseSurfaceDarkHighContrast = Color(0xFFDDE4E3)
private val inverseOnSurfaceDarkHighContrast = Color(0xFF000000)
private val inversePrimaryDarkHighContrast = Color(0xFF005151)
private val surfaceDimDarkHighContrast = Color(0xFF0E1514)
private val surfaceBrightDarkHighContrast = Color(0xFF4B5151)
private val surfaceContainerLowestDarkHighContrast = Color(0xFF000000)
private val surfaceContainerLowDarkHighContrast = Color(0xFF1A2121)
private val surfaceContainerDarkHighContrast = Color(0xFF2B3231)
private val surfaceContainerHighDarkHighContrast = Color(0xFF363D3C)
private val surfaceContainerHighestDarkHighContrast = Color(0xFF414848)

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
fun AquaTheme(
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

@Composable
fun AquaGlanceTheme(): ColorProviders {
    return ColorProviders(
        light = lightScheme,
        dark = darkScheme
    )
}