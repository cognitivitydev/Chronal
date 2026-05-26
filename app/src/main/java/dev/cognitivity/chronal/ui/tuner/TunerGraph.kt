/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2025-2026  cognitivity
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

package dev.cognitivity.chronal.ui.tuner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.round
import dev.cognitivity.chronal.tuner.Pitch
import dev.cognitivity.chronal.tuner.Pitch.Companion.midiToFrequency
import kotlin.math.abs

@Composable
fun BoxScope.TunerGraph(
    history: List<Pair<Long, Float>>,
    fullscreen: Boolean = false
) {
    val filteredHistory = removeOutliers(history)

    val minFreq = if (filteredHistory.isEmpty()) 0f else filteredHistory.minOf { it.second }
    val maxFreq = if (filteredHistory.isEmpty()) 0f else filteredHistory.maxOf { it.second }

    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val outline = MaterialTheme.colorScheme.outline
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    Canvas(Modifier.fillMaxSize()) {
        if (filteredHistory.isEmpty()) return@Canvas

        val yRange = maxFreq - minFreq

        val noteFreqs = generateSequence(0) { it + 1 }
            .map { midiToFrequency(it) }
            .takeWhile { it <= maxFreq }
            .filter { it >= minFreq }
            .toList()

        if((noteFreqs.size < 12 && !fullscreen) || (noteFreqs.size < 24 && fullscreen)) { // max one octave minimized, two octaves maximized
            for (freq in noteFreqs) {
                val y = size.height - ((freq - minFreq) / yRange * size.height)
                drawLine(
                    color = outlineVariant,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        var lastPoint: Offset? = null
        var lastTime: Long? = null

        for (pair in filteredHistory) {
            val x = ((pair.first - filteredHistory.first().first) / (filteredHistory.last().first - filteredHistory.first().first).toFloat() * size.width)
            val y = size.height - ((pair.second - minFreq) / yRange * size.height)
            val currentPoint = Offset(x, y)

            if (lastPoint != null && lastTime != null) {
                val timeDiff = pair.first - lastTime
                if (timeDiff <= 250) { // within roughly 5 updates
                    val centsOff = Pitch.fromFrequency(pair.second).cents
                    val color = when {
                        abs(centsOff) >= 40 -> outline
                        abs(centsOff) >= 30 -> secondary
                        abs(centsOff) >= 20 -> tertiary
                        abs(centsOff) >= 5 -> primary
                        else -> onSurface
                    }
                    drawLine(
                        color = color,
                        start = lastPoint,
                        end = currentPoint,
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            lastPoint = currentPoint
            lastTime = pair.first
        }
    }

    val max = Pitch.fromFrequency(maxFreq)
    val min = Pitch.fromFrequency(minFreq)
    val showCents = maxFreq - minFreq < 100f && !max.cents.isNaN() && !min.cents.isNaN()
    Box(
        modifier = Modifier.align(Alignment.TopStart)
            .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
            .padding(horizontal = 4.dp)
    ) {
        val string = if(showCents) "${max.toDisplayName().name}, ${if(max.cents >= 0) "+" else ""}${max.cents.round(1)}"
            else max.toDisplayName().name
        Text(
            text = string,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Box(
        modifier = Modifier.align(Alignment.BottomStart)
            .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
            .padding(horizontal = 4.dp)
    ) {
        val string = if(showCents) "${min.toDisplayName().name}, ${if(min.cents >= 0) "+" else ""}${min.cents.round(1)}"
            else min.toDisplayName().name
        Text(
            text = string,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Icon(
        painter = painterResource(if(fullscreen) R.drawable.outline_fullscreen_exit_24 else R.drawable.baseline_fullscreen_24),
        contentDescription = context.getString(R.string.generic_fullscreen),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.align(Alignment.BottomEnd)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .size(16.dp),
    )
}

fun removeOutliers(data: List<Pair<Long, Float>>): List<Pair<Long, Float>> {
    if (data.size < 3) return data

    val filtered = mutableListOf<Pair<Long, Float>>()
    for (i in 1 until data.size - 1) {
        val prev = data[i - 1].second
        val curr = data[i].second
        val next = data[i + 1].second

        val minNeighbor = minOf(prev, next)
        val maxNeighbor = maxOf(prev, next)

        val lowerBound = minNeighbor / 4f
        val upperBound = maxNeighbor * 4f

        if (curr in lowerBound..upperBound) {
            filtered.add(data[i])
        }
    }
    return filtered
}
