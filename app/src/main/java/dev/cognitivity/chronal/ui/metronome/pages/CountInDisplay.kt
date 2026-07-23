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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.transformed
import dev.cognitivity.chronal.metronome.Metronome
import dev.cognitivity.chronal.settings.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import androidx.compose.ui.graphics.Matrix as ComposeMatrix

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CountInDisplay(metronome: Metronome) {
    val beat by metronome.countInBeats.collectAsState()
    val totalBeats by metronome.countInTotalBeats.collectAsState()
    var displayedBeat by remember { mutableIntStateOf(beat) }

    @Composable
    fun getShape(beat: Int): RoundedPolygon {
        return when(
            val shapeIndex = (totalBeats - beat + 1).coerceAtLeast(1)
        ) {
            1 -> MaterialShapes.Oval
            2 -> MaterialShapes.Pill
            3 -> MaterialShapes.Arrow
            else -> {
                val matrix = android.graphics.Matrix().apply {
                    setRotate(-90f, 0.5f, 0.5f)
                }
                RoundedPolygon(
                    numVertices = shapeIndex,
                    rounding = CornerRounding(radius = 0.2f)
                ).normalized().transformed(matrix)
            }
        }
    }

    val startShape = getShape(displayedBeat - 1)
    val endShape = getShape(displayedBeat)

    val progress = remember { Animatable(0f) }
    val scale = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    val morph = remember(startShape, endShape) {
        Morph(startShape, endShape)
    }

    LaunchedEffect(beat) {
        delay(Settings.VISUAL_LATENCY.get().toLong())
        displayedBeat = beat

        val mainTrack = metronome.tracks[0]
        val mainTimeSignature = mainTrack.getRhythm().measures[0].timeSig
        val beatLength = ((1f / mainTimeSignature.second) * 60000 / metronome.bpm * mainTrack.beatValue).toLong()
        val animationSpec = tween<Float>(
            durationMillis = (beatLength * 0.33f).toInt(),
            easing = EaseOutExpo
        )

        launch {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec)
        }
        launch {
            rotation.snapTo(0f)
            rotation.animateTo(360f, animationSpec)
        }
        launch {
            scale.animateTo(1f - ((beat.toFloat() - 1) / (totalBeats.toFloat() - 1)) * 0.33f, animationSpec)
        }
        if(beat == totalBeats) {
            launch {
                delay((beatLength * 0.67f).toLong())
                scale.animateTo(0f, animationSpec)
            }
            launch {
                delay((beatLength * 0.67f).toLong())
                rotation.snapTo(0f)
                rotation.animateTo(90f, animationSpec)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val fillColor = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.75f)
        val outlineColor = MaterialTheme.colorScheme.primary
        Canvas(
            modifier = Modifier.aspectRatio(1f)
                .fillMaxSize()
        ) {
            val minSize = min(size.width, size.height)

            val matrix = ComposeMatrix().apply {
                translate(size.width / 2f, size.height / 2f)
                rotateZ(rotation.value)
                translate(-size.width / 2f, -size.height / 2f)
                scale(minSize, minSize)
            }

            val path = morph.toPath(progress.value)
            path.transform(matrix)

            scale(scale.value) {
                drawPath(
                    path = path,
                    color = fillColor,
                    style = Fill
                )
                drawPath(
                    path = path,
                    color = outlineColor,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }
        if(displayedBeat != 0) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = displayedBeat.toString(),
                    style = MaterialTheme.typography.displayLargeEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Count-in",
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}