package dev.cognitivity.chronal.ui.theme.colors

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.cognitivity.chronal.ColorScheme

private val primaryLight = Color(0xFF616118)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFE8E78F)
private val onPrimaryContainerLight = Color(0xFF494900)
private val secondaryLight = Color(0xFF606043)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFE7E4BF)
private val onSecondaryContainerLight = Color(0xFF49482D)
private val tertiaryLight = Color(0xFF3D6657)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFBFECD8)
private val onTertiaryContainerLight = Color(0xFF254E40)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFFDF9EC)
private val onBackgroundLight = Color(0xFF1C1C14)
private val surfaceLight = Color(0xFFFDF9EC)
private val onSurfaceLight = Color(0xFF1C1C14)
private val surfaceVariantLight = Color(0xFFE6E3D1)
private val onSurfaceVariantLight = Color(0xFF48473A)
private val outlineLight = Color(0xFF797869)
private val outlineVariantLight = Color(0xFFCAC7B6)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF313128)
private val inverseOnSurfaceLight = Color(0xFFF4F1E3)
private val inversePrimaryLight = Color(0xFFCBCB76)
private val surfaceDimLight = Color(0xFFDDDACD)
private val surfaceBrightLight = Color(0xFFFDF9EC)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFF7F4E6)
private val surfaceContainerLight = Color(0xFFF1EEE0)
private val surfaceContainerHighLight = Color(0xFFECE8DB)
private val surfaceContainerHighestLight = Color(0xFFE6E3D5)

private val primaryLightMediumContrast = Color(0xFF383800)
private val onPrimaryLightMediumContrast = Color(0xFFFFFFFF)
private val primaryContainerLightMediumContrast = Color(0xFF707026)
private val onPrimaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val secondaryLightMediumContrast = Color(0xFF38371E)
private val onSecondaryLightMediumContrast = Color(0xFFFFFFFF)
private val secondaryContainerLightMediumContrast = Color(0xFF6F6F50)
private val onSecondaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val tertiaryLightMediumContrast = Color(0xFF133D2F)
private val onTertiaryLightMediumContrast = Color(0xFFFFFFFF)
private val tertiaryContainerLightMediumContrast = Color(0xFF4C7565)
private val onTertiaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val errorLightMediumContrast = Color(0xFF740006)
private val onErrorLightMediumContrast = Color(0xFFFFFFFF)
private val errorContainerLightMediumContrast = Color(0xFFCF2C27)
private val onErrorContainerLightMediumContrast = Color(0xFFFFFFFF)
private val backgroundLightMediumContrast = Color(0xFFFDF9EC)
private val onBackgroundLightMediumContrast = Color(0xFF1C1C14)
private val surfaceLightMediumContrast = Color(0xFFFDF9EC)
private val onSurfaceLightMediumContrast = Color(0xFF12110A)
private val surfaceVariantLightMediumContrast = Color(0xFFE6E3D1)
private val onSurfaceVariantLightMediumContrast = Color(0xFF37372A)
private val outlineLightMediumContrast = Color(0xFF545345)
private val outlineVariantLightMediumContrast = Color(0xFF6F6E5F)
private val scrimLightMediumContrast = Color(0xFF000000)
private val inverseSurfaceLightMediumContrast = Color(0xFF313128)
private val inverseOnSurfaceLightMediumContrast = Color(0xFFF4F1E3)
private val inversePrimaryLightMediumContrast = Color(0xFFCBCB76)
private val surfaceDimLightMediumContrast = Color(0xFFC9C7BA)
private val surfaceBrightLightMediumContrast = Color(0xFFFDF9EC)
private val surfaceContainerLowestLightMediumContrast = Color(0xFFFFFFFF)
private val surfaceContainerLowLightMediumContrast = Color(0xFFF7F4E6)
private val surfaceContainerLightMediumContrast = Color(0xFFECE8DB)
private val surfaceContainerHighLightMediumContrast = Color(0xFFE0DDD0)
private val surfaceContainerHighestLightMediumContrast = Color(0xFFD5D2C5)

private val primaryLightHighContrast = Color(0xFF2E2E00)
private val onPrimaryLightHighContrast = Color(0xFFFFFFFF)
private val primaryContainerLightHighContrast = Color(0xFF4C4C01)
private val onPrimaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val secondaryLightHighContrast = Color(0xFF2D2D14)
private val onSecondaryLightHighContrast = Color(0xFFFFFFFF)
private val secondaryContainerLightHighContrast = Color(0xFF4B4B2F)
private val onSecondaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val tertiaryLightHighContrast = Color(0xFF053326)
private val onTertiaryLightHighContrast = Color(0xFFFFFFFF)
private val tertiaryContainerLightHighContrast = Color(0xFF285142)
private val onTertiaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val errorLightHighContrast = Color(0xFF600004)
private val onErrorLightHighContrast = Color(0xFFFFFFFF)
private val errorContainerLightHighContrast = Color(0xFF98000A)
private val onErrorContainerLightHighContrast = Color(0xFFFFFFFF)
private val backgroundLightHighContrast = Color(0xFFFDF9EC)
private val onBackgroundLightHighContrast = Color(0xFF1C1C14)
private val surfaceLightHighContrast = Color(0xFFFDF9EC)
private val onSurfaceLightHighContrast = Color(0xFF000000)
private val surfaceVariantLightHighContrast = Color(0xFFE6E3D1)
private val onSurfaceVariantLightHighContrast = Color(0xFF000000)
private val outlineLightHighContrast = Color(0xFF2D2D21)
private val outlineVariantLightHighContrast = Color(0xFF4B4A3D)
private val scrimLightHighContrast = Color(0xFF000000)
private val inverseSurfaceLightHighContrast = Color(0xFF313128)
private val inverseOnSurfaceLightHighContrast = Color(0xFFFFFFFF)
private val inversePrimaryLightHighContrast = Color(0xFFCBCB76)
private val surfaceDimLightHighContrast = Color(0xFFBCB9AC)
private val surfaceBrightLightHighContrast = Color(0xFFFDF9EC)
private val surfaceContainerLowestLightHighContrast = Color(0xFFFFFFFF)
private val surfaceContainerLowLightHighContrast = Color(0xFFF4F1E3)
private val surfaceContainerLightHighContrast = Color(0xFFE6E3D5)
private val surfaceContainerHighLightHighContrast = Color(0xFFD8D4C8)
private val surfaceContainerHighestLightHighContrast = Color(0xFFC9C7BA)

private val primaryDark = Color(0xFFCBCB76)
private val onPrimaryDark = Color(0xFF323200)
private val primaryContainerDark = Color(0xFF494900)
private val onPrimaryContainerDark = Color(0xFFE8E78F)
private val secondaryDark = Color(0xFFCAC8A5)
private val onSecondaryDark = Color(0xFF323218)
private val secondaryContainerDark = Color(0xFF49482D)
private val onSecondaryContainerDark = Color(0xFFE7E4BF)
private val tertiaryDark = Color(0xFFA4D0BD)
private val onTertiaryDark = Color(0xFF0B372A)
private val tertiaryContainerDark = Color(0xFF254E40)
private val onTertiaryContainerDark = Color(0xFFBFECD8)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF14140C)
private val onBackgroundDark = Color(0xFFE6E3D5)
private val surfaceDark = Color(0xFF14140C)
private val onSurfaceDark = Color(0xFFE6E3D5)
private val surfaceVariantDark = Color(0xFF48473A)
private val onSurfaceVariantDark = Color(0xFFCAC7B6)
private val outlineDark = Color(0xFF939182)
private val outlineVariantDark = Color(0xFF48473A)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFE6E3D5)
private val inverseOnSurfaceDark = Color(0xFF313128)
private val inversePrimaryDark = Color(0xFF616118)
private val surfaceDimDark = Color(0xFF14140C)
private val surfaceBrightDark = Color(0xFF3A3A30)
private val surfaceContainerLowestDark = Color(0xFF0F0F07)
private val surfaceContainerLowDark = Color(0xFF1C1C14)
private val surfaceContainerDark = Color(0xFF202018)
private val surfaceContainerHighDark = Color(0xFF2B2A22)
private val surfaceContainerHighestDark = Color(0xFF36352C)

private val primaryDarkMediumContrast = Color(0xFFE2E18A)
private val onPrimaryDarkMediumContrast = Color(0xFF272700)
private val primaryContainerDarkMediumContrast = Color(0xFF959446)
private val onPrimaryContainerDarkMediumContrast = Color(0xFF000000)
private val secondaryDarkMediumContrast = Color(0xFFE0DEB9)
private val onSecondaryDarkMediumContrast = Color(0xFF27270F)
private val secondaryContainerDarkMediumContrast = Color(0xFF949272)
private val onSecondaryContainerDarkMediumContrast = Color(0xFF000000)
private val tertiaryDarkMediumContrast = Color(0xFFB9E6D2)
private val onTertiaryDarkMediumContrast = Color(0xFF002C1F)
private val tertiaryContainerDarkMediumContrast = Color(0xFF6F9A88)
private val onTertiaryContainerDarkMediumContrast = Color(0xFF000000)
private val errorDarkMediumContrast = Color(0xFFFFD2CC)
private val onErrorDarkMediumContrast = Color(0xFF540003)
private val errorContainerDarkMediumContrast = Color(0xFFFF5449)
private val onErrorContainerDarkMediumContrast = Color(0xFF000000)
private val backgroundDarkMediumContrast = Color(0xFF14140C)
private val onBackgroundDarkMediumContrast = Color(0xFFE6E3D5)
private val surfaceDarkMediumContrast = Color(0xFF14140C)
private val onSurfaceDarkMediumContrast = Color(0xFFFFFFFF)
private val surfaceVariantDarkMediumContrast = Color(0xFF48473A)
private val onSurfaceVariantDarkMediumContrast = Color(0xFFE0DDCB)
private val outlineDarkMediumContrast = Color(0xFFB5B2A2)
private val outlineVariantDarkMediumContrast = Color(0xFF939181)
private val scrimDarkMediumContrast = Color(0xFF000000)
private val inverseSurfaceDarkMediumContrast = Color(0xFFE6E3D5)
private val inverseOnSurfaceDarkMediumContrast = Color(0xFF2B2A22)
private val inversePrimaryDarkMediumContrast = Color(0xFF4B4B00)
private val surfaceDimDarkMediumContrast = Color(0xFF14140C)
private val surfaceBrightDarkMediumContrast = Color(0xFF46453B)
private val surfaceContainerLowestDarkMediumContrast = Color(0xFF080803)
private val surfaceContainerLowDarkMediumContrast = Color(0xFF1E1E16)
private val surfaceContainerDarkMediumContrast = Color(0xFF292820)
private val surfaceContainerHighDarkMediumContrast = Color(0xFF34332A)
private val surfaceContainerHighestDarkMediumContrast = Color(0xFF3F3E35)

private val primaryDarkHighContrast = Color(0xFFF6F59B)
private val onPrimaryDarkHighContrast = Color(0xFF000000)
private val primaryContainerDarkHighContrast = Color(0xFFC8C773)
private val onPrimaryContainerDarkHighContrast = Color(0xFF0C0C00)
private val secondaryDarkHighContrast = Color(0xFFF4F2CC)
private val onSecondaryDarkHighContrast = Color(0xFF000000)
private val secondaryContainerDarkHighContrast = Color(0xFFC6C4A1)
private val onSecondaryContainerDarkHighContrast = Color(0xFF0C0C00)
private val tertiaryDarkHighContrast = Color(0xFFCDFAE5)
private val onTertiaryDarkHighContrast = Color(0xFF000000)
private val tertiaryContainerDarkHighContrast = Color(0xFFA0CCB9)
private val onTertiaryContainerDarkHighContrast = Color(0xFF000E08)
private val errorDarkHighContrast = Color(0xFFFFECE9)
private val onErrorDarkHighContrast = Color(0xFF000000)
private val errorContainerDarkHighContrast = Color(0xFFFFAEA4)
private val onErrorContainerDarkHighContrast = Color(0xFF220001)
private val backgroundDarkHighContrast = Color(0xFF14140C)
private val onBackgroundDarkHighContrast = Color(0xFFE6E3D5)
private val surfaceDarkHighContrast = Color(0xFF14140C)
private val onSurfaceDarkHighContrast = Color(0xFFFFFFFF)
private val surfaceVariantDarkHighContrast = Color(0xFF48473A)
private val onSurfaceVariantDarkHighContrast = Color(0xFFFFFFFF)
private val outlineDarkHighContrast = Color(0xFFF4F0DE)
private val outlineVariantDarkHighContrast = Color(0xFFC6C3B2)
private val scrimDarkHighContrast = Color(0xFF000000)
private val inverseSurfaceDarkHighContrast = Color(0xFFE6E3D5)
private val inverseOnSurfaceDarkHighContrast = Color(0xFF000000)
private val inversePrimaryDarkHighContrast = Color(0xFF4B4B00)
private val surfaceDimDarkHighContrast = Color(0xFF14140C)
private val surfaceBrightDarkHighContrast = Color(0xFF515046)
private val surfaceContainerLowestDarkHighContrast = Color(0xFF000000)
private val surfaceContainerLowDarkHighContrast = Color(0xFF202018)
private val surfaceContainerDarkHighContrast = Color(0xFF313128)
private val surfaceContainerHighDarkHighContrast = Color(0xFF3D3C32)
private val surfaceContainerHighestDarkHighContrast = Color(0xFF48473D)

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
fun YellowTheme(
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