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

package dev.cognitivity.chronal.ui.metronome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.ui.metronome.components.BottomSheet
import dev.cognitivity.chronal.ui.metronome.components.MetronomeDisplay
import dev.cognitivity.chronal.ui.metronome.components.PlayButton
import dev.cognitivity.chronal.ui.metronome.components.TrackList
import dev.cognitivity.chronal.ui.metronome.components.verticalBPMGesture
import dev.cognitivity.chronal.ui.metronome.pages.CircularDisplay
import dev.cognitivity.chronal.ui.metronome.pages.ConductorDisplay
import dev.cognitivity.chronal.ui.metronome.sheets.EditRhythm
import dev.cognitivity.chronal.ui.metronome.sheets.TapTempo
import dev.cognitivity.chronal.ui.metronome.windows.activity
import dev.cognitivity.chronal.ui.metronome.windows.intervals
import dev.cognitivity.chronal.ui.metronome.windows.lastTapTime
import dev.cognitivity.chronal.ui.metronome.windows.paused
import dev.cognitivity.chronal.ui.metronome.windows.setBPM
import dev.cognitivity.chronal.ui.metronome.windows.showRhythmPrimary
import dev.cognitivity.chronal.ui.metronome.windows.showRhythmSecondary
import dev.cognitivity.chronal.ui.metronome.windows.showTempoTapper

var displayDropdown by mutableStateOf(false)
var flipConductor by mutableStateOf(false)
var displayMode by mutableStateOf(0)
var fullscreenMode by mutableStateOf(false)
val modes = listOf(
    R.string.metronome_display_clock to R.drawable.outline_timelapse_24,
    R.string.metronome_display_conductor to R.drawable.outline_person_24,
    R.string.metronome_display_grid to R.drawable.outline_apps_24
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomePageMain(mainActivity: MainActivity, padding: PaddingValues) {
    activity = mainActivity
    val metronome = ChronalApp.getInstance().metronome
    val tracks = metronome.getTracks()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
            .padding(padding)
            .verticalBPMGesture(
                onTap = {
                    paused = !paused
                    if (paused) metronome.stop() else metronome.start()
                    mainActivity.setKeepScreenOn(!paused)
                },
                onHold = {
                    showTempoTapper = true
                },
                onSwipe = { amount ->
                    setBPM(metronome.getTrack(0).bpm + amount)
                }
            )
    ) {
        val isWide = this.maxWidth > 560.dp

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetronomeDisplay(mainActivity, metronome,
                    modifier = Modifier.weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp)
                )
                Column(
                    Modifier.weight(1f),
                ) {
                    TrackList(tracks,
                        modifier = Modifier
                            .heightIn(max = this@BoxWithConstraints.maxHeight * 0.67f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    )
                    Spacer(Modifier.height(16.dp))
                    PlayButton(!paused,
                        onClick = {
                            paused = !it
                            if(paused) metronome.stop() else metronome.start()
                        },
                        modifier = Modifier.fillMaxWidth()
                            .weight(1f)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        } else {
            Column(
                Modifier.fillMaxSize()
                    .padding(16.dp),
            ) {
                TrackList(tracks,
                    modifier = Modifier.heightIn(min = 24.dp, max = min(180.dp, this@BoxWithConstraints.maxHeight * 0.35f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                )
                Spacer(Modifier.height(16.dp))
                MetronomeDisplay(mainActivity, metronome,
                    modifier = Modifier.weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                )
                Spacer(Modifier.height(16.dp))
                PlayButton(!paused,
                    onClick = {
                        paused = !it
                        if(paused) metronome.stop() else metronome.start()
                    },
                    modifier = Modifier.wrapContentHeight()
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        if (fullscreenMode) {
            Dialog(
                onDismissRequest = { fullscreenMode = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .verticalBPMGesture(
                            onTap = {
                                paused = !paused
                                if (paused) metronome.stop() else metronome.start()
                                mainActivity.setKeepScreenOn(!paused)
                            },
                            onHold = {
                                showTempoTapper = true
                            },
                            onSwipe = { amount ->
                                setBPM(metronome.getTrack(0).bpm + amount)
                            }
                        )
                ) {
                    when (displayMode) {
                        0 -> CircularDisplay(metronome)
                        1 -> ConductorDisplay(mainActivity, metronome, flipConductor)
                        else -> CircularDisplay(metronome)
                    }
                    IconButton(
                        onClick = { fullscreenMode = false },
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = context.getString(R.string.generic_close)
                        )
                    }
                }
            }
        }
    }

    if(showTempoTapper) {
        BottomSheet(
            onDismissRequest = {
                showTempoTapper = false
                intervals.clear()
                lastTapTime = 0
            },
        ) {
            TapTempo()
        }
    }
    if(showRhythmPrimary) {
        BottomSheet(
            onDismissRequest = {
                showRhythmPrimary = false
            },
        ) {
            EditRhythm(true) {
                showRhythmPrimary = false
            }
        }
    }
    if(showRhythmSecondary) {
        BottomSheet(
            onDismissRequest = {
                showRhythmSecondary = false
            },
        ) {
            EditRhythm(false) {
                showRhythmSecondary = false
            }
        }
    }
}