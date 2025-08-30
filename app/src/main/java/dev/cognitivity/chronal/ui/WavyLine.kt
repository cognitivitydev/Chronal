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

package dev.cognitivity.chronal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WavyHorizontalLine(modifier: Modifier = Modifier) {
    val outline = MaterialTheme.colorScheme.outline

    Canvas(
        modifier = Modifier.defaultMinSize(64.dp, 0.dp)
            .then(modifier)
    ) {
        val strokeWidth = 2.dp.toPx()
        val path = Path().apply {
            moveTo(0f, size.height / 2)
            for (i in 0..size.width.toInt() step 4) {
                lineTo(i.toFloat(), (size.height / 2) + (sin(i / 10f) * 4))
            }
        }
        drawPath(
            path = path,
            color = outline,
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun WavyVerticalLine(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.outline) {
    Canvas(
        modifier = Modifier.defaultMinSize(0.dp, 64.dp)
            .then(modifier)
    ) {
        val strokeWidth = 2.dp.toPx()
        val path = Path().apply {
            moveTo(size.width / 2, 0f)
            for (i in 0..size.height.toInt() step 4) {
                lineTo((size.width / 2) + (sin(i / 10f) * 4), i.toFloat())
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }
}