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

package dev.cognitivity.chronal.ui.metronome

import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.round
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.toDp
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalTextApi::class)
@Composable
fun TempoChanger(
    modifier: Modifier = Modifier,
    bpm: Float,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onClick: () -> Unit
) {

    @Composable
    fun IncrementButton(
        onClick: () -> Unit,
        content: @Composable () -> Unit,
    ) {
        var isPressed by remember { mutableStateOf(false) }

        LaunchedEffect(isPressed) {
            if (isPressed) {
                onClick()
                delay(500)

                var i = 0
                while (isPressed) {
                    onClick()
                    i++
                    delay(max(100L - (i * 2), 30L))
                }
            }
        }
        Box(
            modifier = Modifier.size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }

    Surface(
        modifier = modifier
            .wrapContentWidth()
            .height(120.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 4.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IncrementButton(onDecrement) {
                Icon(
                    painter = painterResource(R.drawable.baseline_remove_24),
                    contentDescription = context.getString(R.string.editor_bpm_decrease)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
                    .widthIn(min = 120.dp)
            ) {

                val roundedFontFamily = FontFamily(
                    Font(R.font.google_sans_flex_variable,
                        variationSettings = FontVariation.Settings(
                            FontVariation.Setting("ROND", 100f),
                        )
                    )
                )

                val bpmText = if (bpm % 1 == 0f) bpm.toInt().toString()
                    else bpm.round(2).toString()
                val decimals = bpmText.substringAfter('.', "").length
                val bpmLength = bpm.toInt().toString().length + decimals * 0.5f

                val width = 100f - ((bpmLength - 2).coerceAtLeast(0f) * 25f)
                val bpmFontFamily = FontFamily(
                    Font(
                        R.font.google_sans_flex_variable,
                        variationSettings = FontVariation.Settings(
                            FontVariation.Setting("ROND", 100f),
                            FontVariation.Setting("wdth", width)
                        )
                    )
                )

                Row {
                    val fontSize = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // variable fonts only supported for O+
                        56.sp
                    } else {
                        56.sp * (width/100f)
                    }
                    Text(
                        text = bpmText.substringBefore('.'),
                        style = TextStyle(
                            fontFamily = bpmFontFamily,
                            fontWeight = FontWeight(700),
                            fontSize = fontSize,
                            lineHeight = 0.sp,
                            fontFeatureSettings = "'tnum'"
                        ),
                        maxLines = 1,
                        modifier = Modifier.height(fontSize.toDp())
                            .alignByBaseline(),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if(bpmText.contains('.')) {
                        Text(
                            text = "." + bpmText.substringAfter('.'),
                            style = TextStyle(
                                fontFamily = bpmFontFamily,
                                fontWeight = FontWeight(700),
                                fontSize = fontSize/2,
                                lineHeight = 0.sp,
                                fontFeatureSettings = "'tnum'"
                            ),
                            maxLines = 1,
                            modifier = Modifier.height((fontSize/2).toDp())
                                .alignByBaseline(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = context.getString(R.string.metronome_bpm),
                    style = TextStyle(
                        fontFamily = roundedFontFamily,
                        fontWeight = FontWeight(600),
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if(true) { // TODO add setting
                    val tempoMarking = getTempoMarking(bpm.toInt())
                    Text(
                        text = tempoMarking ?: context.getString(R.string.metronome_tempo_unknown),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if(tempoMarking != null) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.error,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            IncrementButton(onIncrement) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = context.getString(R.string.editor_bpm_increase)
                )
            }
        }
    }
}

fun getTempoMarking(bpm: Int): String? {
    val markings = Settings.TEMPO_MARKINGS.get().reversed()
    val marking = markings.firstOrNull { it.range.contains(bpm) }
    return marking?.name
}