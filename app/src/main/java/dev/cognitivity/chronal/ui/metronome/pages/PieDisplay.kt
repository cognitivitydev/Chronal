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

package dev.cognitivity.chronal.ui.metronome.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.metronome.MetronomeTrack
import dev.cognitivity.chronal.toPx
import dev.cognitivity.chronal.ui.metronome.MetronomeViewModel
import dev.cognitivity.chronal.ui.metronome.components.PieRing
import dev.cognitivity.chronal.ui.metronome.components.TempoChanger

@Composable
fun PieDisplay(viewModel: MetronomeViewModel, tracks: List<MetronomeTrack>, modifier: Modifier = Modifier) {
    val displayTracks = tracks.filter { it.enabled }
    if (displayTracks.isEmpty()) return

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxHeight()
                .aspectRatio(1f)
                .align(Alignment.Center)
        ) {
            PieRing(displayTracks[0],
                ringSize = 6.dp.toPx(),
                trackPalette = displayTracks[0].color.getPalette()
            )
        }
        if(displayTracks.size > 1 && displayTracks[1].enabled) {
            Box(
                modifier = Modifier.fillMaxHeight()
                    .padding(24.dp)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
            ) {
                PieRing(displayTracks[1],
                    ringSize = 4.dp.toPx(),
                    trackPalette = displayTracks[1].color.getPalette()
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxHeight()
                .padding(32.dp)
                .aspectRatio(1f)
                .align(Alignment.Center)
        ) {
            val metronome = ChronalApp.getInstance().metronome

            TempoChanger(
                modifier = Modifier.align(Alignment.Center),
                bpm = metronome.bpm,
                onIncrement = {
                    viewModel.setBpm(metronome.bpm + 1)
                },
                onDecrement = {
                    viewModel.setBpm(metronome.bpm - 1)
                },
                onClick = {
                    viewModel.setShowBpmDialog(true)
                }
            )
        }
    }
}
