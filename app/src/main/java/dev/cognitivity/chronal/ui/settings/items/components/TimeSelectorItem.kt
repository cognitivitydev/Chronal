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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectorItem(item: SettingItem.TimeSelector) {
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var time by remember { mutableIntStateOf(item.setting.get()) }
    val timePickerState = rememberTimePickerState(initialHour = time / 60, initialMinute = time % 60)
    val showingPicker = remember { mutableStateOf(true) }

    val onTimeSelected = {
        time = timePickerState.hour * 60 + timePickerState.minute
        scope.launch {
            item.setting.save(time)
            item.onTimeSelected(time)
        }
    }

    Column(
        modifier = Modifier.clickable {
                showDialog = true
            }
            .width(IntrinsicSize.Max)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(context.getString(item.meta.title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        if(item.meta.description != null) {
            Text(item.meta.description.invoke(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if(showDialog) {
        TimePickerDialog(
            title = {
                Text(
                    context.getString(
                        if (showingPicker.value) R.string.generic_picker_select_time
                        else R.string.generic_picker_input_time
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onTimeSelected()
                }) {
                    Text(context.getString(R.string.generic_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(context.getString(R.string.generic_cancel))
                }
            },
            onDismissRequest = { showDialog = false },
            modeToggleButton = {
                IconButton(
                    onClick = { showingPicker.value = !showingPicker.value }) {
                    val icon = if (showingPicker.value) R.drawable.outline_keyboard_24
                        else R.drawable.outline_schedule_24
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = context.getString(
                            if (showingPicker.value) R.string.generic_picker_switch_to_input
                                else R.string.generic_picker_switch_to_picker
                        )
                    )
                }
            }
        ) {
            if (showingPicker.value) {
                TimePicker(state = timePickerState)
            } else {
                TimeInput(state = timePickerState)
            }
        }
    }
}