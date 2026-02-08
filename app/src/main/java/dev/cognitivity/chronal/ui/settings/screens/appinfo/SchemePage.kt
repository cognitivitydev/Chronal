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

package dev.cognitivity.chronal.ui.settings.screens.appinfo

import android.content.Intent
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.ColorScheme
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.data.SettingsPage
import dev.cognitivity.chronal.ui.settings.screens.appinfo.SchemePage.Color
import dev.cognitivity.chronal.ui.settings.screens.appinfo.SchemePage.Contrast
import dev.cognitivity.chronal.ui.settings.screens.appinfo.SchemePage.Reload
import dev.cognitivity.chronal.ui.settings.screens.appinfo.SchemePage.Theme
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.launch

object SchemePage : SettingsPage(
    id = "color_scheme",
    title = R.string.page_settings_color_scheme,
    items = listOf(
        SettingItem.SubCategoryHeader(R.string.setting_color_scheme_title),
        SettingItem.Container { Color() },
        SettingItem.SubCategoryHeader(R.string.setting_color_theme_title),
        SettingItem.Element { Theme() },
        SettingItem.SubCategoryHeader(R.string.setting_color_contrast_title),
        SettingItem.Element { Contrast() },
        SettingItem.Element { Reload() }
    )
) {
    val setting = Settings.COLOR_SCHEME
    var value by mutableStateOf(setting.get())

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun Color() {
        val scope = rememberCoroutineScope()

        val surfaceContainerLow = MaterialTheme.colorScheme.surfaceContainerLow

        @Composable
        fun ColorElement(color: ColorScheme.Color, selected: Boolean) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(if(selected) 16.dp else 24.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        value = value.copy(
                            color = color,
                            contrast = if (color == ColorScheme.Color.SYSTEM) ColorScheme.Contrast.SYSTEM
                                else if (value.contrast == ColorScheme.Contrast.SYSTEM) ColorScheme.Contrast.LOW
                                else value.contrast
                        )
                        scope.launch {
                            setting.save(value)
                        }
                    }
                    .then(
                        if(selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                            .border(6.dp, surfaceContainerLow, RoundedCornerShape(16.dp))
                        else Modifier
                    )

            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            item { Spacer(modifier = Modifier.width(16.dp)) }
            items(ColorScheme.Color.entries) { color ->
                val selected = value.color == color
                MetronomeTheme(color) { ColorElement(color, selected) }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun Theme() {
        val scope = rememberCoroutineScope()

        Column {
            repeat(3) { i ->
                val interactionSource = remember { MutableInteractionSource() }

                val theme = ColorScheme.Theme.entries[i]
                val text = when(theme) {
                    ColorScheme.Theme.SYSTEM -> R.string.setting_color_theme_system
                    ColorScheme.Theme.LIGHT -> R.string.setting_color_theme_light
                    ColorScheme.Theme.DARK -> R.string.setting_color_theme_dark
                }

                val topRounded = i == 0
                val bottomRounded = i == 2
                val shape = RoundedCornerShape(
                    topStart = if (topRounded) 12.dp else 6.dp,
                    topEnd = if (topRounded) 12.dp else 6.dp,
                    bottomStart = if (bottomRounded) 12.dp else 6.dp,
                    bottomEnd = if (bottomRounded) 12.dp else 6.dp
                )

                val onSelect = {
                    value = value.copy(theme = theme)
                    scope.launch {
                        setting.save(value)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = shape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 1.dp)
                        .defaultMinSize(minHeight = 72.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current
                        ) {
                            onSelect()
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = value.theme == theme,
                            onClick = { onSelect() },
                            interactionSource = interactionSource
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(context.getString(text), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun Contrast() {
        val scope = rememberCoroutineScope()

        Column {
            repeat(4) { i ->
                val interactionSource = remember { MutableInteractionSource() }

                val contrast = ColorScheme.Contrast.entries[i]
                val enabled = when(value.color) {
                    ColorScheme.Color.SYSTEM -> contrast == ColorScheme.Contrast.SYSTEM
                    else -> contrast != ColorScheme.Contrast.SYSTEM
                }
                val text = when(contrast) {
                    ColorScheme.Contrast.SYSTEM -> R.string.setting_color_contrast_system
                    ColorScheme.Contrast.LOW -> R.string.setting_color_contrast_low
                    ColorScheme.Contrast.MEDIUM -> R.string.setting_color_contrast_medium
                    ColorScheme.Contrast.HIGH -> R.string.setting_color_contrast_high
                }

                val topRounded = i == 0
                val bottomRounded = i == 3
                val shape = RoundedCornerShape(
                    topStart = if (topRounded) 12.dp else 6.dp,
                    topEnd = if (topRounded) 12.dp else 6.dp,
                    bottomStart = if (bottomRounded) 12.dp else 6.dp,
                    bottomEnd = if (bottomRounded) 12.dp else 6.dp
                )

                val onSelect = {
                    value = value.copy(contrast = contrast)
                    scope.launch {
                        setting.save(value)
                    }
                }

                Surface(
                    color = if(enabled) MaterialTheme.colorScheme.surfaceContainerHigh
                        else MaterialTheme.colorScheme.surfaceContainer,
                    shape = shape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 1.dp)
                        .defaultMinSize(minHeight = 72.dp)
                        .clickable(
                            enabled = enabled,
                            interactionSource = interactionSource,
                            indication = LocalIndication.current
                        ) {
                            onSelect()
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = value.contrast == contrast,
                            onClick = { onSelect() },
                            enabled = enabled,
                            interactionSource = interactionSource
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(context.getString(text),
                            style = MaterialTheme.typography.titleMedium,
                            color = if(enabled) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        if(value.color == ColorScheme.Color.SYSTEM) {
            Row(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = context.getString(R.string.generic_info),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = context.getString(R.string.setting_color_contrast_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun Reload() {
        Box(
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        ) {
            FilledTonalButton(
                modifier = Modifier.heightIn(ButtonDefaults.MediumContainerHeight)
                    .align(Alignment.Center),
                contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                onClick = {
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra("destination", "settings")
                    )
                },
            ) {
                Text(context.getString(R.string.setting_color_save_reload),
                    style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)
                )
            }
        }
    }
}