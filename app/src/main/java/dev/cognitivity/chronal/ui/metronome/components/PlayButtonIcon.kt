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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.rectangle
import androidx.graphics.shapes.toPath

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayPauseIcon(
    paused: Boolean, modifier: Modifier = Modifier,
    playColor: Color = MaterialTheme.colorScheme.onPrimary, pauseColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val animatedColor by animateColorAsState(
        targetValue = if (paused) pauseColor else playColor,
        animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
        label = "animatedColor"
    )

    val playProgress = remember { Animatable(0f) }

    LaunchedEffect(paused) {
        playProgress.animateTo(
            targetValue = if (paused) 0f else 1f,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
        )
    }

    Box(
        modifier = Modifier.then(modifier)
            .drawWithCache {
                val barWidth = size.width / 4.5f
                val barHeight = size.height / 1.333f

                val square = RoundedPolygon.rectangle(
                    width = barWidth,
                    height = barHeight,
                    centerX = barWidth * 1.5f - 9,
                    centerY = barHeight / 1.5f,
                    rounding = CornerRounding(0f)
                )

                val square2 = RoundedPolygon.rectangle(
                    width = barWidth,
                    height = barHeight,
                    centerX = barWidth * 3.5f - 9,
                    centerY = barHeight / 1.5f,
                    rounding = CornerRounding(0f)
                )

                val triangle = RoundedPolygon(
                    numVertices = 3,
                    radius = size.minDimension / 2f,
                    centerX = size.width / 2f - 10,
                    centerY = size.height / 2f,
                    rounding = CornerRounding(
                        size.minDimension / 10f,
                        smoothing = 0.1f
                    )
                )

                val triangle2 = RoundedPolygon(
                    numVertices = 3,
                    radius = size.minDimension / 3f,
                    centerX = size.width / 2f - 10,
                    centerY = size.height / 2f,
                    rounding = CornerRounding(
                        size.minDimension / 10f,
                        smoothing = 0.1f
                    )
                )

                val morphPath = Morph(start = triangle, end = square)
                    .toPath(progress = playProgress.value)
                    .asComposePath()

                val morphPath2 = Morph(start = triangle2, end = square2)
                    .toPath(progress = playProgress.value)
                    .asComposePath()

                onDrawBehind {
                    drawPath(morphPath, color = animatedColor)
                    drawPath(morphPath2, color = animatedColor)
                }
            }
    )
}