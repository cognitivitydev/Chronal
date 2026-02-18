package dev.cognitivity.chronal.ui.settings.items.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import kotlinx.coroutines.launch
import dev.cognitivity.chronal.R

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