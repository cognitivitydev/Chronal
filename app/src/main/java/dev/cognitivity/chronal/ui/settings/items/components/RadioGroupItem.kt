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

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.ui.settings.items.SettingItem

@Composable
fun SettingRadioGroupRow(item: SettingItem.RadioGroupItem) {
    var selection by remember { mutableIntStateOf(item.setting.get()) }

    for((index, option) in item.options.withIndex()) {
        RadioOptionItem(
            option = option,
            selected = option.id == selection,
            onSelect = {
                selection = option.id
                item.setting.set(option.id)
                item.onOptionSelected(option.id)
            },
            topRounded = index == 0,
            bottomRounded = index == item.options.size - 1
        )
    }
}

@Composable
private fun RadioOptionItem(option: SettingItem.RadioOption, selected: Boolean, onSelect: () -> Unit, topRounded: Boolean, bottomRounded: Boolean) {
    val interactionSource = remember { MutableInteractionSource() }

    val shape = RoundedCornerShape(
        topStart = if (topRounded) 12.dp else 6.dp,
        topEnd = if (topRounded) 12.dp else 6.dp,
        bottomStart = if (bottomRounded) 12.dp else 6.dp,
        bottomEnd = if (bottomRounded) 12.dp else 6.dp
    )
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
                selected = selected,
                onClick = { onSelect() },
                interactionSource = interactionSource
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(context.getString(option.title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if(option.description != null) {
                    Text(context.getString(option.description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}