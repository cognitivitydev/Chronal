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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import dev.cognitivity.chronal.metronome.MetronomeTrack
import dev.cognitivity.chronal.settings.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GridDisplayItem(index: Int, track: MetronomeTrack, modifier: Modifier = Modifier) {
    val shape = remember(index) { getShape(index) }
    val morphedShape = Morph(MaterialShapes.Circle, shape)

    val palette = track.color.getPalette()
    val color = palette.colorContainer
    val activeColor = palette.color

    val coroutineScope = rememberCoroutineScope()

    val baseScale = remember { Animatable(0.67f) }
    val baseMorph = remember { Animatable(0f) }
    val baseColor = remember { Animatable(color) }
    val highlightScale = remember { Animatable(1f) }
    val highlightAlpha = remember { Animatable(0f) }

    val animationSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    val colorSpec = MaterialTheme.motionScheme.slowSpatialSpec<Color>()

    LaunchedEffect(track) {
        track.updateEvents.collect { beat ->
            if(beat.duration < 0) return@collect

            coroutineScope.launch {
                delay(Settings.VISUAL_LATENCY.get().toLong())
                track.vibrate(beat)

                baseScale.snapTo(0.9f)
                baseMorph.snapTo(1f)
                baseColor.snapTo(activeColor)
                highlightScale.snapTo(1.25f)
                highlightAlpha.snapTo(0.5f)

                launch { baseScale.animateTo(0.67f, animationSpec) }
                launch { baseMorph.animateTo(0f, animationSpec) }
                launch { baseColor.animateTo(color, colorSpec) }
                launch { highlightScale.animateTo(0.5f, animationSpec) }
                launch { highlightAlpha.animateTo(0f, animationSpec) }
            }
        }
    }

    Canvas(modifier = modifier) {
        drawGridShape(shape.toPath().asComposePath(), activeColor, scale = highlightScale.value, alpha = highlightAlpha.value)
        drawGridShape(morphedShape.toPath(baseMorph.value).asComposePath(), baseColor.value, scale = baseScale.value)
    }
}

private fun DrawScope.drawGridShape(path: Path, color: Color, scale: Float = 0.75f, alpha: Float = 1f) {
    if (alpha <= 0f) return

    val matrix = Matrix()

    matrix.scale(size.minDimension, size.minDimension)
    path.transform(matrix)

    withTransform({
        scale(scaleX = scale, scaleY = scale, pivot = center)
    }) {
        drawPath(path = path, color = color.copy(alpha = alpha))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun getShape(index: Int): RoundedPolygon {
    return when(index % 15) {
        0 -> MaterialShapes.Pill
        1 -> MaterialShapes.Diamond
        2 -> MaterialShapes.Ghostish
        3 -> MaterialShapes.Cookie4Sided
        4 -> MaterialShapes.Clover4Leaf
        5 -> MaterialShapes.Slanted
        6 -> MaterialShapes.Pentagon
        7 -> MaterialShapes.Cookie6Sided
        8 -> MaterialShapes.Gem
        9 -> MaterialShapes.Flower
        10 -> MaterialShapes.SoftBurst
        11 -> MaterialShapes.Sunny
        12 -> MaterialShapes.Cookie7Sided
        13 -> MaterialShapes.Clover8Leaf
        14 -> MaterialShapes.Cookie9Sided
        else -> MaterialShapes.Cookie12Sided
    }
}