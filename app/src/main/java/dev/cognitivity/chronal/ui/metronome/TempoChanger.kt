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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.metronome.windows.setBPM
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun BoxScope.TempoChanger() {
    val metronome = ChronalApp.getInstance().metronome
    val track = metronome.getTrack(0)
    val scope = rememberCoroutineScope()

    var iconsBounds by remember { mutableStateOf<Rect?>(null) }
    var overlayOffset by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = Modifier.align(Alignment.Center)
    ) {
        Row {
            Column(
                modifier = Modifier
                    .onGloballyPositioned { coords ->
                        val pos = coords.localToRoot(Offset.Zero)
                        val size = coords.size
                        iconsBounds = Rect(
                            pos.x,
                            pos.y,
                            pos.x + size.width.toFloat(),
                            pos.y + size.height.toFloat()
                        )
                    }
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = context.getString(R.string.metronome_increase_tempo),
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Decrease tempo",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }

            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = track.bpm.toInt().toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
            Text(
                modifier = Modifier.offset(y = (-4).dp)
                    .align(Alignment.Bottom),
                text = context.getString(R.string.metronome_bpm),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }

        val markings = Settings.TEMPO_MARKINGS.get().reversed()
        val marking = markings.firstOrNull { it.range.contains(track.bpm.toInt()) }
        val string = marking?.name ?: context.getString(R.string.metronome_tempo_unknown)

        Text(
            text = string,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.tertiary,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .offset(y = (-8).dp)
        )
    }

    // expanded bounds for arrows
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords -> overlayOffset = coords.localToRoot(Offset.Zero) }
            .pointerInput(Unit) {
                val paddingX = 24.dp.toPx()
                val paddingY = 12.dp.toPx()

                while (true) {
                    awaitPointerEventScope {
                        val event = awaitPointerEvent()
                        val down = event.changes.firstOrNull { it.changedToDown() }
                        if (down == null) return@awaitPointerEventScope

                        val bounds = iconsBounds
                        if (bounds == null) {
                            down.consume()
                            return@awaitPointerEventScope
                        }

                        val expanded = Rect(
                            bounds.left - paddingX,
                            bounds.top - paddingY,
                            bounds.right + paddingX,
                            bounds.bottom + paddingY
                        )

                        var currentPos = overlayOffset + down.position
                        if(currentPos !in expanded) return@awaitPointerEventScope
                        down.consume()

                        val job = scope.launch {
                            var isUp = currentPos.y < bounds.center.y
                            setBPM(if (isUp) track.bpm + 1 else track.bpm - 1)
                            delay(500)
                            while (isActive) {
                                if (currentPos !in expanded) break

                                isUp = currentPos.y < bounds.center.y
                                setBPM(if (isUp) track.bpm + 1 else track.bpm - 1)
                                delay(50)
                            }
                        }

                        while (true) {
                            val moveEvent = awaitPointerEvent()
                            val change = moveEvent.changes.firstOrNull() ?: continue
                            currentPos = overlayOffset + change.position
                            val up = moveEvent.changes.firstOrNull { it.changedToUp() }
                            if (up != null) {
                                up.consume()
                                break
                            }
                        }
                        job.cancel()
                    }
                }
            }
    )
}