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

package dev.cognitivity.chronal.ui.tuner.windows

import android.Manifest
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.Tuner
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.toSp
import dev.cognitivity.chronal.ui.tuner.SineWavePlayer
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round

val flatOffset = (-3).dp
val sharpOffset = (-4).dp

var playing by mutableStateOf(false)
val player = SineWavePlayer(440.0)

@Composable
fun TunerPageMain(expanded: Boolean, padding: PaddingValues, mainActivity: MainActivity) {
    if (mainActivity.microphoneEnabled) {
        var tuner = ChronalApp.getInstance().tuner
        if (tuner == null) {
            tuner = Tuner()
        }

        if(expanded) {
            TunerPageExpanded(tuner, mainActivity)
        } else {
            TunerPageCompact(tuner, padding, mainActivity)
        }
    } else {
        if(expanded) {
            TunerPageExpanded(null, mainActivity)
        } else {
            TunerPageCompact(null, padding, mainActivity)
        }
    }

}

@Composable
fun PermissionWarning(innerPadding: PaddingValues, mainActivity: MainActivity) {
    Box(
        Modifier.fillMaxWidth()
            .fillMaxHeight()
            .padding(top = innerPadding.calculateTopPadding())
    ) {
        Box(
            Modifier.align(Alignment.BottomCenter)
                .padding(8.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(16.dp))
        ) {
            Row(
                Modifier.padding(horizontal = 8.dp).align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_mic_off_24),
                    contentDescription = context.getString(R.string.generic_microphone),
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Text(context.getString(R.string.tuner_microphone_disabled),
                    Modifier.weight(1f)
                        .padding(start = 8.dp),
                )
                TextButton(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = {
                        mainActivity.microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                    }
                ) {
                    Text(context.getString(R.string.generic_fix))
                }
            }
        }
    }
}

@Composable
fun DrawLineOrElse(
    text: String,
    style: TextStyle,
    fullContent: @Composable () -> Unit,
    shortenedContent: @Composable () -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    var availableWidth by remember { mutableIntStateOf(0) }

    val measuredNameWidth = textMeasurer.measure(text, style).size.width

    val shouldShorten = remember(availableWidth, measuredNameWidth) {
        measuredNameWidth > availableWidth - 75
    }

    Box(
        modifier = Modifier.fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                availableWidth = coordinates.size.width
            }
    ) {
        if (shouldShorten) {
            shortenedContent()
        } else {
            fullContent()
        }
    }
}

@Composable
fun ColumnScope.DrawName(name: String, shortened: String) {
    DrawLineOrElse(
        text = name,
        style = MaterialTheme.typography.titleMedium,
        fullContent = {
            Text(
                text = name,
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        shortenedContent = {
            Text(
                text = shortened,
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
fun DrawNoteWithSize(
    text: String,
) {
    val color = if(text == "-") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondary

    BasicText(
        text = text,
        modifier = Modifier.padding(horizontal = 4.dp),
        autoSize = TextAutoSize.StepBased(
            minFontSize = 4.dp.toSp(),
            maxFontSize = 64.dp.toSp(),
            stepSize = 4.dp.toSp()
        ),
        color = { color },
    )
}

@Composable
fun ColumnScope.DrawNote(frequency: Float) {
    val tune = frequencyToNote(frequency)
    val note = if(tune.first == context.getString(R.string.generic_not_applicable)) "-" else tune.first
    val enharmonic = getEnharmonic(note)
    val accidentals = Settings.ACCIDENTALS.get()

    Row(
        Modifier.fillMaxSize().align(Alignment.CenterHorizontally),
    ) {
        val showSharp = accidentals == 0 || accidentals == 2
        val showFlat = accidentals == 1 || accidentals == 2

        val primaryNote = if(enharmonic != note && !showSharp) enharmonic else note
        val secondaryNote = if(enharmonic != note && showFlat && primaryNote != enharmonic) enharmonic else null
        Box(
            modifier = Modifier.weight(1f)
                .align(Alignment.CenterVertically),
            contentAlignment = Alignment.Center
        ) {
            DrawNoteWithSize(toDisplayNote(primaryNote))
        }
        if(secondaryNote != null) {
            Box(
                Modifier.align(Alignment.CenterVertically)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .width(1.dp)
                    .fillMaxHeight(0.8f)
            )
            Box(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                DrawNoteWithSize(toDisplayNote(secondaryNote))
            }
        }
    }
}

@Composable
fun ColumnScope.DrawLines(mono: Boolean) {
    for(i in 0 until 21) {
        val number = -5*i + 50

        Box(
            Modifier.align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .weight(1f)
        ) {
            DrawVerticalLine(mono, number)
        }
    }
}

@Composable
fun RowScope.DrawLines(mono: Boolean) {
    for(i in 0 until 21) {
        val number = -5*(20-i) + 50

        Box(
            Modifier.align(Alignment.CenterVertically)
                .fillMaxHeight()
                .weight(1f)
        ) {
            DrawHorizontalLine(mono, number)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.DrawVerticalLine(mono: Boolean, number: Int) {
    val textColor = animateColorAsState(
        targetValue = getColors(mono, number).first,
        animationSpec = MotionScheme.expressive().fastEffectsSpec(),
        label = "textColor"
    )

    val lineColor = animateColorAsState(
        targetValue = getColors(mono, number).second,
        animationSpec = MotionScheme.expressive().fastEffectsSpec(),
        label = "lineColor"
    )

    val lineSize = if(number == 0) 90.dp
        else if(number.mod(50) == 0) 100.dp
        else if(number.mod(10) == 0) 64.dp
        else 50.dp

    Row(
        Modifier.align(Alignment.CenterEnd)
    ) {
        Text(
            (if(number >= 0) "+" else "") + number.toString(),
            Modifier.align(Alignment.CenterVertically)
                .padding(end = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = textColor.value,
            overflow = TextOverflow.Visible,
            maxLines = 1
        )
        Box(
            Modifier
                .align(Alignment.CenterVertically)
                .width(lineSize)
                .height(4.dp)
                .clip(CircleShape)
                .background(lineColor.value)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.DrawHorizontalLine(mono: Boolean, number: Int) {
    val textColor = animateColorAsState(
        targetValue = getColors(mono, number).first,
        animationSpec = MotionScheme.expressive().fastEffectsSpec(),
        label = "textColor"
    )

    val lineColor = animateColorAsState(
        targetValue = getColors(mono, number).second,
        animationSpec = MotionScheme.expressive().fastEffectsSpec(),
        label = "lineColor"
    )

    val lineSize = if(number == 0) 90.dp
        else if(number.mod(50) == 0) 100.dp
        else if(number.mod(10) == 0) 64.dp
        else 50.dp

    Column(
        Modifier.align(Alignment.BottomCenter)
    ) {
        Box(
            Modifier.align(Alignment.CenterHorizontally)
                .padding(bottom = 4.dp)
        ) {
            Text(
                text = (if (number >= 0) "+" else "") + number.toString(),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.labelMedium,
                color = textColor.value,
                softWrap = false,
                overflow = TextOverflow.Visible,
                maxLines = 1
            )
        }
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .width(4.dp)
                .height(lineSize)
                .clip(CircleShape)
                .background(lineColor.value)
        )
    }
}

@Composable
fun getColors(mono: Boolean, number: Int): Pair<Color, Color> {
    if(mono) return MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.outlineVariant
    when(abs(number)) {
        in 0..4 -> return MaterialTheme.colorScheme.onSurface to MaterialTheme.colorScheme.onSurfaceVariant
        in 5..19 -> return MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
        in 20..29 -> return MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.tertiaryContainer
        in 30..39 -> return MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.secondaryContainer
        else -> return MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.outlineVariant
    }
}

fun getNoteNames(): List<String> {
    val system = Settings.NOTE_NAMES.get()
    return when(system) {
        0 -> listOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")
        1 -> listOf("Do", "Do♯", "Re", "Re♯", "Mi", "Fa", "Fa♯", "Sol", "Sol♯", "La", "La♯", "Ti")
        2 -> listOf("Do", "Di", "Re", "Ri", "Mi", "Fa", "Fi", "Sol", "Si", "La", "Li", "Ti")
        3 -> listOf("Do", "Do♯", "Re", "Re♯", "Mi", "Fa", "Fa♯", "Sol", "Sol♯", "La", "La♯", "Si")
        4 -> listOf("C", "Cis", "D", "Dis", "E", "F", "Fis", "G", "Gis", "A", "Ais", "H")
        5 -> listOf("1", "♯1", "2", "♯2", "3", "4", "♯4", "5", "♯5", "6", "♯6", "7")
        else -> emptyList()
    }
}
fun getEnharmonics(): List<Pair<String, String>> {
    val system = Settings.NOTE_NAMES.get()
    return when(system) {
        0 -> listOf("C♯" to "D♭", "D♯" to "E♭", "F♯" to "G♭", "G♯" to "A♭", "A♯" to "B♭")
        1 -> listOf("Do♯" to "Re♭", "Re♯" to "Mi♭", "Fa♯" to "Sol♭", "Sol♯" to "La♭", "La♯" to "Ti♭")
        2 -> listOf("Di" to "Ra", "Ri" to "Mi", "Fi" to "Sol", "Si" to "La", "Li" to "Ti")
        3 -> listOf("Do♯" to "Re♭", "Re♯" to "Mi♭", "Fa♯" to "Sol♭", "Sol♯" to "La♭", "La♯" to "Si♭")
        4 -> listOf("Cis" to "Des", "Dis" to "Es", "Fis" to "Ges", "Gis" to "As", "Ais" to "B")
        5 -> listOf("♯1" to "♭2", "♯2" to "♭3", "♯4" to "♭5", "♯5" to "♭6", "♯6" to "♭7")
        else -> emptyList()
    }
}

fun getA4(): Int {
    return Settings.TUNER_FREQUENCY.get()
}
const val A4Midi = 69

fun frequencyToNote(frequency: Float): Pair<String, Float> {
    if(frequency <= 0) return context.getString(R.string.generic_not_applicable) to Float.NaN
    val a4 = getA4()
    val nearestMidi = round(69 + 12 * log2(frequency / a4)).toInt()
    val nearestFrequency = a4 * 2.0.pow((nearestMidi - A4Midi) / 12.0)
    val centsOff = 1200 * log2(frequency / nearestFrequency).toFloat()
    val noteName = getNoteNames()[nearestMidi.mod(12)]
    val octave = (nearestMidi / 12) - 1
    val fullNoteName = "$noteName$octave"
    return fullNoteName to centsOff
}

fun transposeFrequency(frequency: Float, semitones: Int): Float {
    return frequency * 2.0.pow(semitones / 12.0).toFloat()
}

fun keyToSemitones(key: String, octave: Int): Int {
    val normalizedKey = getEnharmonics().firstOrNull { it.second == key }?.first ?: key
    val noteIndex = getNoteNames().indexOf(normalizedKey)
    if(noteIndex == -1) return 0
    return noteIndex + (octave * 12)
}

fun getEnharmonic(note: String): String {
    val octave = (note.last().digitToIntOrNull() ?: "").toString()
    val name = note.replace(Regex("\\d$"), "")
    return (getEnharmonics().firstOrNull { it.first == name }?.second ?: name) + octave
}

fun toDisplayNote(note: String): String {
    if(Settings.SHOW_OCTAVE.get()) {
        return note
    }
    return note.replace(Regex("\\d$"), "")
}