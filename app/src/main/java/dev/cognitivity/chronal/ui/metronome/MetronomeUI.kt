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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import dev.cognitivity.chronal.metronome.MetronomeModifier
import dev.cognitivity.chronal.ui.metronome.components.BottomSheet
import dev.cognitivity.chronal.ui.metronome.components.MetronomeDisplay
import dev.cognitivity.chronal.ui.metronome.components.PlayButton
import dev.cognitivity.chronal.ui.metronome.components.TempoControlsDialog
import dev.cognitivity.chronal.ui.metronome.components.TrackList
import dev.cognitivity.chronal.ui.metronome.components.verticalBPMGesture
import dev.cognitivity.chronal.ui.metronome.pages.CircularDisplay
import dev.cognitivity.chronal.ui.metronome.pages.ConductorDisplay
import dev.cognitivity.chronal.ui.metronome.sheets.TapTempo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomePageMain(mainActivity: MainActivity, viewModel: MetronomeViewModel, padding: PaddingValues) {
    val metronome = ChronalApp.getInstance().metronome
    val tracks = metronome.getTracks()
    val playing by viewModel.playing.collectAsState()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
            .padding(padding)
            .verticalBPMGesture(
                onTap = {
                    viewModel.setPlaying(!playing)
                    mainActivity.setKeepScreenOn(!playing)
                },
                onHold = {
                    viewModel.setShowTapTempo(true)
                },
                onSwipe = { amount ->
                    viewModel.setBpm(metronome.bpm + amount)
                }
            )
    ) {
        val isWide = this.maxWidth > 560.dp

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetronomeDisplay(viewModel, metronome,
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
                    PlayButton(mainActivity, viewModel,
                        onClick = {
                            viewModel.setPlaying(!playing)
                            mainActivity.setKeepScreenOn(!playing)
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
                MetronomeDisplay(viewModel, metronome,
                    modifier = Modifier.weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                )
                Spacer(Modifier.height(16.dp))
                PlayButton(mainActivity, viewModel,
                    onClick = {
                        viewModel.setPlaying(!playing)
                        mainActivity.setKeepScreenOn(!playing)
                    },
                    modifier = Modifier.wrapContentHeight()
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        if(viewModel.fullscreenMode.collectAsState().value) {
            Dialog(
                onDismissRequest = { viewModel.setFullscreenMode(false) },
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
                                viewModel.setPlaying(!playing)
                                mainActivity.setKeepScreenOn(!playing)
                            },
                            onHold = {
                                viewModel.setShowTapTempo(true)
                            },
                            onSwipe = { amount ->
                                viewModel.setBpm(metronome.bpm + amount)
                            }
                        )
                ) {
                    when(viewModel.displayMode.collectAsState().value) {
                        DisplayMode.CLOCK -> CircularDisplay(viewModel, metronome)
                        DisplayMode.CONDUCTOR -> ConductorDisplay(viewModel, metronome)
                        DisplayMode.GRID -> CircularDisplay(viewModel, metronome)
                    }
                    IconButton(
                        onClick = { viewModel.setFullscreenMode(false) },
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

    if(viewModel.showTapTempo.collectAsState().value) {
        BottomSheet(
            onDismissRequest = {
                viewModel.setShowTapTempo(false)
                viewModel.setIntervals(emptyList())
                viewModel.setLastTapTime(0L)
            },
        ) {
            TapTempo(viewModel)
        }
    }
    if(viewModel.showBpmDialog.collectAsState().value) {
        TempoControlsDialog(
            metronome, viewModel,
            onDismissRequest = {
                viewModel.setShowBpmDialog(false)
            }
        )
    }
}