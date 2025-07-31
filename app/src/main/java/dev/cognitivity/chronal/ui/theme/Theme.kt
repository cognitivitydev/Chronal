package dev.cognitivity.chronal.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MetronomeTheme(
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        AquaTheme(isDark, content)
        return
    }

    val context = LocalContext.current
    MaterialExpressiveTheme(
        colorScheme = if(isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context),
        content = content
    )
}