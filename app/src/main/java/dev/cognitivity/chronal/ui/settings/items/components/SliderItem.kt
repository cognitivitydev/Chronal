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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SliderItem(item: SettingItem, onNavigate: ((String) -> Unit)?) {
    val scope = rememberCoroutineScope()

    val pageId = when(item) {
        is SettingItem.IntSlider -> item.pageId
        is SettingItem.FloatSlider -> item.pageId
        else -> null
    }
    Column(
        modifier = Modifier
            .clickable(enabled = pageId != null) {
                val pageId = when(item) {
                    is SettingItem.IntSlider -> item.pageId
                    is SettingItem.FloatSlider -> item.pageId
                    else -> null
                }
                if(pageId != null) {
                    onNavigate?.invoke(pageId)
                }
            }
            .width(IntrinsicSize.Max)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        when (item) {
            is SettingItem.IntSlider -> {
                val value = item.setting.get()

                SliderText(item)
                Slider(
                    value = value.toFloat(),
                    onValueChange = {
                        item.setting.set(it.roundToInt())
                        item.onValueChange(it.roundToInt())
                    },
                    onValueChangeFinished = {
                        scope.launch {
                            item.setting.save(value)
                        }
                        item.onValueChangeFinished(value)
                    },
                    valueRange = item.range.first.toFloat()..item.range.last.toFloat(),
                    steps = item.steps
                )
            }

            is SettingItem.FloatSlider -> {
                val value = item.setting.get()

                SliderText(item)
                Slider(
                    value = value,
                    onValueChange = {
                        item.setting.set(it)
                        item.onValueChange(it)
                    },
                    onValueChangeFinished = {
                        scope.launch {
                            item.setting.save(value)
                        }
                        item.onValueChangeFinished(value)
                    },
                    valueRange = item.range,
                    steps = item.steps
                )
            }

            else -> throw IllegalArgumentException("Unsupported item type for slider element")
        }
    }
}

@Composable
private fun SliderText(item: SettingItem) {
    val value = when(item) {
        is SettingItem.IntSlider -> item.setting.get()
        is SettingItem.FloatSlider -> item.setting.get()
        else -> throw IllegalArgumentException("Unsupported item type for slider element")
    }
    val valueLabel = when(item) {
        is SettingItem.IntSlider -> item.valueLabel.invoke(value as Int)
        is SettingItem.FloatSlider -> item.valueLabel.invoke(value as Float)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.meta.title.invoke(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if(item.meta.description != null) {
                Text(item.meta.description!!.invoke(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            text = valueLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
