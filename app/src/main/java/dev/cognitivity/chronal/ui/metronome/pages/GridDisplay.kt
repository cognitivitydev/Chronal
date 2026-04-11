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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.metronome.MetronomeTrack
import dev.cognitivity.chronal.ui.metronome.MetronomeViewModel
import dev.cognitivity.chronal.ui.metronome.components.GridDisplayItem
import dev.cognitivity.chronal.ui.metronome.components.TempoChanger
import kotlin.math.ceil
import kotlin.math.sqrt

@Composable
fun GridDisplay(viewModel: MetronomeViewModel, tracks: List<MetronomeTrack>, modifier: Modifier = Modifier) {
    val displayTracks = tracks.withIndex().filter { it.value.enabled }
    if (displayTracks.isEmpty()) return

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val visibleCount = displayTracks.size
            val horizontal = maxWidth > maxHeight
            val (columns, rows) = remember(visibleCount, horizontal) {
                getGridDimensions(visibleCount, horizontal)
            }

            val maxCellWidth = (maxWidth / columns).coerceAtLeast(0.dp)
            val maxCellHeight = (maxHeight / rows).coerceAtLeast(0.dp)
            val cellSize = minOf(maxCellWidth, maxCellHeight)
                .coerceAtMost(minOf(maxWidth, maxHeight) * 0.8f)

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for(column in 0 until columns) {
                            val trackIndex = row * columns + column
                            if (trackIndex >= displayTracks.size) continue
                            val trackEntry = displayTracks[trackIndex]
                            key(trackEntry.index) {
                                GridDisplayItem(
                                    index = trackEntry.index,
                                    track = trackEntry.value,
                                    modifier = Modifier.size(cellSize)
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            val metronome = ChronalApp.getInstance().metronome

            TempoChanger(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Center),
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

private fun getGridDimensions(count: Int, horizontal: Boolean): Pair<Int, Int> {
    if (count <= 1) return 1 to 1

    val maxAspectRatio = 2
    val maxEmptySlots = maxOf(2, (count * 0.2).toInt())

    var bestCols = count
    var bestRows = 1
    var bestScore = Double.POSITIVE_INFINITY

    for(rows in 1..count) {
        val cols = ceil(count / rows.toDouble()).toInt()
        val emptySlots = rows * cols - count
        if(emptySlots > maxEmptySlots) continue

        val aspectRatio = maxOf(cols, rows).toDouble() / minOf(cols, rows)
        if(aspectRatio > maxAspectRatio) continue

        val score = aspectRatio + emptySlots
        if(score < bestScore) {
            bestScore = score
            bestCols = cols
            bestRows = rows
        }
    }

    if(!bestScore.isFinite()) {
        bestCols = ceil(sqrt(count.toDouble())).toInt()
        bestRows = ceil(count / bestCols.toDouble()).toInt()
    }

    val max = maxOf(bestCols, bestRows)
    val min = minOf(bestCols, bestRows)
    return if(horizontal) max to min else min to max
}