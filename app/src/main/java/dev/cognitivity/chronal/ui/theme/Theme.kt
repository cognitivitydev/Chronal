package dev.cognitivity.chronal.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.ColorScheme
import dev.cognitivity.chronal.ui.theme.colors.AquaTheme
import dev.cognitivity.chronal.ui.theme.colors.BlueTheme
import dev.cognitivity.chronal.ui.theme.colors.GreenTheme
import dev.cognitivity.chronal.ui.theme.colors.OrangeTheme
import dev.cognitivity.chronal.ui.theme.colors.PurpleTheme
import dev.cognitivity.chronal.ui.theme.colors.RedTheme
import dev.cognitivity.chronal.ui.theme.colors.YellowTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MetronomeTheme(
    color: ColorScheme.Color? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = ChronalApp.getInstance().settings.colorScheme.value
    val isDark = when(colorScheme.theme) {
        ColorScheme.Theme.SYSTEM -> isSystemInDarkTheme()
        ColorScheme.Theme.LIGHT -> false
        ColorScheme.Theme.DARK -> true
    }
    when(color ?: colorScheme.color) {
        ColorScheme.Color.SYSTEM -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MaterialExpressiveTheme(
                    colorScheme = if(isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context),
                    content = content
                )
            } else {
                AquaTheme(isDark, colorScheme.contrast, content)
            }
        }
        ColorScheme.Color.RED -> RedTheme(isDark, colorScheme.contrast, content)
        ColorScheme.Color.ORANGE -> OrangeTheme(isDark, colorScheme.contrast, content)
        ColorScheme.Color.YELLOW -> YellowTheme(isDark, colorScheme.contrast, content)
        ColorScheme.Color.GREEN -> GreenTheme(isDark, colorScheme.contrast, content)
        ColorScheme.Color.AQUA -> AquaTheme(isDark, colorScheme.contrast, content)
        ColorScheme.Color.BLUE -> BlueTheme(isDark, colorScheme.contrast, content)
        ColorScheme.Color.PURPLE -> PurpleTheme(isDark, colorScheme.contrast, content)
    }
}