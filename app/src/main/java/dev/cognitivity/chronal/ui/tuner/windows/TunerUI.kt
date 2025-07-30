package dev.cognitivity.chronal.ui.tuner.windows

import android.Manifest
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.Tuner
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.toSp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin

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
        modifier = Modifier
            .fillMaxWidth()
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
        style = MaterialTheme.typography.headlineMedium,
        fullContent = {
            Text(
                text = name,
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.headlineMedium,
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
                style = MaterialTheme.typography.headlineMedium,
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
            minFontSize = 24.dp.toSp(),
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
    val accidentals = ChronalApp.getInstance().settings.accidentals.value

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
        targetValue =
            if(mono) MaterialTheme.colorScheme.outline
            else if(abs(number) >= 40) MaterialTheme.colorScheme.outline
            else if(abs(number) >= 30) MaterialTheme.colorScheme.secondary
            else if(abs(number) >= 20) MaterialTheme.colorScheme.onTertiaryContainer
            else if(abs(number) >= 5)  MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
        animationSpec = MotionScheme.expressive().fastEffectsSpec(),
        label = "textColor"
    )

    val lineColor = animateColorAsState(
        targetValue =
            if(mono) MaterialTheme.colorScheme.outlineVariant
            else if(abs(number) >= 40) MaterialTheme.colorScheme.outlineVariant
            else if(abs(number) >= 30) MaterialTheme.colorScheme.secondaryContainer
            else if(abs(number) >= 20) MaterialTheme.colorScheme.tertiary
            else if(abs(number) >= 5)  MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
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
                .padding(0.dp, 0.dp, 8.dp, 0.dp),
            style = MaterialTheme.typography.titleMedium,
            color = textColor.value,
            overflow = TextOverflow.Visible,
            maxLines = 1
        )
        Box(
            Modifier
                .align(Alignment.CenterVertically)
                .width(lineSize)
                .height(4.dp)
        ) {
            Canvas(
                Modifier.fillMaxSize()
            ) {
                drawRoundRect(
                    color = lineColor.value,
                    size = size,
                    cornerRadius = CornerRadius(size.height)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.DrawHorizontalLine(mono: Boolean, number: Int) {
    val textColor = animateColorAsState(
        targetValue =
            if(mono) MaterialTheme.colorScheme.outline
            else if(abs(number) >= 40) MaterialTheme.colorScheme.outline
            else if(abs(number) >= 30) MaterialTheme.colorScheme.secondary
            else if(abs(number) >= 20) MaterialTheme.colorScheme.onTertiaryContainer
            else if(abs(number) >= 5)  MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
        animationSpec = MotionScheme.expressive().fastEffectsSpec(),
        label = "textColor"
    )

    val lineColor = animateColorAsState(
        targetValue =
            if(mono) MaterialTheme.colorScheme.outlineVariant
            else if(abs(number) >= 40) MaterialTheme.colorScheme.outlineVariant
            else if(abs(number) >= 30) MaterialTheme.colorScheme.secondaryContainer
            else if(abs(number) >= 20) MaterialTheme.colorScheme.tertiary
            else if(abs(number) >= 5)  MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
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
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = (if (number >= 0) "+" else "") + number.toString(),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleMedium,
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
        ) {
            Canvas(
                Modifier.fillMaxSize()
            ) {
                drawRoundRect(
                    color = lineColor.value,
                    size = size,
                    cornerRadius = CornerRadius(size.height)
                )
            }
        }
    }
}


@Composable
fun TuningDialog(expanded: Boolean, midi: Int, onChange: (Int) -> Unit, onConfirm: () -> Unit, onStop: () -> Unit, onDismiss: () -> Unit) {
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

            Canvas(modifier = Modifier.fillMaxSize()) {
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

fun getNoteNames(): List<String> {
    val system = ChronalApp.getInstance().settings.noteNames.value
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
    val system = ChronalApp.getInstance().settings.noteNames.value
    return when(system) {
        0 -> listOf("C♯" to "D♭", "D♯" to "E♭", "F♯" to "G♭", "G♯" to "A♭", "A♯" to "B♭")
        1 -> listOf("Do♯" to "Re♭", "Re♯" to "Mi♭", "Fa♯" to "Sol♭", "Sol♯" to "La♭", "La♯" to "Ti♭")
        2 -> listOf("Di" to "Ra", "Ri" to "Mi", "Fi" to "Sol", "Si" to "La", "Li" to "Ti")
        3 -> listOf("Do♯" to "Re♭", "Re♯" to "Mi♭", "Fa♯" to "Sol♭", "Sol♯" to "La♭", "La♯" to "Si♭")
        4 -> listOf("Cis" to "Des", "Dis" to "Es", "Fis" to "Ges", "Gis" to "As", "Ais" to "Hes")
        5 -> listOf("♯1" to "♭2", "♯2" to "♭3", "♯4" to "♭5", "♯5" to "♭6", "♯6" to "♭7")
        else -> emptyList()
    }
}

fun getA4(): Int {
    return ChronalApp.getInstance().settings.tunerFrequency.value
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
    if(ChronalApp.getInstance().settings.showOctave.value) {
        return note
    }
    return note.replace(Regex("\\d$"), "")
}

class SineWavePlayer(
    private var frequency: Double,
    private val sampleRate: Int = 44100,
    private val fadeDurationMs: Int = 200
) {
    val bufferSize = 8192

    private var audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    private var phase = 0.0
    private var playJob: Job? = null
    private var stopJob: Job? = null
    private var isStopping = false
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        val silentBuffer = ShortArray(bufferSize * 4)
        audioTrack.write(silentBuffer, 0, silentBuffer.size)
        audioTrack.play()
    }

    suspend fun setFrequency(newFrequency: Double) {
        if (newFrequency <= 0) return

        if(playJob == null && !isStopping) {
            frequency = newFrequency
            return
        }
        stop()
        stopJob?.join()
        frequency = newFrequency
        start()
    }

    fun start() {
        if (playJob != null || isStopping) return

        playJob = scope.launch {
            stopJob?.join()
            stopJob = null

            val fadeSamples = (fadeDurationMs * sampleRate) / 1000
            var amplitude = 0.0
            val amplitudeStep = 1.0 / fadeSamples

            val phaseIncrement = 2 * PI * frequency / sampleRate

            while (isActive) {
                val buffer = ShortArray(bufferSize)

                for (i in buffer.indices) {
                    val sample = (sin(phase) * Short.MAX_VALUE * amplitude).toInt().toShort()
                    buffer[i] = sample
                    phase += phaseIncrement
                    if (phase > 2 * PI) phase -= 2 * PI

                    if (amplitude < 1.0) {
                        amplitude += amplitudeStep
                        if (amplitude > 1.0) amplitude = 1.0
                    }
                }

                audioTrack.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stop() {
        if (playJob == null || isStopping) return

        isStopping = true

        stopJob = scope.launch {
            playJob?.cancelAndJoin()
            playJob = null

            val fadeSamples = (fadeDurationMs * sampleRate) / 1000
            var amplitude = 1.0
            val amplitudeStep = 1.0 / fadeSamples

            val phaseIncrement = 2 * PI * frequency / sampleRate

            while (amplitude > 0.0) {
                val buffer = ShortArray(bufferSize)

                for (i in buffer.indices) {
                    val sample = (sin(phase) * Short.MAX_VALUE * amplitude).toInt().toShort()
                    buffer[i] = sample
                    phase += phaseIncrement
                    if (phase > 2 * PI) phase -= 2 * PI

                    amplitude -= amplitudeStep
                    if (amplitude < 0.0) amplitude = 0.0
                }

                audioTrack.write(buffer, 0, buffer.size)
            }

            audioTrack.flush()
            isStopping = false
        }
    }
}