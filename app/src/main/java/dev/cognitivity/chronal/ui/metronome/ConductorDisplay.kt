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

package dev.cognitivity.chronal.ui.metronome

import android.content.Context
import android.graphics.Matrix
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.vibratorManager
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmAtom
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmTuplet
import dev.cognitivity.chronal.round
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.toPx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

// TODO
//   - pick display track
//   - separate versions for different time signatures (5/4, 6/8...)
@Composable
fun ConductorDisplay() {
    val metronome = ChronalApp.getInstance().metronome
    val displayTrack = metronome.getTrack(0)

    val coroutineScope = rememberCoroutineScope()
    var loopIndex by remember { mutableIntStateOf(0) }
    var currentTimeSignature by remember { mutableStateOf(displayTrack.getRhythm().measures[0].timeSig) }
    var (path, segments) = getConductorPath(currentTimeSignature.first)
    val currentBeat = remember { Animatable(0f) }

    var flipped by remember { mutableStateOf(false) }

    var lastBeatId: Pair<Int, Int>? = null // measure index and beat index

    displayTrack.setEditListener(4) {
        currentTimeSignature = displayTrack.getRhythm().measures[0].timeSig
        val newPath = getConductorPath(currentTimeSignature.first)
        path = newPath.first
        segments = newPath.second
    }

    displayTrack.setUpdateListener(4) { beat ->
        val rhythm = displayTrack.getRhythm()
        val timestamp = metronome.timestamp


        coroutineScope.launch {
            delay(Settings.VISUAL_LATENCY.get().toLong())
            if(!metronome.playing || timestamp != metronome.timestamp) return@launch
            if(!Settings.METRONOME_VIBRATIONS.get()) return@launch
            if(beat.duration >= 0f) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && vibratorManager != null) {
                    val vibration = if(beat.isHigh) VibrationEffect.createOneShot(10, 255) else VibrationEffect.createOneShot(3, 255)
                    vibratorManager!!.vibrate(CombinedVibration.createParallel(vibration))
                } else {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(if(beat.isHigh) 10 else 3)
                }
            }
        }

        coroutineScope.launch {
            delay(Settings.VISUAL_LATENCY.get().toLong())
            if (!metronome.playing || timestamp != metronome.timestamp) return@launch

            if (beat.measure == 0 && beat.index == 0) loopIndex++

            val measure = rhythm.measures[beat.measure]
            currentTimeSignature = measure.timeSig

            val beatDuration = 1f / currentTimeSignature.second.toFloat()
            val beatId = Pair(beat.measure, beat.index)

            if (lastBeatId != beatId) {
                lastBeatId = beatId

                var globalIndex = 0
                var currentDuration = 0.0
                for(element in measure.elements) {
                    if(globalIndex >= beat.index) break
                    when(element) {
                        is RhythmAtom -> {
                            globalIndex++
                            currentDuration += abs(element.baseDuration)
                        }
                        is RhythmTuplet -> {
                            for(tuple in element.notes) {
                                if(globalIndex >= beat.index) break
                                globalIndex++
                                currentDuration += abs(tuple.baseDuration)
                            }
                        }
                    }
                }
                if(currentDuration.round(6).mod(beatDuration) != 0.0) {
                    return@launch
                }

                val startAnimation = beatId
                var currentAnimation = currentDuration
                while(startAnimation == lastBeatId) {
                    val startValue = currentAnimation
                    val endValue = currentAnimation + beatDuration

                    currentBeat.snapTo(startValue.toFloat())
                    currentBeat.animateTo(
                        targetValue = endValue.toFloat(),
                        animationSpec = tween(
                            durationMillis = (beatDuration * 60000 / displayTrack.bpm * displayTrack.beatValue).toInt(),
                            easing = CubicBezierEasing(0f, 0f, 0.5f, 0.75f)
                        )
                    )
                    currentAnimation += beatDuration
                }
            }
        }
    }
    displayTrack.setPauseListener(4) { playing ->
        if(!playing) return@setPauseListener
        coroutineScope.launch {
            currentBeat.snapTo(0f)
            loopIndex = 0
        }
    }

    Column {
        Box(
            modifier = Modifier.fillMaxSize()
                .weight(1f)
        ) {
            if(segments.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_person_off_24),
                        contentDescription = context.getString(R.string.generic_error),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = context.getString(R.string.metronome_conductor_not_supported),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                FilledIconToggleButton(
                    checked = flipped,
                    onCheckedChange = { flipped = it },
                    modifier = Modifier.align(Alignment.TopStart)
                        .padding(horizontal = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_flip_24),
                        contentDescription = context.getString(R.string.metronome_conductor_flip)
                    )
                }

                DrawPath(flipped, path.copy(), 0f, 0f, color = MaterialTheme.colorScheme.secondaryContainer, style = Stroke(width = 3.dp.toPx()))
                if(!metronome.playing && currentBeat.value == 0f) {
                    return@Box
                }

                val measureDuration = currentTimeSignature.first / currentTimeSignature.second.toFloat()

                val reorderedSegments = segments.dropLast(1)
                val beatDuration = 1f / currentTimeSignature.second
                var remaining = if(segments.size > 2) 50f else 10f

                // downbeats
                val downStart = 0f
                val downEnd = segments[0]
                val downStartTime = measureDuration - (beatDuration / 3)
                val downEndTime = measureDuration

                if (currentBeat.value in downStartTime..downEndTime) {
                    val progress = (currentBeat.value - downStartTime) / (downEndTime - downStartTime)
                    val selectedEnd = downStart + (downEnd - downStart) * progress
                    val selectedStart = selectedEnd - remaining

                    if (selectedStart <= 0f) {
                        DrawPath(flipped, path.copy(), 0f, selectedEnd, color = MaterialTheme.colorScheme.primary)
                        remaining -= abs(selectedEnd)
                    } else {
                        DrawPath(flipped, path.copy(), selectedStart, selectedEnd, color = MaterialTheme.colorScheme.primary)
                        remaining = 0f
                    }
                }

                // other beats
                for(reversedIndex in reorderedSegments.indices) {
                    val i = reorderedSegments.size - 1 - reversedIndex
                    val start = reorderedSegments[i]
                    val end = if (i == reorderedSegments.lastIndex) segments.last() else reorderedSegments[i + 1]
                    val length = end - start

                    val targetBeat = beatDuration * i
                    val nextBeat = beatDuration * (i + 1)

                    if (currentBeat.value in targetBeat..nextBeat && remaining > 0f) {
                        val factor = (currentBeat.value - targetBeat) / (nextBeat - targetBeat)
                        val lengthMultiplier = if (i == reorderedSegments.lastIndex) 1.5f else 1f
                        val selectedEnd = lengthMultiplier * length * factor + start
                        val selectedStart = selectedEnd - remaining

                        if (selectedStart <= 0) {
                            DrawPath(flipped, path.copy(), 0f, selectedEnd, color = MaterialTheme.colorScheme.primary)
                            remaining -= abs(selectedEnd)
                        } else {
                            DrawPath(flipped, path.copy(), selectedStart, selectedEnd, color = MaterialTheme.colorScheme.primary)
                            remaining = 0f
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .height(IntrinsicSize.Min)
        ) {
            TempoChanger()
        }
    }
}

@Composable
fun BoxScope.DrawPath(flipped: Boolean, path: Path, start: Float, end: Float, color: Color, style: DrawStyle = Stroke(width = 4.dp.toPx())) {
    Canvas(
        modifier = Modifier.aspectRatio(1f)
            .fillMaxSize()
            .align(Alignment.Center)
            .graphicsLayer(
                scaleX = if (flipped) -1f else 1f,
                scaleY = 1f,
            )
    ) {
        val matrix = Matrix()
        val pathSize = path.getBounds().size
        val sx = size.width / pathSize.width
        val sy = size.height / pathSize.height
        val scale = if (sx < sy) sx else sy
        matrix.setScale(scale, scale)
        path.asAndroidPath().transform(matrix)
        val measurer = PathMeasure()
        measurer.setPath(path, false)
        val newPath = Path()
        measurer.getSegment(start * scale, if(end == 0f) measurer.length else end * scale, newPath, true)

        drawPath(
            path = newPath,
            color = color,
            style = style
        )
    }
}

fun getConductorPath(beats: Int): Pair<Path, FloatArray> {
    fun measure(path: Path): Float {
        val measurer = PathMeasure()
        measurer.setPath(path, false)
        return measurer.length
    }
    when(beats) {
        1 -> {
            var segmentLengths = floatArrayOf()
            val path = Path().apply {
                moveTo(50f, 0f)
                relativeLineTo(0f, 100f)
                segmentLengths += measure(this)
                relativeLineTo(0f, -100f)
                segmentLengths += measure(this)
                close()
            }
            return path to segmentLengths
        }
        2 -> {
            var segmentLengths = floatArrayOf()
            val path = Path().apply {
                moveTo(50f, 0f)
                relativeCubicTo(0f, 0f, -0.04f, 1.56f, 0f, 2.91f)
                relativeCubicTo(0f, 0.03f, 0f, 0.05f, 0f, 0.05f)
                relativeCubicTo(0.11f, 5.17f, 0.02f, 11.5f, 0.06f, 16.93f)
                relativeCubicTo(0.25f, 40.33f, -0.99f, 64.32f, 12.9f, 74.89f)
                relativeCubicTo(2.28f, 1.73f, 6.16f, 4.03f, 12.4f, 4.99f)
                segmentLengths += measure(this)
                relativeCubicTo(2.7f, -0.04f, 7.21f, -0.53f, 11.64f, -3.3f)
                relativeCubicTo(11.24f, -7.01f, 11.07f, -21.79f, 11.05f, -22.79f)
                relativeCubicTo(-0.39f, 0.99f, -6.35f, 15.42f, -18.64f, 16.72f)
                segmentLengths += measure(this)
                relativeCubicTo(-5.34f, 0.56f, -9.5f, -1.6f, -10.52f, -2.16f)
                relativeCubicTo(-7.49f, -4.12f, -11.04f, -13.24f, -14.53f, -39.81f)
                cubicTo(51.06f, 23.28f, 50.11f, 0f, 50f, 0f)
                segmentLengths += measure(this)
                close()
            }
            return path to segmentLengths
        }
        3 -> {
            var segmentLengths = floatArrayOf()
            val path = Path().apply {
                moveTo(50f, 0f)
                relativeLineTo(0f, 100f)
                segmentLengths += measure(this)
                relativeCubicTo(0.14f, -6.18f, 1.27f, -12.84f, 5.55f, -15.03f)
                relativeCubicTo(8.34f, -4.28f, 20.15f, 12.77f, 31.55f, 8.76f)
                segmentLengths += measure(this)
                relativeCubicTo(8.65f, -3.04f, 12.14f, -17.09f, 10.41f, -19.3f)
                relativeCubicTo(-1.03f, -0.72f, -4.4f, 6.63f, -11.01f, 7.66f)
                segmentLengths += measure(this)
                relativeCubicTo(-6.84f, 1.07f, -13.11f, -5.21f, -15.78f, -7.89f)
                cubicTo(53.63f, 58.08f, 50.45f, 7.89f, 50f, 0f)
                segmentLengths += measure(this)
                close()
            }
            return path to segmentLengths
        }
        4 -> {
            var segmentLengths = floatArrayOf()
            val path = Path().apply {
                moveTo(50f, 0f)
                relativeLineTo(0f, 100f)
                segmentLengths += measure(this)
                relativeCubicTo(-0.26f, -0.72f, -4.97f, -13.03f, -17.96f, -15.69f)
                relativeCubicTo(-8.04f, -1.31f, -15.04f, 2.69f, -21.04f, 6.69f)
                relativeCubicTo(-7f, 5f, -11f, 4f, -11f, 0f)
                segmentLengths += measure(this)
                relativeCubicTo(0f, -6f, 7f, -13f, 15.61f, -15.92f)
                relativeCubicTo(18.39f, -7.08f, 58.39f, 13.92f, 67.39f, 13.92f)
                segmentLengths += measure(this)
                relativeCubicTo(8f, 0f, 17f, -5f, 17f, -11f)
                relativeCubicTo(0f, -12f, -10f, -2f, -15f, -2f)
                segmentLengths += measure(this)
                relativeCubicTo(-11f, 0f, -19.43f, -13.74f, -35f, -76f)
                segmentLengths += measure(this)
                close()
            }
            return path to segmentLengths
        }
        5 -> {
            var segmentLengths = floatArrayOf()
            val path = Path().apply {
                moveTo(50f, 0f)
                relativeLineTo(0f, 100f)
                segmentLengths += measure(this)
                relativeCubicTo(-0.7f, -2.11f, -3.49f, -9.72f, -11.36f, -14.06f)
                relativeCubicTo(-8.16f, -4.5f, -18.58f, -3.72f, -26.93f, 1.92f)
                relativeCubicTo(-4.55f, 3.07f, -7.34f, 4.94f, -8.29f, 4.15f)
                segmentLengths += measure(this)
                relativeCubicTo(-1.77f, -1.48f, 2.99f, -13.1f, 9.67f, -17.93f)
                relativeCubicTo(17.79f, -12.87f, 51.91f, 16.92f, 53.91f, 17.92f)
                segmentLengths += measure(this)
                relativeCubicTo(0f, -4f, 2f, -6f, 4f, -6f)
                relativeCubicTo(2f, 0f, 13f, 7f, 17.06f, 5.26f)
                segmentLengths += measure(this)
                relativeCubicTo(1.94f, -1.26f, 6.94f, -4.26f, 6.94f, -9.26f)
                relativeCubicTo(0f, -2f, -1f, -4f, -2f, -5f)
                relativeCubicTo(-4f, -4f, -6f, -5f, -15f, -1f)
                segmentLengths += measure(this)
                relativeCubicTo(-10f, 3f, -27.55f, -63.85f, -28f, -76f)
                segmentLengths += measure(this)
                close()
            }
            return path to segmentLengths
        }
        6 -> {
            var segmentLengths = floatArrayOf()
            val path = Path().apply {
                moveTo(50f, 0f)
                relativeLineTo(0f, 100f)
                segmentLengths += measure(this)
                relativeCubicTo(0.04f, -0.78f, 0f, -8f, -3f, -10f)
                relativeCubicTo(-5f, -3f, -11.7f, -1.21f, -16.75f, 4.5f)
                segmentLengths += measure(this)
                relativeCubicTo(-0.65f, -1.99f, -1.62f, -3.22f, -2.49f, -4.01f)
                relativeCubicTo(-6.76f, -6.49f, -13.76f, -3.49f, -22.78f, 2.93f)
                segmentLengths += measure(this)
                relativeCubicTo(-1.97f, -4.5f, -2.23f, -7.9f, -0.76f, -10.22f)
                relativeCubicTo(7.78f, -12.2f, 26.56f, -3.8f, 63.78f, 6.8f)
                segmentLengths += measure(this)
                relativeCubicTo(0f, -3f, 2f, -5f, 5f, -5f)
                relativeCubicTo(4f, 0f, 10f, 7f, 14f, 7f)
                segmentLengths += measure(this)
                relativeCubicTo(4f, 0f, 8f, -5f, 8f, -9f)
                relativeCubicTo(0f, -4f, -4f, -9f, -8f, -9f)
                relativeCubicTo(-6f,  0f, -8f, 4f, -13f, 4f)
                segmentLengths += measure(this)
                relativeCubicTo(-5f, 0f, -8f, -2f, -9f, -4f)
                relativeCubicTo(-2f, -4f, -12.92f, -30.94f, -15.01f, -73.99f)
                segmentLengths += measure(this)
                close()
            }
            return path to segmentLengths
        }
        else -> {
            return Path() to floatArrayOf()
        }
    }
}