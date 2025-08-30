/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2025  cognitivity
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

package dev.cognitivity.chronal.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.Setting
import dev.cognitivity.chronal.ui.settings.windows.SettingOption

@Composable
fun ExpandableOption(
    setting: Setting<*>,
    menu: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val background by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.surfaceContainerLow
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 250, easing = EaseInOutCubic),
        label = "background"
    )

    val rotation = remember { Animatable(0f) }

    LaunchedEffect(expanded) {
        rotation.snapTo(if (expanded) 180f else 0f)
        rotation.animateTo(if (expanded) 0f else 180f)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(0.dp, 0.dp, 0.dp, if (!expanded) 0.dp else 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(background)
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        SettingOption(
            name = ChronalApp.context.getString(setting.key.settingName),
            hint = ChronalApp.context.getString(setting.hint),
            onClick = {
                expanded = !expanded
            },
            interactionSource = interactionSource,
            indication = null
        ) {
            IconButton(
                onClick = {
                    expanded = !expanded
                },
                interactionSource = interactionSource,
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = ChronalApp.context.getString(if (expanded) R.string.generic_menu_collapse else R.string.generic_menu_expand),
                    modifier = Modifier.rotate(rotation.value),
                )
            }
        }

        if (expanded) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .padding(8.dp)
            ) {
                menu()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpandableButtonRow(setting: Setting<*>, labels: List<String>, isSelected: (Int, Int) -> Boolean = { index, selection -> index == selection }, onClick: (Int) -> Int) {
    var selection by remember { mutableIntStateOf(setting.value as Int) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        for (i in labels.indices) {
            ToggleButton(
                checked = isSelected(i, selection),
                onCheckedChange = {
                    selection = onClick(i)
                },
                shapes = when (i) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    labels.size - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                if (isSelected(i, selection)) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = ChronalApp.context.getString(R.string.generic_selected),
                    )
                    Spacer(Modifier.width(ToggleButtonDefaults.IconSpacing))
                }
                Text(labels[i])
            }
        }
    }
}

@Composable
fun ExpandableSlider(setting: Setting<*>, range: ClosedFloatingPointRange<Float>,
                     minText: String, recommended: String, info: @Composable BoxScope.() -> Unit = {},
                     onValueChange: (Float) -> Unit, valueText: (Float) -> String, content: @Composable ColumnScope.() -> Unit = {}) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.align(Alignment.CenterVertically)
                    .padding(16.dp, 0.dp)
            ) {
                Text(
                    text = minText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }

            var value by remember {
                mutableFloatStateOf(
                    if (setting.value is Int) (setting.value as Int).toFloat()
                    else setting.value as Float
                )
            }
            Slider(
                modifier = Modifier.weight(1f),
                value = value,
                onValueChange = {
                    value = it
                    onValueChange(it)
                },
                valueRange = range,
            )

            Box(
                modifier = Modifier.align(Alignment.CenterVertically)
                    .padding(16.dp, 0.dp)
            ) {
                Text(
                    text = valueText(value),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
        Box(
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 2.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            info()
            Row(
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = ChronalApp.context.getString(R.string.setting_info_recommended_value),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = recommended,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        content()
    }
}