package dev.cognitivity.chronal.ui.theme.colors

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.cognitivity.chronal.ColorScheme

private val primaryLight = Color(0xFF425E91)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFD7E2FF)
private val onPrimaryContainerLight = Color(0xFF294677)
private val secondaryLight = Color(0xFF565E71)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFDAE2F9)
private val onSecondaryContainerLight = Color(0xFF3E4759)
private val tertiaryLight = Color(0xFF705574)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFFAD8FD)
private val onTertiaryContainerLight = Color(0xFF573E5B)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFF9F9FF)
private val onBackgroundLight = Color(0xFF1A1C20)
private val surfaceLight = Color(0xFFF9F9FF)
private val onSurfaceLight = Color(0xFF1A1C20)
private val surfaceVariantLight = Color(0xFFE0E2EC)
private val onSurfaceVariantLight = Color(0xFF44474E)
private val outlineLight = Color(0xFF74777F)
private val outlineVariantLight = Color(0xFFC4C6D0)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF2E3036)
private val inverseOnSurfaceLight = Color(0xFFF0F0F7)
private val inversePrimaryLight = Color(0xFFABC7FF)
private val surfaceDimLight = Color(0xFFD9D9E0)
private val surfaceBrightLight = Color(0xFFF9F9FF)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFF3F3FA)
private val surfaceContainerLight = Color(0xFFEDEDF4)
private val surfaceContainerHighLight = Color(0xFFE8E7EE)
private val surfaceContainerHighestLight = Color(0xFFE2E2E9)

private val primaryLightMediumContrast = Color(0xFF153566)
private val onPrimaryLightMediumContrast = Color(0xFFFFFFFF)
private val primaryContainerLightMediumContrast = Color(0xFF516DA1)
private val onPrimaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val secondaryLightMediumContrast = Color(0xFF2E3647)
private val onSecondaryLightMediumContrast = Color(0xFFFFFFFF)
private val secondaryContainerLightMediumContrast = Color(0xFF656D80)
private val onSecondaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val tertiaryLightMediumContrast = Color(0xFF452E4A)
private val onTertiaryLightMediumContrast = Color(0xFFFFFFFF)
private val tertiaryContainerLightMediumContrast = Color(0xFF806483)
private val onTertiaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val errorLightMediumContrast = Color(0xFF740006)
private val onErrorLightMediumContrast = Color(0xFFFFFFFF)
private val errorContainerLightMediumContrast = Color(0xFFCF2C27)
private val onErrorContainerLightMediumContrast = Color(0xFFFFFFFF)
private val backgroundLightMediumContrast = Color(0xFFF9F9FF)
private val onBackgroundLightMediumContrast = Color(0xFF1A1C20)
private val surfaceLightMediumContrast = Color(0xFFF9F9FF)
private val onSurfaceLightMediumContrast = Color(0xFF0F1116)
private val surfaceVariantLightMediumContrast = Color(0xFFE0E2EC)
private val onSurfaceVariantLightMediumContrast = Color(0xFF33363E)
private val outlineLightMediumContrast = Color(0xFF4F525A)
private val outlineVariantLightMediumContrast = Color(0xFF6A6D75)
private val scrimLightMediumContrast = Color(0xFF000000)
private val inverseSurfaceLightMediumContrast = Color(0xFF2E3036)
private val inverseOnSurfaceLightMediumContrast = Color(0xFFF0F0F7)
private val inversePrimaryLightMediumContrast = Color(0xFFABC7FF)
private val surfaceDimLightMediumContrast = Color(0xFFC6C6CD)
private val surfaceBrightLightMediumContrast = Color(0xFFF9F9FF)
private val surfaceContainerLowestLightMediumContrast = Color(0xFFFFFFFF)
private val surfaceContainerLowLightMediumContrast = Color(0xFFF3F3FA)
private val surfaceContainerLightMediumContrast = Color(0xFFE8E7EE)
private val surfaceContainerHighLightMediumContrast = Color(0xFFDCDCE3)
private val surfaceContainerHighestLightMediumContrast = Color(0xFFD1D1D8)

private val primaryLightHighContrast = Color(0xFF052B5B)
private val onPrimaryLightHighContrast = Color(0xFFFFFFFF)
private val primaryContainerLightHighContrast = Color(0xFF2B497A)
private val onPrimaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val secondaryLightHighContrast = Color(0xFF242C3D)
private val onSecondaryLightHighContrast = Color(0xFFFFFFFF)
private val secondaryContainerLightHighContrast = Color(0xFF41495B)
private val onSecondaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val tertiaryLightHighContrast = Color(0xFF3B243F)
private val onTertiaryLightHighContrast = Color(0xFFFFFFFF)
private val tertiaryContainerLightHighContrast = Color(0xFF5A405E)
private val onTertiaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val errorLightHighContrast = Color(0xFF600004)
private val onErrorLightHighContrast = Color(0xFFFFFFFF)
private val errorContainerLightHighContrast = Color(0xFF98000A)
private val onErrorContainerLightHighContrast = Color(0xFFFFFFFF)
private val backgroundLightHighContrast = Color(0xFFF9F9FF)
private val onBackgroundLightHighContrast = Color(0xFF1A1C20)
private val surfaceLightHighContrast = Color(0xFFF9F9FF)
private val onSurfaceLightHighContrast = Color(0xFF000000)
private val surfaceVariantLightHighContrast = Color(0xFFE0E2EC)
private val onSurfaceVariantLightHighContrast = Color(0xFF000000)
private val outlineLightHighContrast = Color(0xFF292C33)
private val outlineVariantLightHighContrast = Color(0xFF464951)
private val scrimLightHighContrast = Color(0xFF000000)
private val inverseSurfaceLightHighContrast = Color(0xFF2E3036)
private val inverseOnSurfaceLightHighContrast = Color(0xFFFFFFFF)
private val inversePrimaryLightHighContrast = Color(0xFFABC7FF)
private val surfaceDimLightHighContrast = Color(0xFFB8B8BF)
private val surfaceBrightLightHighContrast = Color(0xFFF9F9FF)
private val surfaceContainerLowestLightHighContrast = Color(0xFFFFFFFF)
private val surfaceContainerLowLightHighContrast = Color(0xFFF0F0F7)
private val surfaceContainerLightHighContrast = Color(0xFFE2E2E9)
private val surfaceContainerHighLightHighContrast = Color(0xFFD4D4DB)
private val surfaceContainerHighestLightHighContrast = Color(0xFFC6C6CD)

private val primaryDark = Color(0xFFABC7FF)
private val onPrimaryDark = Color(0xFF0D2F5F)
private val primaryContainerDark = Color(0xFF294677)
private val onPrimaryContainerDark = Color(0xFFD7E2FF)
private val secondaryDark = Color(0xFFBEC6DC)
private val onSecondaryDark = Color(0xFF283041)
private val secondaryContainerDark = Color(0xFF3E4759)
private val onSecondaryContainerDark = Color(0xFFDAE2F9)
private val tertiaryDark = Color(0xFFDDBCE0)
private val onTertiaryDark = Color(0xFF3F2844)
private val tertiaryContainerDark = Color(0xFF573E5B)
private val onTertiaryContainerDark = Color(0xFFFAD8FD)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF111318)
private val onBackgroundDark = Color(0xFFE2E2E9)
private val surfaceDark = Color(0xFF111318)
private val onSurfaceDark = Color(0xFFE2E2E9)
private val surfaceVariantDark = Color(0xFF44474E)
private val onSurfaceVariantDark = Color(0xFFC4C6D0)
private val outlineDark = Color(0xFF8E9099)
private val outlineVariantDark = Color(0xFF44474E)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFE2E2E9)
private val inverseOnSurfaceDark = Color(0xFF2E3036)
private val inversePrimaryDark = Color(0xFF425E91)
private val surfaceDimDark = Color(0xFF111318)
private val surfaceBrightDark = Color(0xFF37393E)
private val surfaceContainerLowestDark = Color(0xFF0C0E13)
private val surfaceContainerLowDark = Color(0xFF1A1C20)
private val surfaceContainerDark = Color(0xFF1E2025)
private val surfaceContainerHighDark = Color(0xFF282A2F)
private val surfaceContainerHighestDark = Color(0xFF33353A)

private val primaryDarkMediumContrast = Color(0xFFCEDCFF)
private val onPrimaryDarkMediumContrast = Color(0xFF002452)
private val primaryContainerDarkMediumContrast = Color(0xFF7591C7)
private val onPrimaryContainerDarkMediumContrast = Color(0xFF000000)
private val secondaryDarkMediumContrast = Color(0xFFD4DCF2)
private val onSecondaryDarkMediumContrast = Color(0xFF1D2636)
private val secondaryContainerDarkMediumContrast = Color(0xFF8891A5)
private val onSecondaryContainerDarkMediumContrast = Color(0xFF000000)
private val tertiaryDarkMediumContrast = Color(0xFFF4D1F6)
private val onTertiaryDarkMediumContrast = Color(0xFF341D39)
private val tertiaryContainerDarkMediumContrast = Color(0xFFA587A8)
private val onTertiaryContainerDarkMediumContrast = Color(0xFF000000)
private val errorDarkMediumContrast = Color(0xFFFFD2CC)
private val onErrorDarkMediumContrast = Color(0xFF540003)
private val errorContainerDarkMediumContrast = Color(0xFFFF5449)
private val onErrorContainerDarkMediumContrast = Color(0xFF000000)
private val backgroundDarkMediumContrast = Color(0xFF111318)
private val onBackgroundDarkMediumContrast = Color(0xFFE2E2E9)
private val surfaceDarkMediumContrast = Color(0xFF111318)
private val onSurfaceDarkMediumContrast = Color(0xFFFFFFFF)
private val surfaceVariantDarkMediumContrast = Color(0xFF44474E)
private val onSurfaceVariantDarkMediumContrast = Color(0xFFDADCE6)
private val outlineDarkMediumContrast = Color(0xFFAFB1BB)
private val outlineVariantDarkMediumContrast = Color(0xFF8E9099)
private val scrimDarkMediumContrast = Color(0xFF000000)
private val inverseSurfaceDarkMediumContrast = Color(0xFFE2E2E9)
private val inverseOnSurfaceDarkMediumContrast = Color(0xFF282A2F)
private val inversePrimaryDarkMediumContrast = Color(0xFF2A4879)
private val surfaceDimDarkMediumContrast = Color(0xFF111318)
private val surfaceBrightDarkMediumContrast = Color(0xFF43444A)
private val surfaceContainerLowestDarkMediumContrast = Color(0xFF06070C)
private val surfaceContainerLowDarkMediumContrast = Color(0xFF1C1E22)
private val surfaceContainerDarkMediumContrast = Color(0xFF26282D)
private val surfaceContainerHighDarkMediumContrast = Color(0xFF313238)
private val surfaceContainerHighestDarkMediumContrast = Color(0xFF3C3D43)

private val primaryDarkHighContrast = Color(0xFFEBF0FF)
private val onPrimaryDarkHighContrast = Color(0xFF000000)
private val primaryContainerDarkHighContrast = Color(0xFFA7C3FC)
private val onPrimaryContainerDarkHighContrast = Color(0xFF000B21)
private val secondaryDarkHighContrast = Color(0xFFEBF0FF)
private val onSecondaryDarkHighContrast = Color(0xFF000000)
private val secondaryContainerDarkHighContrast = Color(0xFFBAC2D8)
private val onSecondaryContainerDarkHighContrast = Color(0xFF040B1A)
private val tertiaryDarkHighContrast = Color(0xFFFFEAFE)
private val onTertiaryDarkHighContrast = Color(0xFF000000)
private val tertiaryContainerDarkHighContrast = Color(0xFFD9B8DC)
private val onTertiaryContainerDarkHighContrast = Color(0xFF17031D)
private val errorDarkHighContrast = Color(0xFFFFECE9)
private val onErrorDarkHighContrast = Color(0xFF000000)
private val errorContainerDarkHighContrast = Color(0xFFFFAEA4)
private val onErrorContainerDarkHighContrast = Color(0xFF220001)
private val backgroundDarkHighContrast = Color(0xFF111318)
private val onBackgroundDarkHighContrast = Color(0xFFE2E2E9)
private val surfaceDarkHighContrast = Color(0xFF111318)
private val onSurfaceDarkHighContrast = Color(0xFFFFFFFF)
private val surfaceVariantDarkHighContrast = Color(0xFF44474E)
private val onSurfaceVariantDarkHighContrast = Color(0xFFFFFFFF)
private val outlineDarkHighContrast = Color(0xFFEEEFF9)
private val outlineVariantDarkHighContrast = Color(0xFFC0C2CC)
private val scrimDarkHighContrast = Color(0xFF000000)
private val inverseSurfaceDarkHighContrast = Color(0xFFE2E2E9)
private val inverseOnSurfaceDarkHighContrast = Color(0xFF000000)
private val inversePrimaryDarkHighContrast = Color(0xFF2A4879)
private val surfaceDimDarkHighContrast = Color(0xFF111318)
private val surfaceBrightDarkHighContrast = Color(0xFF4E5056)
private val surfaceContainerLowestDarkHighContrast = Color(0xFF000000)
private val surfaceContainerLowDarkHighContrast = Color(0xFF1E2025)
private val surfaceContainerDarkHighContrast = Color(0xFF2E3036)
private val surfaceContainerHighDarkHighContrast = Color(0xFF393B41)
private val surfaceContainerHighestDarkHighContrast = Color(0xFF45474C)

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
fun BlueTheme(
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