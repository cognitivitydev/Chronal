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

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.metronome.MetronomeTrack
import dev.cognitivity.chronal.rhythm.metronome.Beat
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.TrackColorPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

private class WedgeState(initialColor: Color, val isSkipped: Boolean, val isHigh: Boolean, val weight: Float) {
    val color = Animatable(initialColor)
    val highlightAlpha = Animatable(0f)
    val strokeBoost = Animatable(0f)
}

private fun buildWedges(intervals: List<Beat>, measure: Int, inactiveColor: Color): List<WedgeState> {
    return intervals.filter { it.measure == measure }.map {
        WedgeState(inactiveColor, isSkipped = it.duration < 0, isHigh = it.isHigh, weight = abs(it.duration).toFloat())
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.PieRing(track: MetronomeTrack, ringSize: Float, trackPalette: TrackColorPalette, accentOutward: Boolean) {
    val metronome = ChronalApp.getInstance().metronome

    var intervals by remember(track) { mutableStateOf(track.getIntervals()) }
    var wedges by remember(track) { mutableStateOf(buildWedges(intervals, 0, trackPalette.colorContainer)) }

    val coroutineScope = rememberCoroutineScope()
    val animationSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    val highlightSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()
    val colorSpec = MaterialTheme.motionScheme.slowSpatialSpec<Color>()

    LaunchedEffect(track) {
        track.editEvents.collect {
            intervals = track.getIntervals()
            wedges = buildWedges(intervals, 0, trackPalette.colorContainer)
        }
    }

    LaunchedEffect(track) {
        track.updateEvents.collect { beat ->
            val timestamp = metronome.timestamp

            coroutineScope.launch {
                delay(Settings.VISUAL_LATENCY.get().toLong().milliseconds)
                if (!metronome.playing || timestamp != metronome.timestamp) return@launch
                track.vibrate(beat)

                if (beat.index == 0) {
                    wedges = buildWedges(intervals, beat.measure, trackPalette.colorContainer)
                }

                val wedge = wedges.getOrNull(beat.index) ?: return@launch
                if (wedge.isSkipped) return@launch
                wedge.color.snapTo(trackPalette.color)
                wedge.highlightAlpha.snapTo(0.5f)
                wedge.strokeBoost.snapTo(ringSize * 0.5f)

                launch { wedge.color.animateTo(trackPalette.colorContainer, colorSpec) }
                launch { wedge.highlightAlpha.animateTo(0f, highlightSpec) }
                launch { wedge.strokeBoost.animateTo(0f, animationSpec) }
            }
        }
    }

    LaunchedEffect(track) {
        track.pauseEvents.collect { paused ->
            if (!paused) return@collect
            wedges = buildWedges(intervals, 0, trackPalette.colorContainer)
        }
    }

    Box(
        modifier = Modifier.aspectRatio(1f)
            .align(Alignment.Center)
            .padding(9.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val wedgeCount = wedges.size
            if (wedgeCount == 0) return@Canvas

            val radius = (size.minDimension / 2) - ringSize / 2
            val center = Offset(size.width / 2, size.height / 2)
            val gapDegrees = 4f
            val accentExtra = ringSize / 2f
            val accentDirection = if (accentOutward) 1f else -1f

            val totalWeight = wedges.sumOf { it.weight.toDouble() }.toFloat()
            val slotDegrees = wedges.map { it.weight / totalWeight * 360f }
            val offset = -90f - slotDegrees[0] / 2f
            var cumulative = 0f

            for (i in 0 until wedgeCount) {
                val wedge = wedges[i]
                val sweepDegrees = slotDegrees[i] - gapDegrees
                val startAngle = offset + cumulative + gapDegrees / 2f
                cumulative += slotDegrees[i]

                val baseWidth = when {
                    wedge.isSkipped -> ringSize * 0.5f
                    wedge.isHigh -> ringSize + accentExtra
                    else -> ringSize
                }
                val wedgeRadius = if (wedge.isHigh && !wedge.isSkipped) radius + accentDirection * accentExtra / 2f else radius
                val topLeft = Offset(center.x - wedgeRadius, center.y - wedgeRadius)
                val ovalSize = Size(wedgeRadius * 2, wedgeRadius * 2)

                drawArc(
                    color = trackPalette.color.copy(alpha = wedge.highlightAlpha.value),
                    startAngle = startAngle,
                    sweepAngle = sweepDegrees,
                    useCenter = false,
                    topLeft = topLeft,
                    size = ovalSize,
                    style = Stroke(width = baseWidth + wedge.strokeBoost.value)
                )
                drawArc(
                    color = wedge.color.value,
                    startAngle = startAngle,
                    sweepAngle = sweepDegrees,
                    useCenter = false,
                    topLeft = topLeft,
                    size = ovalSize,
                    style = Stroke(width = baseWidth)
                )
            }
        }
    }
}
