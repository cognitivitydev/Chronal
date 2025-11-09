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

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.ui.metronome.windows.setBPM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BoxScope.TempoChanger() {
    val metronome = ChronalApp.getInstance().metronome
    Column(
        modifier = Modifier.align(Alignment.Center)
    ) {
        Row {
            val scope = rememberCoroutineScope()

            Column(
                modifier = Modifier.align(Alignment.CenterVertically)
                    .padding(2.dp, 0.dp, 2.dp, 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = context.getString(R.string.metronome_increase_tempo),
                    modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    var isHeld = true
                                    scope.launch {
                                        setBPM(metronome.getTrack(0).bpm + 1)
                                        delay(500)
                                        while (isHeld) {
                                            setBPM(metronome.getTrack(0).bpm + 1)
                                            delay(50)
                                        }
                                    }
                                    tryAwaitRelease()
                                    isHeld = false
                                }
                            )
                        },
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Decrease tempo",
                    modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    var isHeld = true
                                    scope.launch {
                                        setBPM(metronome.getTrack(0).bpm - 1)

                                        delay(500)
                                        while (isHeld) {
                                            setBPM(metronome.getTrack(0).bpm - 1)
                                            delay(50)
                                        }
                                    }
                                    tryAwaitRelease()
                                    isHeld = false
                                }
                            )
                        },
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = metronome.getTrack(0).bpm.toInt().toString(),
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

        val markings = ChronalApp.getInstance().settings.tempoMarkings.value.reversed()
        val marking = markings.firstOrNull { it.range.contains(metronome.getTrack(0).bpm.toInt()) }
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
}
