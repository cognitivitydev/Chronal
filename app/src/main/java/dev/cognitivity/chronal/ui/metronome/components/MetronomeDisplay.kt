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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.Metronome
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.ui.metronome.displayDropdown
import dev.cognitivity.chronal.ui.metronome.displayMode
import dev.cognitivity.chronal.ui.metronome.flipConductor
import dev.cognitivity.chronal.ui.metronome.fullscreenMode
import dev.cognitivity.chronal.ui.metronome.modes
import dev.cognitivity.chronal.ui.metronome.pages.CircularDisplay
import dev.cognitivity.chronal.ui.metronome.pages.ConductorDisplay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MetronomeDisplay(
    mainActivity: MainActivity,
    metronome: Metronome,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
    ) {
        when (displayMode) {
            0 -> CircularDisplay(metronome)
            1 -> ConductorDisplay(mainActivity, metronome, flipConductor)
            else -> CircularDisplay(metronome)
        }

        Row(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DisplaySelector()

            if(displayMode == 1) { // Conductor
                IconToggleButton(
                    checked = flipConductor,
                    onCheckedChange = { flipConductor = it }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_flip_24),
                        contentDescription = context.getString(R.string.metronome_conductor_flip)
                    )
                }
            }
        }
        IconButton(
            onClick = { fullscreenMode = true },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_fullscreen_24),
                contentDescription = context.getString(R.string.generic_fullscreen)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DisplaySelector() {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(context.getString(R.string.metronome_switch_display_mode)) } },
        state = rememberTooltipState(),
    ) {
        IconButton(
            onClick = { displayDropdown = true }
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_browse_24),
                contentDescription = null
            )
        }
        DropdownMenuPopup(
            expanded = displayDropdown,
            onDismissRequest = { displayDropdown = false },
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(displayMode, modes.size)
            ) {
                MenuDefaults.Label {
                    Text(
                        context.getString(R.string.metronome_switch_display_mode),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(MenuDefaults.HorizontalDividerPadding)
                )
                for((index, mode) in modes.withIndex()) {
                    val (string, icon) = mode

                    DropdownMenuItem(
                        text = { Text(context.getString(string)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(icon),
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                contentDescription = null
                            )
                        },
                        checkedLeadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                contentDescription = null
                            )
                        },
                        checked = displayMode == index,
                        onCheckedChange = { displayMode = index },
                        shapes = MenuDefaults.itemShape(index, modes.size),
                        enabled = index != 2 // TODO
                    )
                }
            }
        }
    }
}