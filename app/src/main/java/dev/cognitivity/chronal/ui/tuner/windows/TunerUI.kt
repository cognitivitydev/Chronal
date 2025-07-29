package dev.cognitivity.chronal.ui.tuner.windows

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.Tuner
import dev.cognitivity.chronal.toSp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
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

@Composable
fun TunerPageMain(expanded: Boolean, padding: PaddingValues) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    if (permissionGranted) {
        var tuner = ChronalApp.getInstance().tuner
        if (tuner == null) {
            tuner = Tuner()
        }

        if(expanded) {
            TunerPageExpanded(tuner)
        } else {
            TunerPageCompact(tuner, padding)
        }
    } else {
        if(expanded) {
            TunerPageExpanded(null)
        } else {
            TunerPageCompact(null, padding)
        }
    }
}

@Composable
fun ShowPermissionWarning(innerPadding: PaddingValues) {
    val context = LocalContext.current
    val permissionGranted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissionGranted.value = granted
        }
    )

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        launcher.launch(Manifest.permission.RECORD_AUDIO)
        AlertDialog(
            onDismissRequest = { },
            confirmButton = @Composable {
                TextButton(onClick = {
                    showDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = ("package:" + context.packageName).toUri()
                    context.startActivity(intent)
                }) {
                    Text(context.getString(R.string.generic_settings))
                }
            },
            dismissButton = @Composable {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text(context.getString(R.string.generic_cancel))
                }
            },
            icon = @Composable {
                Icon(
                    painter = painterResource(R.drawable.outline_warning_24),
                    contentDescription = context.getString(R.string.generic_warning),
                )
            },
            title = @Composable {
                Text(context.getString(R.string.tuner_missing_permission_title))
            },
            text = @Composable {
                Text(context.getString(R.string.tuner_missing_permission_text))
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }

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
                        showDialog = true
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
fun OctaveDialog(vertical: Boolean) {
    Canvas(
        Modifier
    ) {
        val white = listOf("C", "D", "E", "F", "G", "A", "B")
        repeat(white.size) { i ->
            
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
    private val frequency: Double,
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

    private var playJob: Job? = null
    private var stopJob: Job? = null
    private var isStopping = false

    init {
        val silentBuffer = ShortArray(bufferSize * 4)
        audioTrack.write(silentBuffer, 0, silentBuffer.size)
        audioTrack.play()
    }

    fun start() {
        if (playJob != null) {
            stop()
            return
        }
        isStopping = false


//        audioTrack = AudioTrack(
//            AudioManager.STREAM_MUSIC,
//            sampleRate,
//            AudioFormat.CHANNEL_OUT_MONO,
//            AudioFormat.ENCODING_PCM_16BIT,
//            bufferSize,
//            AudioTrack.MODE_STREAM
//        )

        playJob = CoroutineScope(Dispatchers.Default).launch {
            stopJob?.cancelAndJoin()
            stopJob = null

            val fadeSamples = (fadeDurationMs * sampleRate) / 1000
            var amplitude = 0.0
            val amplitudeStep = 1.0 / fadeSamples

            var phase = 0.0
            val phaseIncrement = 2 * PI * frequency / sampleRate

            while (isActive) {
                val buffer = ShortArray(bufferSize * 2)

                for (i in buffer.indices) {
                    val sample = (sin(phase) * Short.MAX_VALUE * amplitude).toInt().toShort()
                    buffer[i] = sample
                    phase += phaseIncrement
                    if (phase > 2 * PI) phase -= 2 * PI

                    // Gradually increase amplitude during fade-in
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

        stopJob = CoroutineScope(Dispatchers.Default).launch {
            playJob?.cancelAndJoin()
            playJob = null

            val fadeSamples = (fadeDurationMs * sampleRate) / 1000
            var amplitude = 1.0
            val amplitudeStep = 1.0 / fadeSamples

            var phase = 0.0
            val phaseIncrement = 2 * PI * frequency / sampleRate

            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            while (amplitude > 0.0) {
                val buffer = ShortArray(bufferSize / 2)

                for (i in buffer.indices) {
                    val sample = (sin(phase) * Short.MAX_VALUE * amplitude).toInt().toShort()
                    buffer[i] = sample
                    phase += phaseIncrement
                    if (phase > 2 * PI) phase -= 2 * PI

                    // Gradually increase amplitude during fade-in
                    amplitude -= amplitudeStep
                    if (amplitude < 0.0) amplitude = 0.0
                }

                audioTrack.write(buffer, 0, buffer.size)
            }
//            audioTrack.stop()
            audioTrack.flush()
        }
    }
}