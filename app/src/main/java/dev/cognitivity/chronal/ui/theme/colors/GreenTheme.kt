package dev.cognitivity.chronal.ui.theme.colors

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.cognitivity.chronal.ColorScheme

private val primaryLight = Color(0xFF406836)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFC0EFB0)
private val onPrimaryContainerLight = Color(0xFF285020)
private val secondaryLight = Color(0xFF54634D)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFD7E8CD)
private val onSecondaryContainerLight = Color(0xFF3C4B37)
private val tertiaryLight = Color(0xFF386568)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFBCEBEE)
private val onTertiaryContainerLight = Color(0xFF1E4D50)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFF8FBF1)
private val onBackgroundLight = Color(0xFF191D17)
private val surfaceLight = Color(0xFFF8FBF1)
private val onSurfaceLight = Color(0xFF191D17)
private val surfaceVariantLight = Color(0xFFDFE4D7)
private val onSurfaceVariantLight = Color(0xFF43483F)
private val outlineLight = Color(0xFF73796E)
private val outlineVariantLight = Color(0xFFC3C8BC)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF2E322B)
private val inverseOnSurfaceLight = Color(0xFFEFF2E8)
private val inversePrimaryLight = Color(0xFFA5D395)
private val surfaceDimLight = Color(0xFFD8DBD2)
private val surfaceBrightLight = Color(0xFFF8FBF1)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFF2F5EB)
private val surfaceContainerLight = Color(0xFFECEFE5)
private val surfaceContainerHighLight = Color(0xFFE6E9E0)
private val surfaceContainerHighestLight = Color(0xFFE1E4DA)

private val primaryLightMediumContrast = Color(0xFF173E11)
private val onPrimaryLightMediumContrast = Color(0xFFFFFFFF)
private val primaryContainerLightMediumContrast = Color(0xFF4E7743)
private val onPrimaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val secondaryLightMediumContrast = Color(0xFF2C3A27)
private val onSecondaryLightMediumContrast = Color(0xFFFFFFFF)
private val secondaryContainerLightMediumContrast = Color(0xFF62715B)
private val onSecondaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val tertiaryLightMediumContrast = Color(0xFF073D3F)
private val onTertiaryLightMediumContrast = Color(0xFFFFFFFF)
private val tertiaryContainerLightMediumContrast = Color(0xFF477477)
private val onTertiaryContainerLightMediumContrast = Color(0xFFFFFFFF)
private val errorLightMediumContrast = Color(0xFF740006)
private val onErrorLightMediumContrast = Color(0xFFFFFFFF)
private val errorContainerLightMediumContrast = Color(0xFFCF2C27)
private val onErrorContainerLightMediumContrast = Color(0xFFFFFFFF)
private val backgroundLightMediumContrast = Color(0xFFF8FBF1)
private val onBackgroundLightMediumContrast = Color(0xFF191D17)
private val surfaceLightMediumContrast = Color(0xFFF8FBF1)
private val onSurfaceLightMediumContrast = Color(0xFF0E120D)
private val surfaceVariantLightMediumContrast = Color(0xFFDFE4D7)
private val onSurfaceVariantLightMediumContrast = Color(0xFF32382F)
private val outlineLightMediumContrast = Color(0xFF4E544A)
private val outlineVariantLightMediumContrast = Color(0xFF696F64)
private val scrimLightMediumContrast = Color(0xFF000000)
private val inverseSurfaceLightMediumContrast = Color(0xFF2E322B)
private val inverseOnSurfaceLightMediumContrast = Color(0xFFEFF2E8)
private val inversePrimaryLightMediumContrast = Color(0xFFA5D395)
private val surfaceDimLightMediumContrast = Color(0xFFC4C8BE)
private val surfaceBrightLightMediumContrast = Color(0xFFF8FBF1)
private val surfaceContainerLowestLightMediumContrast = Color(0xFFFFFFFF)
private val surfaceContainerLowLightMediumContrast = Color(0xFFF2F5EB)
private val surfaceContainerLightMediumContrast = Color(0xFFE6E9E0)
private val surfaceContainerHighLightMediumContrast = Color(0xFFDBDED4)
private val surfaceContainerHighestLightMediumContrast = Color(0xFFD0D3C9)

private val primaryLightHighContrast = Color(0xFF0C3407)
private val onPrimaryLightHighContrast = Color(0xFFFFFFFF)
private val primaryContainerLightHighContrast = Color(0xFF2B5222)
private val onPrimaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val secondaryLightHighContrast = Color(0xFF22301E)
private val onSecondaryLightHighContrast = Color(0xFFFFFFFF)
private val secondaryContainerLightHighContrast = Color(0xFF3F4D39)
private val onSecondaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val tertiaryLightHighContrast = Color(0xFF003234)
private val onTertiaryLightHighContrast = Color(0xFFFFFFFF)
private val tertiaryContainerLightHighContrast = Color(0xFF215053)
private val onTertiaryContainerLightHighContrast = Color(0xFFFFFFFF)
private val errorLightHighContrast = Color(0xFF600004)
private val onErrorLightHighContrast = Color(0xFFFFFFFF)
private val errorContainerLightHighContrast = Color(0xFF98000A)
private val onErrorContainerLightHighContrast = Color(0xFFFFFFFF)
private val backgroundLightHighContrast = Color(0xFFF8FBF1)
private val onBackgroundLightHighContrast = Color(0xFF191D17)
private val surfaceLightHighContrast = Color(0xFFF8FBF1)
private val onSurfaceLightHighContrast = Color(0xFF000000)
private val surfaceVariantLightHighContrast = Color(0xFFDFE4D7)
private val onSurfaceVariantLightHighContrast = Color(0xFF000000)
private val outlineLightHighContrast = Color(0xFF282E25)
private val outlineVariantLightHighContrast = Color(0xFF454B41)
private val scrimLightHighContrast = Color(0xFF000000)
private val inverseSurfaceLightHighContrast = Color(0xFF2E322B)
private val inverseOnSurfaceLightHighContrast = Color(0xFFFFFFFF)
private val inversePrimaryLightHighContrast = Color(0xFFA5D395)
private val surfaceDimLightHighContrast = Color(0xFFB7BAB1)
private val surfaceBrightLightHighContrast = Color(0xFFF8FBF1)
private val surfaceContainerLowestLightHighContrast = Color(0xFFFFFFFF)
private val surfaceContainerLowLightHighContrast = Color(0xFFEFF2E8)
private val surfaceContainerLightHighContrast = Color(0xFFE1E4DA)
private val surfaceContainerHighLightHighContrast = Color(0xFFD2D6CC)
private val surfaceContainerHighestLightHighContrast = Color(0xFFC4C8BE)

private val primaryDark = Color(0xFFA5D395)
private val onPrimaryDark = Color(0xFF11380B)
private val primaryContainerDark = Color(0xFF285020)
private val onPrimaryContainerDark = Color(0xFFC0EFB0)
private val secondaryDark = Color(0xFFBBCBB2)
private val onSecondaryDark = Color(0xFF263422)
private val secondaryContainerDark = Color(0xFF3C4B37)
private val onSecondaryContainerDark = Color(0xFFD7E8CD)
private val tertiaryDark = Color(0xFFA0CFD2)
private val onTertiaryDark = Color(0xFF003739)
private val tertiaryContainerDark = Color(0xFF1E4D50)
private val onTertiaryContainerDark = Color(0xFFBCEBEE)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF11140F)
private val onBackgroundDark = Color(0xFFE1E4DA)
private val surfaceDark = Color(0xFF11140F)
private val onSurfaceDark = Color(0xFFE1E4DA)
private val surfaceVariantDark = Color(0xFF43483F)
private val onSurfaceVariantDark = Color(0xFFC3C8BC)
private val outlineDark = Color(0xFF8D9387)
private val outlineVariantDark = Color(0xFF43483F)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFE1E4DA)
private val inverseOnSurfaceDark = Color(0xFF2E322B)
private val inversePrimaryDark = Color(0xFF406836)
private val surfaceDimDark = Color(0xFF11140F)
private val surfaceBrightDark = Color(0xFF363A34)
private val surfaceContainerLowestDark = Color(0xFF0C0F0A)
private val surfaceContainerLowDark = Color(0xFF191D17)
private val surfaceContainerDark = Color(0xFF1D211B)
private val surfaceContainerHighDark = Color(0xFF272B25)
private val surfaceContainerHighestDark = Color(0xFF32362F)

private val primaryDarkMediumContrast = Color(0xFFBAE9AA)
private val onPrimaryDarkMediumContrast = Color(0xFF052D03)
private val primaryContainerDarkMediumContrast = Color(0xFF719C64)
private val onPrimaryContainerDarkMediumContrast = Color(0xFF000000)
private val secondaryDarkMediumContrast = Color(0xFFD1E1C7)
private val onSecondaryDarkMediumContrast = Color(0xFF1C2918)
private val secondaryContainerDarkMediumContrast = Color(0xFF86957E)
private val onSecondaryContainerDarkMediumContrast = Color(0xFF000000)
private val tertiaryDarkMediumContrast = Color(0xFFB6E5E8)
private val onTertiaryDarkMediumContrast = Color(0xFF002B2D)
private val tertiaryContainerDarkMediumContrast = Color(0xFF6B989B)
private val onTertiaryContainerDarkMediumContrast = Color(0xFF000000)
private val errorDarkMediumContrast = Color(0xFFFFD2CC)
private val onErrorDarkMediumContrast = Color(0xFF540003)
private val errorContainerDarkMediumContrast = Color(0xFFFF5449)
private val onErrorContainerDarkMediumContrast = Color(0xFF000000)
private val backgroundDarkMediumContrast = Color(0xFF11140F)
private val onBackgroundDarkMediumContrast = Color(0xFFE1E4DA)
private val surfaceDarkMediumContrast = Color(0xFF11140F)
private val onSurfaceDarkMediumContrast = Color(0xFFFFFFFF)
private val surfaceVariantDarkMediumContrast = Color(0xFF43483F)
private val onSurfaceVariantDarkMediumContrast = Color(0xFFD9DED1)
private val outlineDarkMediumContrast = Color(0xFFAEB4A8)
private val outlineVariantDarkMediumContrast = Color(0xFF8C9287)
private val scrimDarkMediumContrast = Color(0xFF000000)
private val inverseSurfaceDarkMediumContrast = Color(0xFFE1E4DA)
private val inverseOnSurfaceDarkMediumContrast = Color(0xFF272B25)
private val inversePrimaryDarkMediumContrast = Color(0xFF2A5121)
private val surfaceDimDarkMediumContrast = Color(0xFF11140F)
private val surfaceBrightDarkMediumContrast = Color(0xFF42463F)
private val surfaceContainerLowestDarkMediumContrast = Color(0xFF050804)
private val surfaceContainerLowDarkMediumContrast = Color(0xFF1B1F19)
private val surfaceContainerDarkMediumContrast = Color(0xFF252923)
private val surfaceContainerHighDarkMediumContrast = Color(0xFF30342D)
private val surfaceContainerHighestDarkMediumContrast = Color(0xFF3B3F38)

private val primaryDarkHighContrast = Color(0xFFCEFDBC)
private val onPrimaryDarkHighContrast = Color(0xFF000000)
private val primaryContainerDarkHighContrast = Color(0xFFA1CF92)
private val onPrimaryContainerDarkHighContrast = Color(0xFF000F00)
private val secondaryDarkHighContrast = Color(0xFFE4F5DA)
private val onSecondaryDarkHighContrast = Color(0xFF000000)
private val secondaryContainerDarkHighContrast = Color(0xFFB7C8AE)
private val onSecondaryContainerDarkHighContrast = Color(0xFF030E02)
private val tertiaryDarkHighContrast = Color(0xFFC9F9FC)
private val onTertiaryDarkHighContrast = Color(0xFF000000)
private val tertiaryContainerDarkHighContrast = Color(0xFF9CCBCE)
private val onTertiaryContainerDarkHighContrast = Color(0xFF000E0F)
private val errorDarkHighContrast = Color(0xFFFFECE9)
private val onErrorDarkHighContrast = Color(0xFF000000)
private val errorContainerDarkHighContrast = Color(0xFFFFAEA4)
private val onErrorContainerDarkHighContrast = Color(0xFF220001)
private val backgroundDarkHighContrast = Color(0xFF11140F)
private val onBackgroundDarkHighContrast = Color(0xFFE1E4DA)
private val surfaceDarkHighContrast = Color(0xFF11140F)
private val onSurfaceDarkHighContrast = Color(0xFFFFFFFF)
private val surfaceVariantDarkHighContrast = Color(0xFF43483F)
private val onSurfaceVariantDarkHighContrast = Color(0xFFFFFFFF)
private val outlineDarkHighContrast = Color(0xFFECF2E5)
private val outlineVariantDarkHighContrast = Color(0xFFBFC5B8)
private val scrimDarkHighContrast = Color(0xFF000000)
private val inverseSurfaceDarkHighContrast = Color(0xFFE1E4DA)
private val inverseOnSurfaceDarkHighContrast = Color(0xFF000000)
private val inversePrimaryDarkHighContrast = Color(0xFF2A5121)
private val surfaceDimDarkHighContrast = Color(0xFF11140F)
private val surfaceBrightDarkHighContrast = Color(0xFF4D514A)
private val surfaceContainerLowestDarkHighContrast = Color(0xFF000000)
private val surfaceContainerLowDarkHighContrast = Color(0xFF1D211B)
private val surfaceContainerDarkHighContrast = Color(0xFF2E322B)
private val surfaceContainerHighDarkHighContrast = Color(0xFF393D36)
private val surfaceContainerHighestDarkHighContrast = Color(0xFF444841)

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
fun GreenTheme(
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