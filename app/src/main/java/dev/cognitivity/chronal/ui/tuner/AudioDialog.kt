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

package dev.cognitivity.chronal.ui.tuner

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.toSp
import dev.cognitivity.chronal.ui.tuner.windows.A4Midi
import dev.cognitivity.chronal.ui.tuner.windows.getA4
import dev.cognitivity.chronal.ui.tuner.windows.getEnharmonics
import dev.cognitivity.chronal.ui.tuner.windows.getNoteNames
import dev.cognitivity.chronal.ui.tuner.windows.playing
import dev.cognitivity.chronal.ui.tuner.windows.transposeFrequency
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AudioDialog(expanded: Boolean, midi: Int, onChange: (Int) -> Unit, onConfirm: () -> Unit, onStop: () -> Unit, onDismiss: () -> Unit) {
    var frequency by remember { mutableFloatStateOf(transposeFrequency(getA4().toFloat(),
        semitones = if(midi == -1) 0 else (midi - A4Midi))) }

    val phase = remember { Animatable(0f) }
    LaunchedEffect(playing) {
        while(playing) {
            phase.snapTo(0f)
            phase.animateTo(
                targetValue = (2 * PI).toFloat(),
                animationSpec = tween(
                    durationMillis = 250,
                    easing = LinearEasing
                )
            )
        }
        phase.snapTo(0f)
    }


    @Composable
    fun DialogContent() {
        val waveModifier = if(expanded) Modifier.fillMaxWidth(0.2f).fillMaxHeight()
        else Modifier.fillMaxWidth().height(96.dp)
        Box(
            modifier = waveModifier.clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) {

            val noteIndex = if(midi == -1) 9 else midi.mod(12)
            val noteName = getNoteNames()[noteIndex]
            val octave = if(midi == -1) 4 else midi / 12 - 1
            Text("$noteName$octave",
                modifier = Modifier.align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = context.getString(R.string.tuner_hz_decimal, frequency),
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val primary = MaterialTheme.colorScheme.primary

            Canvas(Modifier.fillMaxSize()) {
                val sampleRate = 44100f
                val strokeWidth = 2.dp.toPx()
                val amplitude = center.y / 4
                val widthInPixels = size.width

                val path = Path().apply {
                    moveTo(0f, center.y)
                    for (x in 0 until widthInPixels.toInt()) {
                        val t = x / sampleRate
                        val y = center.y + amplitude * sin(2 * Math.PI * frequency * t + phase.value).toFloat()
                        lineTo(x.toFloat(), y)
                    }
                }

                drawPath(
                    path = path,
                    color = primary,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
        val pianoModifier = if(expanded) Modifier.fillMaxWidth().fillMaxHeight()
        else Modifier.fillMaxHeight(0.4f)
        Box(
            modifier = pianoModifier
        ) {
            PianoDisplay(midi) {
                onChange(it)
                frequency = transposeFrequency(getA4().toFloat(), it - A4Midi)
            }
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(context.getString(R.string.tuner_set_frequency)) },
        text = {
            if(expanded) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DialogContent()
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    DialogContent()
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                enabled = midi != -1 && !playing
            ) {
                Text(context.getString(R.string.generic_start))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onStop()
                    onDismiss()
                },
                enabled = playing
            ) {
                Text(context.getString(R.string.generic_stop))
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    )

}

@Composable
fun BoxScope.PianoDisplay(midi: Int, onChange: (Int) -> Unit) {
    var selected by remember { mutableStateOf(midi != -1) }
    var note by remember { mutableIntStateOf(if(midi == -1) 9 else midi.mod(12)) }
    var octave by remember { mutableIntStateOf(if(midi == -1) 4 else midi / 12 - 1) }

    val whiteKey = MaterialTheme.colorScheme.surfaceVariant
    val onWhiteKey = MaterialTheme.colorScheme.onSurfaceVariant
    val whiteKeySelected = MaterialTheme.colorScheme.primaryContainer
    val onWhiteKeySelected = MaterialTheme.colorScheme.onPrimaryContainer
    val blackKey = MaterialTheme.colorScheme.onSurfaceVariant
    val onBlackKey = MaterialTheme.colorScheme.surfaceVariant
    val blackKeySelected = MaterialTheme.colorScheme.primary
    val onBlackKeySelected = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = Modifier.fillMaxWidth()
            .fillMaxHeight(0.75f)
            .align(Alignment.TopCenter)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.width(32.dp)
                .fillMaxHeight(0.5f)
                .align(Alignment.CenterStart)
                .offset(x = 8.dp)
                .clip(CircleShape)
                .background(if(octave > 3) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer)
                .clickable {
                    if(octave > 3) {
                        octave--
                        if(selected) {
                            onChange(note + (octave + 1) * 12)
                        }
                    }
                },
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_remove_24),
                contentDescription = context.getString(R.string.generic_subtract),
                tint = if(octave > 1) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Column(
            modifier = Modifier.width(32.dp)
                .fillMaxHeight(0.5f)
                .align(Alignment.CenterEnd)
                .offset(x = (-8).dp)
                .clip(CircleShape)
                .background(if(octave < 6) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer)
                .clickable {
                    if(octave < 6) {
                        octave++
                        if(selected) {
                            onChange(note + (octave + 1) * 12)
                        }
                    }
                },
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = context.getString(R.string.generic_add),
                tint = if(octave < 6) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().padding(48.dp, 8.dp)
    ) {
        val keyWidth = this.maxWidth / 7
        val keyHeight = maxHeight
        val keyShape = RoundedCornerShape(10, 10, 50, 50)

        val allKeys = getNoteNames()
        val blackKeys = getEnharmonics()
        val whiteKeys = allKeys.filter { it !in blackKeys.map { enharmonic -> enharmonic.first } }
        whiteKeys.forEachIndexed { index, key ->
            val noteIndex = allKeys.indexOf(key)
            Column(
                modifier = Modifier.size(keyWidth, keyHeight)
                    .padding(3.dp)
                    .offset(
                        x = keyWidth * index,
                        y = 0.dp
                    )
                    .clip(keyShape)
                    .background(if(note == noteIndex && selected) whiteKeySelected else whiteKey)
                    .clickable {
                        selected = true
                        note = noteIndex
                        onChange(noteIndex + (octave + 1) * 12)
                    },
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = key,
                    fontSize = minOf(keyWidth, keyHeight).toSp() * 0.5f,
                    color = if(note == noteIndex && selected) onWhiteKeySelected else onWhiteKey,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(bottom = 4.dp)
                )
                Text(
                    text = "$octave",
                    fontSize = minOf(keyWidth, keyHeight).toSp() * 0.5f,
                    color = if(note == noteIndex && selected) onWhiteKeySelected else onWhiteKey,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(bottom = 4.dp)
                )
            }
        }
        blackKeys.forEachIndexed { index, key ->
            val noteIndex = allKeys.indexOf(key.first)

            val blackKeyWidth = keyWidth * 0.9f
            val blackKeyHeight = keyHeight * 0.67f
            val whiteIndex = if (index < 2) index else index + 1
            val modifier = Modifier.size(blackKeyWidth, blackKeyHeight)
                .padding(3.dp)
                .offset(x = (keyWidth * (whiteIndex + 1) - blackKeyWidth / 2f))
                .clip(keyShape)
                .background(if(note == noteIndex && selected) blackKeySelected else blackKey)
                .clickable {
                    selected = true
                    note = noteIndex
                    onChange(noteIndex + (octave + 1) * 12)
                }

            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = key.first,
                    fontSize = minOf(blackKeyWidth, blackKeyHeight).toSp() * 0.5f,
                    color = if(note == noteIndex && selected) onBlackKeySelected else onBlackKey,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(bottom = 4.dp)
                )
                Text(
                    text = key.second,
                    fontSize = minOf(blackKeyWidth, blackKeyHeight).toSp() * 0.5f,
                    color = if(note == noteIndex && selected) onBlackKeySelected else onBlackKey,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(bottom = 4.dp)
                )
                Text(
                    text = "$octave",
                    fontSize = minOf(blackKeyWidth, blackKeyHeight).toSp() * 0.5f,
                    color = if(note == noteIndex && selected) onBlackKeySelected else onBlackKey,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(bottom = 4.dp)
                )
            }
        }
    }
}
