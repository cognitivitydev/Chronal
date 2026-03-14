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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.Metronome
import dev.cognitivity.chronal.toPx
import dev.cognitivity.chronal.ui.metronome.components.CircularClock
import dev.cognitivity.chronal.ui.metronome.components.TempoChanger
import dev.cognitivity.chronal.ui.metronome.windows.setBPM

@Composable
fun CircularDisplay(metronome: Metronome, modifier: Modifier = Modifier) {
    val tracks = metronome.getTracks()
    Box(
        modifier = modifier.fillMaxSize()
        ) {
        Box(
            modifier = Modifier.fillMaxHeight()
                .aspectRatio(1f)
                .align(Alignment.Center)
        ) {
            CircularClock(tracks[0],
                trackSize = 6.dp.toPx(),
                trackOff = MaterialTheme.colorScheme.onPrimary,
                trackPrimary = MaterialTheme.colorScheme.primary,
                trackSecondary = MaterialTheme.colorScheme.secondary
            )
        }
        if (tracks.size > 1 && tracks[1].enabled) {
            Box(
                modifier = Modifier.fillMaxHeight()
                    .padding(24.dp)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
            ) {
                CircularClock(tracks[1],
                    trackSize = 4.dp.toPx(),
                    trackOff = MaterialTheme.colorScheme.onTertiary,
                    trackPrimary = MaterialTheme.colorScheme.tertiary,
                    trackSecondary = MaterialTheme.colorScheme.secondary
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
            val track = metronome.getTrack(0)

            TempoChanger(
                modifier = Modifier.align(Alignment.Center),
                bpm = track.bpm,
                onIncrement = {
                    setBPM(track.bpm + 1)
                },
                onDecrement = {
                    setBPM(track.bpm - 1)
                },
                onClick = { // TODO
                    track.bpm += 0.01f
                }
            )
        }
    }
}