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

package dev.cognitivity.chronal.ui.metronome.windows

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.MetronomeTrack
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.activity.vibratorManager
import dev.cognitivity.chronal.rhythm.metronome.Beat
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.MetronomeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// TODO remove this

var dropdownExpanded by mutableStateOf(false)
var showTempoTapper by mutableStateOf(false)
var showRhythmPrimary by mutableStateOf(false)
var showRhythmSecondary by mutableStateOf(false)

var vibratePrimary by mutableStateOf(Settings.METRONOME_VIBRATIONS.get())
var vibrateSecondary by mutableStateOf(Settings.METRONOME_VIBRATIONS_SECONDARY.get())

var paused by mutableStateOf(true)

lateinit var activity: MainActivity

val intervals = mutableListOf<Long>()
var lastTapTime: Long? = null

@Composable
fun ClockBeats(beats: List<Beat>, progress: Animatable<Float, AnimationVector1D>, trackSize: Float,
               offColor: Color, primaryColor: Color, surface: Color = MaterialTheme.colorScheme.surface) {
    val showBeats = Settings.SHOW_BEATS.get()
    val showSubdivisions = Settings.SHOW_SUBDIVISIONS.get()
    if (!showBeats && !showSubdivisions) return

    Canvas(Modifier.fillMaxSize()) {

        val borderSize = 8.dp.toPx()
        val radius = (size.minDimension / 2) - trackSize / 2

        val pillWidth = 16.dp.toPx()
        val pillHeight = 8.dp.toPx()


        val totalDuration = beats.sumOf { abs(it.duration) }
        var currentDuration = 0.0
        for(beat in beats) {
            val isMajor = beat.isHigh
            if((isMajor && !showBeats) || (!isMajor && !showSubdivisions) || beat.duration <= 0) {
                currentDuration += abs(beat.duration)
                continue
            }

            val percentage = currentDuration / totalDuration
            val angle = 2 * Math.PI * (percentage - 0.25)
            val x = cos(angle).toFloat()
            val y = sin(angle).toFloat()

            val dotX = (center.x + radius * x)
            val dotY = (center.y + radius * y)

            val reached = progress.value > percentage
            val beatColor = if (isMajor) {
                if (reached) primaryColor else offColor
            } else {
                if (reached) primaryColor else offColor
            }

            drawContext.canvas.save()
            drawContext.canvas.rotate((360f * percentage.toFloat()) - 90f, dotX, dotY)

            val size = if(isMajor) Size(pillWidth + borderSize, pillHeight + borderSize) else Size(pillHeight + borderSize, pillHeight + borderSize)
            drawRoundRect(
                color = surface,
                topLeft = Offset(dotX - size.width / 2, dotY - size.height / 2),
                size = size,
                cornerRadius = CornerRadius(size.minDimension / 2),
            )

            val outlineSize = if(isMajor) Size(pillWidth, pillHeight) else Size(pillHeight, pillHeight)
            drawRoundRect(
                color = beatColor,
                topLeft = Offset(dotX - outlineSize.width / 2, dotY - outlineSize.height / 2),
                size = outlineSize,
                cornerRadius = CornerRadius(outlineSize.minDimension / 2),
            )

            drawContext.canvas.restore()
            currentDuration += abs(beat.duration)
        }
    }
}

var lastVibration = 0L

fun setBPM(bpm: Float) {
    val metronome = ChronalApp.getInstance().metronome
    if(metronome.getTrack(0).bpm == bpm) return

    paused = true
    metronome.stop()
    metronome.getTracks().forEach { it.bpm = bpm }

    val primaryTrack = metronome.getTrack(0)
    val secondaryTrack = metronome.getTrack(1)

    CoroutineScope(Dispatchers.Main).launch {
        Settings.METRONOME_STATE.save(MetronomeState(
            bpm = bpm, beatValuePrimary = primaryTrack.beatValue,
            beatValueSecondary = secondaryTrack.beatValue, secondaryEnabled = secondaryTrack.enabled,
        ))
    }

    if(bpm <= MetronomeTrack.MIN_BPM || bpm >= MetronomeTrack.MAX_BPM) {
        if(System.currentTimeMillis() - lastVibration < 100) return
        lastVibration = System.currentTimeMillis()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && vibratorManager != null)
            vibratorManager!!.vibrate(
            CombinedVibration.createParallel(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            )
        ) else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(10)
        }
    } else {
        val tickPattern = longArrayOf(5)
        val tickAmplitude = intArrayOf((bpm / 2).toInt().coerceIn(1, 255))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && vibratorManager != null) {
            vibratorManager!!.vibrate(
                CombinedVibration.createParallel(
                    VibrationEffect.createWaveform(tickPattern,tickAmplitude, -1)
                )
            )
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(5)
        }
    }
}