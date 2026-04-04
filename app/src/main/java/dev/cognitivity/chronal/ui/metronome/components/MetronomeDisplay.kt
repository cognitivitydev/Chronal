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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.metronome.Metronome
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.ui.metronome.DisplayMode
import dev.cognitivity.chronal.ui.metronome.MetronomeViewModel
import dev.cognitivity.chronal.ui.metronome.pages.CircularDisplay
import dev.cognitivity.chronal.ui.metronome.pages.ConductorDisplay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MetronomeDisplay(
    viewModel: MetronomeViewModel,
    metronome: Metronome,
    modifier: Modifier = Modifier,
) {
    val displayMode by viewModel.displayMode.collectAsState()
    val tracks by viewModel.tracks.collectAsState()

    Box(
        modifier = modifier
    ) {
        when(displayMode) {
            DisplayMode.CLOCK -> CircularDisplay(viewModel, tracks)
            DisplayMode.CONDUCTOR -> ConductorDisplay(viewModel, metronome, tracks)
            DisplayMode.GRID -> CircularDisplay(viewModel, tracks)
        }

        Row(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DisplaySelector(viewModel)

            if(displayMode == DisplayMode.CONDUCTOR) {
                IconToggleButton(
                    checked = viewModel.flipConductor.collectAsState().value,
                    onCheckedChange = { viewModel.setFlipConductor(it) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_flip_24),
                        contentDescription = context.getString(R.string.metronome_conductor_flip)
                    )
                }
            }
        }
        IconButton(
            onClick = { viewModel.setFullscreenMode(true) },
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
fun DisplaySelector(viewModel: MetronomeViewModel) {
    val modes = listOf(
        R.string.metronome_display_clock to R.drawable.outline_timelapse_24,
        R.string.metronome_display_conductor to R.drawable.outline_person_24,
        R.string.metronome_display_grid to R.drawable.outline_apps_24
    )

    val displayMode by viewModel.displayMode.collectAsState()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(context.getString(R.string.metronome_switch_display_mode)) } },
        state = rememberTooltipState(),
    ) {
        IconButton(
            onClick = { viewModel.setModesExpanded(true) }
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_browse_24),
                contentDescription = null
            )
        }
        DropdownMenuPopup(
            expanded = viewModel.modesExpanded.collectAsState().value,
            onDismissRequest = { viewModel.setModesExpanded(false) },
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(displayMode.ordinal, modes.size),
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
                        checked = displayMode.ordinal == index,
                        onCheckedChange = { viewModel.setDisplayMode(DisplayMode.entries[index]) },
                        shapes = MenuDefaults.itemShape(index, modes.size),
                        enabled = index != 2 // TODO
                    )
                }
            }
        }
    }
}