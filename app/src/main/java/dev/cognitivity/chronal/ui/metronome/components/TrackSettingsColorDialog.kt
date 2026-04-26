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

package dev.cognitivity.chronal.ui.metronome.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.types.json.TrackColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackSettingsColorDialog(color: TrackColor, onDismissRequest: () -> Unit, onColorChange: (TrackColor) -> Unit) {
    var isCustomMode by remember { mutableStateOf(color is TrackColor.Custom) }
    var deviceSelection by remember {
        mutableStateOf(
            when (color) {
                is TrackColor.Secondary -> TrackColor.Secondary
                else -> TrackColor.Primary
            }
        )
    }
    val initialColor = when (val color = color) {
        is TrackColor.Custom -> color.value
        is TrackColor.Primary -> MaterialTheme.colorScheme.primary.toArgb()
        is TrackColor.Secondary -> MaterialTheme.colorScheme.tertiary.toArgb()
    }
    var red by remember { mutableIntStateOf(initialColor.red) }
    var green by remember { mutableIntStateOf(initialColor.green) }
    var blue by remember { mutableIntStateOf(initialColor.blue) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.track_settings_color_dialog_title)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val deviceLabel = stringResource(R.string.track_settings_color_device)
                val customLabel = stringResource(R.string.track_settings_color_custom)
                ButtonGroup(
                    overflowIndicator = { menuState ->
                        ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    toggleableItem(
                        checked = !isCustomMode,
                        onCheckedChange = {
                            isCustomMode = false
                        },
                        label = deviceLabel,
                        weight = 1f
                    )
                    toggleableItem(
                        checked = isCustomMode,
                        onCheckedChange = {
                            isCustomMode = true
                        },
                        label = customLabel,
                        weight = 1f
                    )
                }

                if (!isCustomMode) {
                    DeviceOptions(deviceSelection) { deviceSelection = it }
                } else {
                    ColorPicker(red, green, blue) { r,g,b ->
                        red = r
                        green = g
                        blue = b
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedColor = if (isCustomMode) {
                        TrackColor.Custom(Color(red, green, blue).toArgb())
                    } else {
                        deviceSelection
                    }
                    onColorChange(selectedColor)
                }
            ) {
                Text(stringResource(R.string.generic_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.generic_cancel))
            }
        }
    )
}

@Composable
private fun DeviceOptions(color: TrackColor, onColorChange: (TrackColor) -> Unit) {
    Column {
        DeviceOption(
            label = stringResource(R.string.track_settings_color_primary),
            selected = color is TrackColor.Primary,
            onSelect = { onColorChange(TrackColor.Primary) },
            topRounded = true,
            bottomRounded = false
        )
        DeviceOption(
            label = stringResource(R.string.track_settings_color_secondary),
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.tertiary,
            ),
            selected = color is TrackColor.Secondary,
            onSelect = { onColorChange(TrackColor.Secondary) },
            topRounded = false,
            bottomRounded = true
        )
    }
}

@Composable
private fun DeviceOption(
    label: String, colors: RadioButtonColors = RadioButtonDefaults.colors(),
    selected: Boolean, onSelect: () -> Unit,
    topRounded: Boolean, bottomRounded: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }

    val shape = RoundedCornerShape(
        topStart = if (topRounded) 12.dp else 6.dp,
        topEnd = if (topRounded) 12.dp else 6.dp,
        bottomStart = if (bottomRounded) 12.dp else 6.dp,
        bottomEnd = if (bottomRounded) 12.dp else 6.dp
    )
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp)
            .defaultMinSize(minHeight = 64.dp),
        onClick = onSelect,
        interactionSource = interactionSource
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect,
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = colors
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if(selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ColorPicker(red: Int, green: Int, blue: Int, onColorChange: (Int, Int, Int) -> Unit) {
    var redExpanded by remember { mutableStateOf(false) }
    var greenExpanded by remember { mutableStateOf(false) }
    var blueExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp)
                .clip(CircleShape)
                .background(Color(red, green, blue))
        )
        Text(
            text = stringResource(R.string.track_settings_color_rgb, red, green, blue),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            ColorSlider(
                label = stringResource(R.string.track_settings_color_red),
                expanded = redExpanded,
                onExpandChange = { redExpanded = !redExpanded },
                value = red,
                onValueChange = {
                    onColorChange(it, green, blue)
                },
                topRounded = true,
                bottomRounded = false
            )
        }
        item {
            ColorSlider(
                label = stringResource(R.string.track_settings_color_green),
                expanded = greenExpanded,
                onExpandChange = { greenExpanded = !greenExpanded },
                value = green,
                onValueChange = {
                    onColorChange(red, it, blue)
                },
                topRounded = false,
                bottomRounded = false
            )
        }
        item {
            ColorSlider(
                label = stringResource(R.string.track_settings_color_blue),
                expanded = blueExpanded,
                onExpandChange = { blueExpanded = !blueExpanded },
                value = blue,
                onValueChange = {
                    onColorChange(red, green, it)
                },
                topRounded = false,
                bottomRounded = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColorSlider(
    label: String,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    topRounded: Boolean,
    bottomRounded: Boolean,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(
        topStart = if (topRounded) 16.dp else 8.dp,
        topEnd = if (topRounded) 16.dp else 8.dp,
        bottomStart = if (bottomRounded) 16.dp else 8.dp,
        bottomEnd = if (bottomRounded) 16.dp else 8.dp
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.5.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(onClick = onExpandChange)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
            )
            Icon(
                imageVector = if(expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(if(expanded) R.string.generic_menu_collapse else R.string.generic_menu_expand),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        if(expanded) {
            Slider(
                value = value.toFloat(),
                valueRange = 0f..255f,
                onValueChange = { onValueChange(it.toInt()) },
            )
        }
    }
}