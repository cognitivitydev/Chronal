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

package dev.cognitivity.chronal.ui.settings.items.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.materialkolor.dynamicColorScheme
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.ColorScheme
import dev.cognitivity.chronal.ui.settings.items.SettingItem

@Composable
fun CategoryOptionItem(item: SettingItem.CategoryOption, onNavigate: ((String) -> Unit)?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onNavigate?.invoke(item.pageId) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if(item.meta.icon != null) {
            val theme = Settings.COLOR_SCHEME.get().theme
            val seedColor = Color(item.color.value)
            val colorScheme = dynamicColorScheme(
                seedColor = seedColor,
                isDark = if(theme == ColorScheme.Theme.SYSTEM) isSystemInDarkTheme() else theme == ColorScheme.Theme.DARK
            )

            Box(
                modifier = Modifier.padding(end = 16.dp)
                    .size(40.dp)
                    .background(color = colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(item.meta.icon),
                    contentDescription = null,
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(item.meta.title.invoke(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if(item.meta.description != null) {
                Text(item.meta.description.invoke(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}