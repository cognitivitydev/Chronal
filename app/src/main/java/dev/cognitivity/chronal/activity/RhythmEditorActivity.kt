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

package dev.cognitivity.chronal.activity

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.Metronome
import dev.cognitivity.chronal.MetronomeState
import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.SimpleRhythm
import dev.cognitivity.chronal.rhythm.metronome.Measure
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmElement
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmNote
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmTuplet
import dev.cognitivity.chronal.ui.WavyVerticalLine
import dev.cognitivity.chronal.ui.metronome.PlayPauseIcon
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

class RhythmEditorActivity : ComponentActivity() {
    private var errors = mutableStateListOf<String>()
    private var shownError by mutableStateOf(false)

    private var noteSelected by mutableIntStateOf(-1)
    private var musicSelected by mutableIntStateOf(-1)

    private var rhythm by mutableStateOf("{4/4}Q;q;q;q;")
    private var parsedRhythm by mutableStateOf(Rhythm.deserialize(rhythm))
    private var backupRhythm by mutableStateOf(parsedRhythm)

    private val metronome = Metronome(parsedRhythm, sendNotifications = false)
    private var appMetronome by mutableStateOf(ChronalApp.getInstance().metronome)
    var isPlaying by mutableStateOf(false)

    private var showTimeSignature by mutableIntStateOf(-1)
    private var showBpm by mutableStateOf(false)

    private var isPrimary by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if(!intent.hasExtra("isPrimary")) {
            finish()
            return
        }
        isPrimary = intent.getBooleanExtra("isPrimary", true)

        this.rhythm = if(isPrimary) ChronalApp.getInstance().settings.metronomeRhythm.value else ChronalApp.getInstance().settings.metronomeRhythmSecondary.value
        parsedRhythm = Rhythm.deserialize(rhythm)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        setContent {
            MetronomeTheme {
                MainContent()
            }
        }

        appMetronome = if(isPrimary) ChronalApp.getInstance().metronome else ChronalApp.getInstance().metronomeSecondary
        metronome.bpm = appMetronome.bpm
        metronome.beatValue = appMetronome.beatValue

        metronome.setRhythm(parsedRhythm)
        metronome.setUpdateListener(2) { beat ->
            val timestamp = metronome.timestamp
            lifecycleScope.launch {
                delay(ChronalApp.getInstance().settings.visualLatency.value.toLong())
                if(metronome.playing && timestamp == metronome.timestamp) {
                    if(beat.measure == 0) {
                        musicSelected = beat.index
                    } else {
                        //calc the index of the note in the measure
                        var index = 0
                        for (i in 0 until beat.measure) {
                            for(element in parsedRhythm.measures[i].elements) {
                                index += when (element) {
                                    is RhythmNote -> 1
                                    is RhythmTuplet -> element.notes.size
                                }
                            }
                        }
                        index += beat.index
                        musicSelected = index
                    }
                }
            }
        }
        metronome.setPauseListener(2) { isPaused ->
            val timestamp = metronome.timestamp
            isPlaying = !isPaused
            if(isPaused) musicSelected = -1
            lifecycleScope.launch {
                delay(ChronalApp.getInstance().settings.visualLatency.value.toLong())
                if(timestamp == metronome.timestamp) musicSelected = if(isPaused) -1 else 0
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        metronome.stop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun MainContent() {
        val ltr = LocalLayoutDirection.current == LayoutDirection.Ltr
        var backDropdown by remember { mutableStateOf(false) }
        var notesEnabled by remember { mutableStateOf(true) }
        var showSimpleWarning by remember { mutableStateOf(false) }

        val animatedRatio by animateFloatAsState(
            targetValue = if (isPlaying) 1.5f else 1f,
            animationSpec = MotionScheme.expressive().fastSpatialSpec(),
            label = "animatedRatio"
        )

        Scaffold(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
        ) { innerPadding ->
            Column {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .weight(4f)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                            end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                            bottom = 0.dp
                        )
                        .padding(32.dp, 0.dp)
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxSize()
                            .padding(1.dp, 0.dp),
                        reverseLayout = !ltr // always display ltr
                    ) {
                        item {
                            DrawRhythm(parsedRhythm, updateBackup = true)
                        }
                        item {
                            var showTimeSignature by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier.fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(modifier = Modifier.align(Alignment.Center)) {
                                    Button(
                                        modifier = Modifier.padding(16.dp, 4.dp)
                                            .align(Alignment.CenterHorizontally),
                                        onClick = {
                                            showTimeSignature = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = getString(R.string.editor_measure_add),
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                        Text(
                                            getString(R.string.editor_measure_add),
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                    FilledTonalButton(
                                        modifier = Modifier.padding(16.dp, 4.dp)
                                            .align(Alignment.CenterHorizontally),
                                        onClick = {
                                            val measures = parsedRhythm.measures.toMutableList()
                                            if (measures.size > 1) {
                                                measures.removeAt(parsedRhythm.measures.lastIndex)
                                            }
                                            parsedRhythm = Rhythm(measures)
                                            metronome.setRhythm(parsedRhythm)
                                        },
                                        enabled = parsedRhythm.measures.size > 1
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_remove_24),
                                            contentDescription = getString(R.string.editor_measure_remove),
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                        Text(
                                            getString(R.string.editor_measure_remove),
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                            }

                            if (showTimeSignature) {
                                var timeSignature by remember {
                                    mutableStateOf(
                                        parsedRhythm.measures.lastOrNull()?.timeSig ?: (4 to 4)
                                    )
                                }
                                AlertDialog(
                                    onDismissRequest = { showTimeSignature = false },
                                    title = { Text(getString(R.string.editor_set_time_signature)) },
                                    text = {
                                        Column(
                                            modifier = Modifier.aspectRatio(1f)
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceContainer,
                                                    MaterialShapes.Bun.toShape(0)
                                                )
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f)
                                                    .padding(horizontal = 32.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        timeSignature = Pair(
                                                            (timeSignature.first - 1).coerceIn(1..32),
                                                            timeSignature.second
                                                        )
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                                                        contentDescription = getString(R.string.generic_subtract),
                                                        tint = MaterialTheme.colorScheme.tertiary
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier.weight(1f)
                                                        .fillMaxHeight(0.8f)
                                                        .align(Alignment.CenterVertically),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    MusicFont.Number.TimeSignatureLine(
                                                        timeSignature.first,
                                                        MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        timeSignature = Pair(
                                                            (timeSignature.first + 1).coerceIn(1..32),
                                                            timeSignature.second
                                                        )
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                                                        contentDescription = getString(R.string.generic_add),
                                                        tint = MaterialTheme.colorScheme.tertiary
                                                    )
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.weight(1f)
                                                    .padding(horizontal = 32.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        timeSignature = Pair(
                                                            timeSignature.first,
                                                            (timeSignature.second / 2).coerceIn(1..32)
                                                        )
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                                                        contentDescription = getString(R.string.generic_subtract),
                                                        tint = MaterialTheme.colorScheme.tertiary
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier.weight(1f)
                                                        .fillMaxHeight(0.8f)
                                                        .align(Alignment.CenterVertically),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    MusicFont.Number.TimeSignatureLine(
                                                        timeSignature.second,
                                                        MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        timeSignature = Pair(
                                                            timeSignature.first,
                                                            (timeSignature.second * 2).coerceIn(1..32)
                                                        )
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                                                        contentDescription = getString(R.string.generic_add),
                                                        tint = MaterialTheme.colorScheme.tertiary
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            val measures = parsedRhythm.measures.toMutableList()
                                            val elements = mutableListOf<RhythmElement>().apply {
                                                repeat(timeSignature.first) {
                                                    add(RhythmNote(
                                                        display = MusicFont.Notation.convert(timeSignature.second, false).toString(),
                                                        isRest = true,
                                                        isInverted = false,
                                                        duration = 1.0 / timeSignature.second,
                                                        dots = 0
                                                    ))
                                                }
                                            }
                                            measures.add(Measure(
                                                timeSig = timeSignature,
                                                elements = elements
                                            ))
                                            parsedRhythm = Rhythm(measures)
                                            metronome.setRhythm(parsedRhythm)
                                            showTimeSignature = false
                                        }) {
                                            Text(getString(R.string.editor_measure_add))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showTimeSignature = false }) {
                                            Text(getString(R.string.generic_cancel))
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .fillMaxHeight(0.8f)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Box(
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .weight(1f)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Box(
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .fillMaxHeight(0.8f)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                var selectedNote: RhythmNote? = null
                var isTuplet = false
                var selectedDuration = 0.0
                var dots = -1
                var nextDots = 0
                var largestNote = 0

                var globalIndex = 0
                for (measure in parsedRhythm.measures) {
                    val measureDuration = measure.timeSig.first / measure.timeSig.second.toDouble()
                    var remainingDuration = measureDuration

                    for (element in measure.elements) {
                        when (element) {
                            is RhythmNote -> {
                                if (globalIndex == noteSelected) {
                                    selectedNote = element
                                    isTuplet = false
                                    selectedDuration = abs(element.duration)
                                    dots = element.dots
                                    largestNote = ceil(1.0 / remainingDuration).toInt()
                                    globalIndex++
                                    break
                                }
                                globalIndex++
                                remainingDuration -= abs(element.duration)
                            }

                            is RhythmTuplet -> {
                                val tupletDuration = element.notes.sumOf { abs(it.duration) }
                                var remainingTupletDuration = tupletDuration
                                for (i in 0 until element.notes.size) {
                                    if (globalIndex == noteSelected) {
                                        selectedNote = element.notes[i]
                                        isTuplet = true
                                        selectedDuration = abs(element.notes[i].duration)
                                        dots = element.notes[i].dots
                                        globalIndex++
                                        largestNote =
                                            ceil((1.0 / remainingTupletDuration) * element.ratio.second / element.ratio.first).toInt()
                                        break
                                    }
                                    globalIndex++
                                    remainingDuration -= abs(element.notes[i].duration)
                                    remainingTupletDuration -= abs(element.notes[i].duration)
                                }
                                if (isTuplet) break
                            }
                        }
                    }
                    if (dots != -1) {
                        val oldDotModifier = 1 + (1..dots).sumOf { 1.0 / (2.0.pow(it)) }
                        val oldDuration = abs(selectedDuration) / oldDotModifier
                        if (dots == 2) {
                            nextDots = 0
                        } else {
                            for (i in dots + 1..2) {
                                val newDotModifier = 1 + (1..i).sumOf { 1.0 / (2.0.pow(it)) }
                                val newDuration = oldDuration * newDotModifier

                                if (remainingDuration >= newDuration) {
                                    nextDots = i
                                    break
                                }
                            }
                        }
                    }
                }
                var emphasis by remember { mutableStateOf(true) }

                Box(
                    modifier = Modifier.fillMaxWidth()
                        .weight(4f)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 4.dp)
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxHeight()
                            .align(Alignment.Center),
                        contentPadding = PaddingValues(
                            start = maxOf(8.dp, innerPadding.calculateStartPadding(LocalLayoutDirection.current)),
                            end = maxOf(8.dp, innerPadding.calculateEndPadding(LocalLayoutDirection.current)),
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        item {
                            Column(
                                modifier = Modifier.width(IntrinsicSize.Max)
                                    .fillMaxHeight()
                                    .padding(8.dp, 0.dp)
                            ) {
                                TimeSignature(noteSelected != -1)
                                ChangeEmphasis(noteSelected != -1)
                            }
                        }
                        item {
                            Column(
                                modifier = Modifier.width(IntrinsicSize.Max)
                                    .fillMaxHeight()
                                    .padding(8.dp, 0.dp)
                            ) {
                                AddTuplet(noteSelected != -1 && (dots == 0 || isTuplet), isTuplet)
                                ToggleDot(noteSelected != -1 && dots != nextDots, selectedNote, nextDots)
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier.width(IntrinsicSize.Max)
                                    .padding(24.dp, 0.dp)
                                    .fillMaxHeight()
                            ) {
                                WavyVerticalLine(
                                    modifier = Modifier.fillMaxHeight(0.75f)
                                        .align(Alignment.Center),
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                        for (i in 0..4) {
                            item {
                                Column(
                                    modifier = Modifier.width(IntrinsicSize.Max)
                                        .fillMaxHeight()
                                        .padding(8.dp, 0.dp)
                                ) {
                                    val topValue = (2.0).pow(i * 2).toInt()
                                    NoteButton(
                                        topValue,
                                        !notesEnabled,
                                        emphasis,
                                        largestNote <= topValue && noteSelected != -1
                                    )

                                    val bottomValue = (2.0).pow(i * 2 + 1).toInt()
                                    NoteButton(
                                        bottomValue,
                                        !notesEnabled,
                                        emphasis,
                                        largestNote <= bottomValue && noteSelected != -1
                                    )
                                }
                            }
                        }
                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(8.dp))
                                    .padding(8.dp, 16.dp, 16.dp, 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val interactionSource = remember { MutableInteractionSource() }
                                    RadioButton(
                                        selected = emphasis,
                                        onClick = { emphasis = true },
                                        enabled = notesEnabled,
                                        interactionSource = interactionSource
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = getString(R.string.editor_emphasis_high),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (emphasis && notesEnabled) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.clickable(
                                            enabled = notesEnabled,
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) { emphasis = true }
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val interactionSource = remember { MutableInteractionSource() }
                                    RadioButton(
                                        selected = !emphasis,
                                        onClick = { emphasis = false },
                                        enabled = notesEnabled,
                                        interactionSource = interactionSource
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = getString(R.string.editor_emphasis_low),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (!emphasis && notesEnabled) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.clickable(
                                            enabled = notesEnabled,
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) { emphasis = false }
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .weight(2f)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(
                            top = 0.dp,
                            start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                            end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                            bottom = innerPadding.calculateBottomPadding()
                        )
                        .padding(16.dp, 0.dp)
                ) {
                    val scope = rememberCoroutineScope()
                    MaterialTheme(
                        colorScheme = MaterialTheme.colorScheme.copy(surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shapes = MaterialTheme.shapes.copy(
                            extraSmall = RoundedCornerShape(
                                16.dp
                            )
                        )
                    ) {
                        DropdownMenu(
                            expanded = backDropdown,
                            onDismissRequest = { backDropdown = false },
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_save_24),
                                        contentDescription = getString(R.string.generic_save_exit)
                                    )
                                },
                                text = { Text(getString(R.string.generic_save_exit)) },
                                onClick = {
                                    backDropdown = false
                                    appMetronome.setRhythm(parsedRhythm)
                                    appMetronome.bpm = metronome.bpm
                                    appMetronome.beatValue = metronome.beatValue

                                    val primaryMetronome = ChronalApp.getInstance().metronome
                                    val secondaryMetronome = ChronalApp.getInstance().metronomeSecondary
                                    ChronalApp.getInstance().settings.metronomeState.value = MetronomeState(
                                        bpm = metronome.bpm,
                                        beatValuePrimary = primaryMetronome.beatValue,
                                        beatValueSecondary = secondaryMetronome.beatValue,
                                        secondaryEnabled = secondaryMetronome.active
                                    )

                                    if (isPrimary) {
                                        ChronalApp.getInstance().settings.metronomeRhythm.value =
                                            parsedRhythm.serialize()
                                        ChronalApp.getInstance().settings.metronomeSimpleRhythm.value =
                                            SimpleRhythm(0 to 0, 0, 0)
                                    } else {
                                        ChronalApp.getInstance().settings.metronomeRhythmSecondary.value =
                                            parsedRhythm.serialize()
                                        ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary.value =
                                            SimpleRhythm(0 to 0, 0, 0)
                                    }
                                    scope.launch {
                                        ChronalApp.getInstance().settings.save()
                                        finish()
                                    }
                                },
                                enabled = errors.isEmpty()
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = getString(R.string.generic_exit_discard)
                                    )
                                },
                                text = { Text(getString(R.string.generic_exit_discard)) },
                                onClick = {
                                    backDropdown = false
                                    finish()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = getString(R.string.generic_cancel)
                                    )
                                },
                                text = { Text(getString(R.string.generic_cancel)) },
                                onClick = {
                                    backDropdown = false
                                }
                            )
                        }
                    }
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .padding(8.dp, 0.dp),
                        onClick = {
                            backDropdown = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = getString(R.string.generic_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = {
                            showSimpleWarning = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPrimary) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Text(getString(R.string.simple_editor_switch_simple))
                    }
                    Box(
                        modifier = Modifier.weight(1f)
                            .align(Alignment.CenterVertically)
                    ) {

                        Row(
                            Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                        ) {
                            ToggleButton(
                                checked = notesEnabled,
                                onCheckedChange = { notesEnabled = true },
                                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                                contentPadding = ButtonDefaults.ContentPadding
                            ) {
                                Text(getString(R.string.editor_input_notes))
                            }

                            ToggleButton(
                                checked = !notesEnabled,
                                onCheckedChange = { notesEnabled = false },
                                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                contentPadding = ButtonDefaults.ContentPadding
                            ) {
                                Text(getString(R.string.editor_input_rests))
                            }
                        }

                    }
                    Row(
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable {
                                showBpm = true
                            }
                            .padding(24.dp, 8.dp)
                    ) {
                        MusicFont.Notation.NoteCentered(
                            note = getBeatValue(metronome.beatValue).first,
                            dots = if(getBeatValue(metronome.beatValue).second) 1 else 0,
                            modifier = Modifier.align(Alignment.CenterVertically),
                            color = MaterialTheme.colorScheme.onSurface,
                            size = 32.dp,
                        )
                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )
                        Text(
                            "= ${metronome.bpm}",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Box(
                        modifier = Modifier.padding(start = 8.dp)
                            .align(Alignment.CenterVertically)
                            .fillMaxHeight(0.8f)
                            .aspectRatio(1.5f)
                    ) {
                        val animatedColor by animateColorAsState(
                            targetValue = if (isPlaying) MaterialTheme.colorScheme.tertiaryContainer
                            else if (errors.isNotEmpty()) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer,
                            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
                            label = "animatedColor"
                        )

                        Box(
                            modifier = Modifier.aspectRatio(animatedRatio, true)
                                .clip(CircleShape)
                                .background(animatedColor)
                                .clickable {
                                    if (errors.isNotEmpty()) {
                                        shownError = false
                                        return@clickable
                                    }
                                    isPlaying = !isPlaying
                                    metronome.setRhythm(parsedRhythm)
                                    if (isPlaying) {
                                        metronome.start()
                                    } else {
                                        metronome.stop()
                                    }
                                }
                                .align(Alignment.Center),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxHeight(0.5f)
                                    .aspectRatio(1f, true)
                                    .align(Alignment.Center)
                            ) {
                                if (errors.isEmpty()) {
                                    PlayPauseIcon(
                                        !isPlaying,
                                        modifier = Modifier.fillMaxSize(),
                                        pauseColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        playColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_warning_24),
                                        contentDescription = getString(R.string.generic_error),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (errors.isNotEmpty() && !shownError) {
                AlertDialog(
                    onDismissRequest = { shownError = false },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.outline_warning_24),
                            contentDescription = getString(R.string.generic_error)
                        )
                    },
                    title = { Text(getString(R.string.editor_invalid_rhythm_name)) },
                    text = {
                        Column {
                            Text(getString(R.string.editor_invalid_rhythm_text))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text(
                                getString(R.string.editor_invalid_rhythm_amount, errors.size),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceContainer,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                items(errors) { error ->
                                    Text(error, modifier = Modifier.padding(8.dp, 4.dp))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            errors.clear()
                            shownError = false
                            parsedRhythm = backupRhythm
                            metronome.setRhythm(parsedRhythm)
                        }) {
                            Text(getString(R.string.generic_revert))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            shownError = true
                        }) {
                            Text(getString(R.string.generic_ignore))
                        }
                    }
                )
            }
        }
        if(showSimpleWarning) {
            val scope = rememberCoroutineScope()
            AlertDialog(
                onDismissRequest = { showSimpleWarning = false },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_warning_24),
                        contentDescription = getString(R.string.generic_warning)
                    )
                },
                title = { Text(getString(R.string.simple_editor_simple_warning_title)) },
                text = {
                    Text(getString(R.string.simple_editor_simple_warning_text))
                },
                confirmButton = {
                    TextButton(onClick = {
                        showSimpleWarning = false
                        val elements = arrayListOf<RhythmElement>().apply {
                            repeat(parsedRhythm.measures[0].timeSig.first) {
                                add(
                                    RhythmNote(
                                        display = MusicFont.Notation.convert(parsedRhythm.measures[0].timeSig.second, false).toString(),
                                        isRest = false,
                                        isInverted = false,
                                        duration = 1.0 / parsedRhythm.measures[0].timeSig.second,
                                        dots = 0
                                    )
                                )
                            }
                        }
                        appMetronome.setRhythm(
                            Rhythm(listOf(Measure(parsedRhythm.measures[0].timeSig, elements)))
                        )

                        appMetronome.beatValue = 4f
                        val primaryMetronome = ChronalApp.getInstance().metronome
                        val secondaryMetronome = ChronalApp.getInstance().metronomeSecondary
                        ChronalApp.getInstance().settings.metronomeState.value = MetronomeState(
                            bpm = metronome.bpm,
                            beatValuePrimary = primaryMetronome.beatValue,
                            beatValueSecondary = secondaryMetronome.beatValue,
                            secondaryEnabled = secondaryMetronome.active
                        )

                        if (isPrimary) {
                            ChronalApp.getInstance().settings.metronomeRhythm.value =
                                metronome.getRhythm().serialize()
                            ChronalApp.getInstance().settings.metronomeSimpleRhythm.value = SimpleRhythm(
                                parsedRhythm.measures[0].timeSig,
                                parsedRhythm.measures[0].timeSig.second, 0
                            )
                        } else {
                            ChronalApp.getInstance().settings.metronomeRhythmSecondary.value =
                                metronome.getRhythm().serialize()
                            ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary.value =
                                SimpleRhythm(
                                    parsedRhythm.measures[0].timeSig,
                                    parsedRhythm.measures[0].timeSig.second, 0
                                )
                        }
                        scope.launch {
                            ChronalApp.getInstance().settings.save()
                        }
                        finish()
                    }) {
                        Text(getString(R.string.generic_switch))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSimpleWarning = false }) {
                        Text(getString(R.string.generic_cancel))
                    }
                }
            )
        }
        if(showBpm) {
            EditBpmDialog()
        }
        if(showTimeSignature != -1) {
            TimeSignatureDialog(showTimeSignature)
        }
    }

    fun getBeatValue(value: Float): Pair<MusicFont.Notation, Boolean> {
        return when (value) {
            16f -> MusicFont.Notation.N_16TH to false
            8f ->  MusicFont.Notation.N_EIGHTH to false
            4f ->  MusicFont.Notation.N_QUARTER to false
            2f ->  MusicFont.Notation.N_HALF to false
            1f ->  MusicFont.Notation.N_WHOLE to false
            16f / 1.5f -> MusicFont.Notation.N_16TH to true
            8f / 1.5f ->  MusicFont.Notation.N_EIGHTH to true
            4f / 1.5f ->  MusicFont.Notation.N_QUARTER to true
            2f / 1.5f ->  MusicFont.Notation.N_HALF to true
            1f / 1.5f ->  MusicFont.Notation.N_WHOLE to true
            else -> MusicFont.Notation.N_QUARTER to false
        }
    }
    fun getBeatValueString(value: Float): String {
        return when (value) {
            16f -> MusicFont.Notation.N_16TH.char.toString()
            8f ->  MusicFont.Notation.N_EIGHTH.char.toString()
            4f ->  MusicFont.Notation.N_QUARTER.char.toString()
            2f ->  MusicFont.Notation.N_HALF.char.toString()
            1f ->  MusicFont.Notation.N_WHOLE.char.toString()
            16f / 1.5f -> MusicFont.Notation.N_16TH.char + " " + MusicFont.Notation.DOT.char
            8f / 1.5f ->  MusicFont.Notation.N_EIGHTH.char + " " + MusicFont.Notation.DOT.char
            4f / 1.5f ->  MusicFont.Notation.N_QUARTER.char + " " + MusicFont.Notation.DOT.char
            2f / 1.5f ->  MusicFont.Notation.N_HALF.char + " " + MusicFont.Notation.DOT.char
            1f / 1.5f ->  MusicFont.Notation.N_WHOLE.char + " " + MusicFont.Notation.DOT.char
            else -> ""
        }
    }

    @Composable
    fun EditBpmDialog() {
        var beatValue by remember { mutableFloatStateOf(metronome.beatValue) }
        var bpm by remember { mutableIntStateOf(metronome.bpm) }
        val scope = rememberCoroutineScope()
        var change = 0

        Dialog(
            onDismissRequest = { showBpm = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.padding(16.dp)
                    .fillMaxWidth(0.5f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        getString(R.string.editor_bpm_set),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(16.dp))

                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        modifier = Modifier.padding(bottom = 16.dp)
                            .fillMaxWidth()
                            .height(144.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center
                    ) {
                        repeat(10) { index ->
                            item {
                                val noteOption = floor(index / 2f).toInt()
                                val baseValue = (2.0).pow(4 - noteOption).toFloat()
                                val dotted = index % 2 == 1
                                val value = if (dotted) baseValue / 1.5f else baseValue
                                val string = getBeatValueString(value)
                                val char = MusicFont.Notation.entries.find { it.char == string[0] }
                                    ?: MusicFont.Notation.N_QUARTER
                                val isSelected = beatValue == value

                                Box(
                                    modifier = Modifier.padding(4.dp)
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainer
                                        )
                                        .clickable {
                                            beatValue = value
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    MusicFont.Notation.NoteCentered(
                                        note = char,
                                        dots = if(dotted) 1 else 0,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurface,
                                        size = 48.dp,
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { _, dragAmount ->
                                    change += dragAmount.toInt()
                                    if (abs(change) >= 8) {
                                        val adjustment = (change / 8)
                                        bpm = (bpm - adjustment).coerceIn(1, 500)
                                        change %= 8
                                    }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        MusicFont.Notation.NoteCentered(
                            note = getBeatValue(beatValue).first,
                            dots = if(getBeatValue(beatValue).second) 1 else 0,
                            modifier = Modifier.align(Alignment.CenterVertically),
                            color = MaterialTheme.colorScheme.onSurface,
                            size = 64.dp,
                        )
                        Text(
                            "=",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.displayMedium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                "$bpm",
                                modifier = Modifier.align(Alignment.CenterVertically),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.displayLarge
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = getString(R.string.editor_bpm_increase),
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                var isHeld = true
                                                scope.launch {
                                                    bpm = (bpm + 1).coerceIn(1, 500)
                                                    delay(500)
                                                    while (isHeld) {
                                                        bpm = (bpm + 1).coerceIn(1, 500)
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
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = getString(R.string.editor_bpm_decrease),
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                var isHeld = true
                                                scope.launch {
                                                    bpm = (bpm - 1).coerceIn(1, 500)
                                                    delay(500)
                                                    while (isHeld) {
                                                        bpm = (bpm - 1).coerceIn(1, 500)
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
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showBpm = false }) {
                            Text(getString(R.string.generic_cancel))
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            metronome.stop()
                            metronome.beatValue = beatValue
                            metronome.bpm = bpm
                            showBpm = false
                        }) {
                            Text(getString(R.string.generic_confirm))
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun ColumnScope.TimeSignature(enabled: Boolean) {
        val backgroundColor by animateColorAsState(
            targetValue = if (!enabled) MaterialTheme.colorScheme.surfaceContainer
            else MaterialTheme.colorScheme.primaryContainer,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "backgroundColor"
        )
        val textColor by animateColorAsState(
            targetValue = if (!enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onPrimaryContainer,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "textColor"
        )

        var globalIndex = 0
        var measureIndex = -1
        for ((index, measure) in parsedRhythm.measures.withIndex()) {
            for (element in measure.elements) {
                globalIndex += when (element) {
                    is RhythmNote -> 1
                    is RhythmTuplet -> element.notes.size
                }
            }
            if (globalIndex > noteSelected) {
                measureIndex = index
                break
            }
        }

        Row(
            modifier = Modifier.weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .clickable(enabled) {
                    showTimeSignature = measureIndex
                }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxHeight(0.5f)
                    .align(Alignment.CenterVertically)
            ) {
                val timeSig = parsedRhythm.measures.getOrNull(measureIndex)?.timeSig ?: (4 to 4)
                MusicFont.Number.TimeSignature(timeSig.first, timeSig.second, textColor)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = getString(R.string.editor_set_time_signature),
                modifier = Modifier.align(Alignment.CenterVertically),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun ColumnScope.ChangeEmphasis(enabled: Boolean) {
        var showDialog by remember { mutableStateOf(false) }

        val backgroundColor by animateColorAsState(
            targetValue = if (!enabled) MaterialTheme.colorScheme.surfaceContainer
            else MaterialTheme.colorScheme.primaryContainer,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "backgroundColor"
        )
        val textColor by animateColorAsState(
            targetValue = if (!enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onPrimaryContainer,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "textColor"
        )

        Row(
            modifier = Modifier.weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .clickable(enabled) {
                    showDialog = true
                }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_volume_up_24),
                contentDescription = getString(R.string.editor_change_emphasis),
                modifier = Modifier.align(Alignment.CenterVertically),
                tint = textColor
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = getString(R.string.editor_change_emphasis),
                modifier = Modifier.align(Alignment.CenterVertically),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        if(showDialog) {
            var emphasis by remember { mutableStateOf(true) }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(getString(R.string.editor_change_emphasis)) },
                text = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val interactionSource = remember { MutableInteractionSource() }
                            RadioButton(
                                selected = emphasis,
                                onClick = { emphasis = true },
                                enabled = enabled,
                                interactionSource = interactionSource
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = getString(R.string.editor_emphasis_high),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (emphasis) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { emphasis = true }
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val interactionSource = remember { MutableInteractionSource() }
                            RadioButton(
                                selected = !emphasis,
                                onClick = { emphasis = false },
                                enabled = enabled,
                                interactionSource = interactionSource
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = getString(R.string.editor_emphasis_low),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (!emphasis) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { emphasis = false }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val selectedNote = getNote(noteSelected) ?: return@TextButton
                        val newNote = selectedNote.copy(
                            display = MusicFont.Notation.setEmphasis(selectedNote.display, emphasis),
                            isInverted = !emphasis
                        )
                        val newRhythm = setNote(noteSelected, newNote, isScaled = true)
                        parsedRhythm = newRhythm
                        rhythm = newRhythm.serialize()
                        metronome.setRhythm(parsedRhythm)

                        showDialog = false
                    }) {
                        Text(getString(R.string.generic_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(getString(R.string.generic_cancel))
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun ColumnScope.AddTuplet(enabled: Boolean, tupletSelected: Boolean) {
        var ratio: Pair<Int, Int>? by remember { mutableStateOf(null) }
        var showDialog by remember { mutableStateOf(false) }

        val backgroundColor by animateColorAsState(
            targetValue = if (!enabled) MaterialTheme.colorScheme.surfaceContainer
                else if (tupletSelected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.primaryContainer,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "backgroundColor"
        )
        val textColor by animateColorAsState(
            targetValue = if (!enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else if (tupletSelected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onPrimaryContainer,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "textColor"
        )

        Row(
            modifier = Modifier.weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .clickable(enabled) {
                    if (!tupletSelected) {
                        showDialog = true
                    } else {
                        // remove tuplet
                        var globalIndex = 0
                        var measureIndex = 0
                        var measureElement = 0
                        var selectedTuplet: RhythmTuplet? = null

                        for (measure in parsedRhythm.measures) {
                            for (element in measure.elements) {
                                when (element) {
                                    is RhythmNote -> {
                                        globalIndex++
                                    }

                                    is RhythmTuplet -> {
                                        for (i in element.notes.indices) {
                                            if (globalIndex == noteSelected) {
                                                selectedTuplet = element
                                                noteSelected -= i // move selection to first note in tuplet
                                                break
                                            }
                                            globalIndex++
                                        }
                                        if (selectedTuplet != null) break
                                    }
                                }
                                measureElement++
                            }
                            if (selectedTuplet != null) break
                            measureIndex++
                            measureElement = 0
                        }
                        if (selectedTuplet == null) return@clickable

                        val duration = selectedTuplet.notes.sumOf { abs(it.duration) }
                        // get dots
                        for (i in 0..2) {
                            val dotModifier = 1 + (1..i).sumOf { 1.0 / (2.0.pow(it)) }
                            val dottedDuration = duration * dotModifier

                            val intValue = (1.0 / dottedDuration).toInt()
                            if (intValue.toDouble() == 1.0 / dottedDuration) {
                                // found a valid dot
                                val newNote = RhythmNote(
                                    display = MusicFont.Notation.convert(intValue, selectedTuplet.notes.first().isRest)
                                        .toString(),
                                    isRest = selectedTuplet.notes.first().isRest,
                                    isInverted = selectedTuplet.notes.first().isInverted,
                                    rawDuration = 1.0 / intValue,
                                    duration = dottedDuration,
                                    dots = i
                                )
                                val measure = parsedRhythm.measures[measureIndex]
                                val newElements = measure.elements.toMutableList()
                                newElements[measureElement] = newNote
                                val newMeasure = Measure(
                                    timeSig = measure.timeSig,
                                    elements = newElements
                                )
                                val newRhythm = Rhythm(parsedRhythm.measures.toMutableList().apply {
                                    this[measureIndex] = newMeasure
                                })
                                parsedRhythm = newRhythm
                                rhythm = newRhythm.serialize()
                                metronome.setRhythm(parsedRhythm)
                                return@clickable
                            }
                        }
                    }
                }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val text = if (tupletSelected) getString(R.string.editor_tuplet_remove)
                else getString(R.string.editor_tuplet_add)
            Icon(
                painter = painterResource(R.drawable.outline_avg_pace_24),
                contentDescription = text,
                modifier = Modifier.align(Alignment.CenterVertically),
                tint = textColor
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = text,
                modifier = Modifier.align(Alignment.CenterVertically),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        if(showDialog) {
            val tuplet = getNewTuplet(ratio) ?: return
            ratio = tuplet.ratio

            Dialog(
                onDismissRequest = { showDialog = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.width(IntrinsicSize.Max)
                            .padding(16.dp)
                    ) {
                        Text(
                            getString(R.string.editor_tuplet_add),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))

                        Box(
                            modifier = Modifier.width(400.dp)
                                .height(128.dp)
                                .padding(horizontal = 32.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    RoundedCornerShape(16.dp)
                                )
                                .offset(y = 16.dp)
                        ) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                for (element in tuplet.notes) {
                                    item { NoteText(element, 0, errored = false, editable = false) }
                                }
                            }
                            // tuplet header
                            Box(
                                modifier = Modifier.padding(horizontal = 8.dp)
                                    .matchParentSize()
                            ) {
                                Row(
                                    modifier = Modifier.align(Alignment.TopCenter)
                                ) {
                                    Box(
                                        modifier = Modifier.width(1.dp)
                                            .height(8.dp)
                                            .align(Alignment.CenterVertically)
                                            .offset(0.dp, 4.dp)
                                            .background(MaterialTheme.colorScheme.onSurface)
                                    )
                                    Box(
                                        modifier = Modifier.weight(1f)
                                            .height(1.dp)
                                            .align(Alignment.CenterVertically)
                                            .background(MaterialTheme.colorScheme.onSurface)
                                    )
                                    Text(
                                        "${tuplet.ratio.first}:${tuplet.ratio.second}",
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                            .padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                    Box(
                                        modifier = Modifier.weight(1f)
                                            .height(1.dp)
                                            .align(Alignment.CenterVertically)
                                            .background(MaterialTheme.colorScheme.onSurface)
                                    )
                                    Box(
                                        modifier = Modifier.width(1.dp)
                                            .height(8.dp)
                                            .align(Alignment.CenterVertically)
                                            .offset(0.dp, 4.dp)
                                            .background(MaterialTheme.colorScheme.onSurface)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            FilledTonalIconButton(onClick = {
                                val first = ratio!!.first - 1
                                if (first < 1) return@FilledTonalIconButton
                                var second = 1
                                while (second < 1024) {
                                    second *= 2
                                    if (first < second) {
                                        second /= 2
                                        break
                                    }
                                }

                                ratio = first to second
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_remove_24),
                                    contentDescription = getString(R.string.generic_subtract)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = {
                                showDialog = false

                                var globalIndex = 0
                                var foundElement: RhythmElement? = null
                                var foundMeasureIndex = 0
                                var foundElementIndex = 0
                                for ((measureIndex, measure) in parsedRhythm.measures.withIndex()) {
                                    for ((elementIndex, element) in measure.elements.withIndex()) {
                                        when (element) {
                                            is RhythmNote -> {
                                                if (globalIndex + 1 > noteSelected) {
                                                    foundElement = element
                                                    foundMeasureIndex = measureIndex
                                                    foundElementIndex = elementIndex
                                                    break
                                                }
                                                globalIndex += 1
                                            }

                                            is RhythmTuplet -> {
                                                if (globalIndex + element.notes.size > noteSelected) {
                                                    foundElement = element
                                                    foundMeasureIndex = measureIndex
                                                    foundElementIndex = elementIndex
                                                    break
                                                }
                                                globalIndex += element.notes.size
                                            }
                                        }
                                    }
                                    if (foundElement != null) break
                                }

                                val newMeasure = Measure(
                                    timeSig = parsedRhythm.measures[foundMeasureIndex].timeSig,
                                    elements = parsedRhythm.measures[foundMeasureIndex].elements.toMutableList().apply {
                                        if (foundElement is RhythmNote) {
                                            this[foundElementIndex] = tuplet
                                        } else if (foundElement is RhythmTuplet) {
                                            this[foundElementIndex] = tuplet
                                        }
                                    }
                                )
                                val newMeasures = parsedRhythm.measures.toMutableList().apply {
                                    this[foundMeasureIndex] = newMeasure
                                }
                                parsedRhythm = Rhythm(newMeasures)
                                metronome.setRhythm(parsedRhythm)
                            }) {
                                Text(getString(R.string.generic_confirm))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            FilledTonalIconButton(onClick = {
                                val first = ratio!!.first + 1
                                var second = 1
                                while (second < 1024) {
                                    second *= 2
                                    if (first < second) {
                                        second /= 2
                                        break
                                    }
                                }

                                ratio = first to second
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = getString(R.string.generic_add)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun ColumnScope.ToggleDot(enabled: Boolean, oldElement: RhythmNote?, dots: Int) {
        val backgroundColor by animateColorAsState(
            targetValue = if (!enabled) MaterialTheme.colorScheme.surfaceContainer
            else when (dots) {
                1 -> MaterialTheme.colorScheme.primaryContainer
                2 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            },
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "backgroundColor"
        )
        val textColor by animateColorAsState(
            targetValue = if (!enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else when (dots) {
                1 -> MaterialTheme.colorScheme.onPrimaryContainer
                2 -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.onSecondaryContainer
            },
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "textColor"
        )

        Row(
            modifier = Modifier.weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .clickable(enabled && oldElement != null) {
                    val oldDuration = oldElement!!.duration
                    val oldDotModifier = 1 + (1..oldElement.dots).sumOf { 1.0 / (2.0.pow(it)) }

                    val newDotModifier = 1 + (1..dots).sumOf { 1.0 / (2.0.pow(it)) }
                    val newDuration = (oldDuration / oldDotModifier) * newDotModifier

                    val newNote = RhythmNote(
                        display = oldElement.display.replace(
                            " ${MusicFont.Notation.DOT.char}",
                            ""
                        ) + " ${MusicFont.Notation.DOT.char}".repeat(dots),
                        isRest = oldElement.isRest,
                        isInverted = oldElement.isInverted,
                        rawDuration = oldElement.rawDuration,
                        duration = newDuration,
                        dots = dots
                    )

                    val newRhythm = setNote(noteSelected, newNote, isScaled = true)
                    parsedRhythm = newRhythm
                    rhythm = newRhythm.serialize()
                    metronome.setRhythm(parsedRhythm)
                }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_music_note_24),
                contentDescription = getString(R.string.editor_toggle_dot),
                modifier = Modifier.align(Alignment.CenterVertically),
                tint = textColor
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = getString(R.string.editor_toggle_dot),
                modifier = Modifier.align(Alignment.CenterVertically),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun ColumnScope.NoteButton(value: Int, rest: Boolean, emphasized: Boolean, enabled: Boolean) {
        if(value > 1024) {
            Box(
                modifier = Modifier.weight(1f)
                    .padding(0.dp, 4.dp, 0.dp, 8.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            )
            return
        }

        val animatedColor = animateColorAsState(
            targetValue = if (!enabled) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "animatedColor"
        )
        val animatedOnColor = animateColorAsState(
            targetValue = if (!enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "animatedOnColor"
        )

        Box(
            modifier = Modifier.weight(1f)
                .padding(0.dp, 8.dp, 0.dp, 4.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(animatedColor.value)
                .clickable(enabled) {
                    val restModifier = if (rest) -1 else 1
                    val newRhythm = setNote(
                        noteSelected,
                        RhythmNote(
                            display = MusicFont.Notation.setEmphasis(
                                MusicFont.Notation.convert(value, rest).toString(),
                                emphasized
                            ),
                            isRest = rest,
                            isInverted = !emphasized,
                            rawDuration = 1.0 / value,
                            duration = 1.0 / value * restModifier,
                            dots = 0
                        ),
                        isScaled = false
                    )
                    parsedRhythm = newRhythm
                    rhythm = newRhythm.serialize()
                    metronome.setRhythm(parsedRhythm)
                }
        ) {
            if (value >= 256) {
                val animatedFontWeight by animateIntAsState(
                    targetValue = if (rest) 300 else 900,
                    animationSpec = MotionScheme.expressive().fastEffectsSpec(),
                    label = "animatedFontWeight"
                )
                Text(
                    "$value",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight(animatedFontWeight),
                )
            } else {
                val text = MusicFont.Notation.setEmphasis(MusicFont.Notation.convert(value, rest).toString(), emphasized)[0]
                var note: MusicFont.Notation? = null
                for (character in MusicFont.Notation.entries) {
                    if (character.char == text) {
                        note = character
                    }
                }
                MusicFont.Notation.NoteCentered(
                    note = note ?: MusicFont.Notation.N_QUARTER,
                    color = animatedOnColor.value,
                    size = 40.dp,
                    modifier = Modifier.align(Alignment.Center)
                )

                // staff line
                val yOffset = 40.dp * (note ?: MusicFont.Notation.N_QUARTER).offset.y
                Box(
                    modifier = Modifier.fillMaxWidth(0.25f)
                        .height(1.dp)
                        .offset(y = (if (!rest) yOffset - 4.dp else 0.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        .align(Alignment.Center)
                )
            }
        }
    }

    @Composable
    fun DrawRhythm(rhythm: Rhythm, indexOffset: Int = 0, measureOffset: Int = 0, scale: Double = 1.0,
                   allErrored: Boolean = false, updateBackup: Boolean = false, editable: Boolean = true, complete: Boolean = true) {
        var anyError = false
        var globalIndex = indexOffset
        var previousTimeSig = 0 to 0

        rhythm.measures.forEachIndexed { measureIndex, measure ->
            var measureErrored = false
            if(measure.timeSig.first != 0 && measure.timeSig.second != 0) {
                if ((measure.timeSig.first <= 0 || measure.timeSig.second <= 0) && editable) {
                    val error = getString(
                        R.string.editor_error_invalid_time_signature, measureIndex + measureOffset + 1,
                        "${measure.timeSig.first}/${measure.timeSig.second}")
                    if (!errors.contains(error)) {
                        Log.e("RhythmEditorActivity", error)
                        errors.add(error)
                    }
                    anyError = true
                    measureErrored = true
                }
                if ((measure.timeSig.second and (measure.timeSig.second - 1) != 0) && editable) {
                    val error = getString(
                        R.string.editor_error_invalid_denominator, measureIndex + measureOffset + 1,
                        "${measure.timeSig.first}/${measure.timeSig.second}")
                    if (!errors.contains(error)) {
                        Log.e("RhythmEditorActivity", error)
                        errors.add(error)
                    }
                    anyError = true
                    measureErrored = true
                }

                val measureDuration = measure.timeSig.first.toDouble() / measure.timeSig.second
                val measureLength = measure.elements.sumOf {
                    when (it) {
                        is RhythmNote -> abs(it.duration) * scale
                        is RhythmTuplet -> it.notes.sumOf { note -> abs(note.duration) / scale }
                    }
                }
                if(measureDuration != measureLength && editable) {
                    val comparison = if(measureLength > measureDuration) ">" else "<"
                    val error = getString(
                        R.string.editor_error_invalid_length, measureIndex + measureOffset + 1,
                        "${measure.timeSig.first}/${measure.timeSig.second}", "$measureLength $comparison $measureDuration")
                    if(!errors.contains(error)) {
                        Log.e("RhythmEditorActivity", error)
                        errors.add(error)
                    }
                    anyError = true
                    measureErrored = true
                }
            } else if(complete && editable) {
                val error = getString(
                    R.string.editor_error_invalid_time_signature, measureIndex + measureOffset + 1,
                    "${measure.timeSig.first}/${measure.timeSig.second}")
                if(!errors.contains(error)) {
                    Log.e("RhythmEditorActivity", error)
                    errors.add(error)
                }
                anyError = true
                measureErrored = true
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(0.dp, 8.dp, if (indexOffset == 0 && editable) 32.dp else 0.dp, 8.dp)
                        .background(
                            if ((measureErrored || allErrored) && editable) MaterialTheme.colorScheme.errorContainer.copy(
                                alpha = 0.5f
                            ) else Color.Transparent
                        )
                ) {
                    if (measureIndex != 0) {
                        MeasureBreak(measureIndex, measure.timeSig != previousTimeSig)
                    }
                    if ((measureIndex == 0 || measure.timeSig != previousTimeSig) && measure.timeSig != 0 to 0) {
                        TimeSignatureDisplay(measureIndex)
                    }

                    measure.elements.forEach { element ->
                        when (element) {
                            is RhythmNote -> {
                                var errored = false
                                if (element.display.contains("?") && editable) {
                                    val errorDisplay = element.display.replace(MusicFont.Notation.DOT.char, '.')
                                    val error = getString(
                                        R.string.editor_error_invalid_note,
                                        globalIndex,
                                        errorDisplay,
                                        element.duration
                                    )
                                    if (!errors.contains(error)) {
                                        Log.e("RhythmEditorActivity", error)
                                        errors.add(error)
                                    }
                                    anyError = true
                                    errored = true
                                }
                                NoteText(element, globalIndex++, errored, editable)
                            }

                            is RhythmTuplet -> {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .padding(16.dp, 0.dp)
                                ) {
                                    Row {
                                        val tupletRhythm = Rhythm(
                                            arrayListOf(Measure(0 to 0, element.notes))
                                        )
                                        DrawRhythm(
                                            tupletRhythm,
                                            globalIndex,
                                            measureIndex,
                                            element.ratio.second.toDouble() / element.ratio.first,
                                            allErrored = allErrored,
                                            editable = editable,
                                            complete = false
                                        )
                                        globalIndex += element.notes.size
                                    }
                                    // tuplet header
                                    Box(
                                        modifier = Modifier.padding(8.dp, 0.dp)
                                            .matchParentSize()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .align(Alignment.TopCenter)
                                        ) {
                                            Box(
                                                modifier = Modifier.width(1.dp)
                                                    .height(8.dp)
                                                    .align(Alignment.CenterVertically)
                                                    .offset(0.dp, 4.dp)
                                                    .background(MaterialTheme.colorScheme.onSurface)
                                            )
                                            Box(
                                                modifier = Modifier.weight(1f)
                                                    .height(1.dp)
                                                    .align(Alignment.CenterVertically)
                                                    .background(MaterialTheme.colorScheme.onSurface)
                                            )
                                            Text(
                                                "${element.ratio.first}:${element.ratio.second}",
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                                    .padding(16.dp, 0.dp),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.labelLarge,
                                            )
                                            Box(
                                                modifier = Modifier.weight(1f)
                                                    .height(1.dp)
                                                    .align(Alignment.CenterVertically)
                                                    .background(MaterialTheme.colorScheme.onSurface)
                                            )
                                            Box(
                                                modifier = Modifier.width(1.dp)
                                                    .height(8.dp)
                                                    .align(Alignment.CenterVertically)
                                                    .offset(0.dp, 4.dp)
                                                    .background(MaterialTheme.colorScheme.onSurface)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    previousTimeSig = measure.timeSig
                }
            }
        }
        if(updateBackup && !anyError) {
            backupRhythm = rhythm
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun NoteText(note: RhythmNote, noteIndex: Int, errored: Boolean = false, editable: Boolean = true) {
        val isNoteSelected = noteIndex == noteSelected
        val isMusicSelected = noteIndex == musicSelected

        val color = animateColorAsState(
            targetValue = if (!editable) Color.Transparent
            else if (isNoteSelected) MaterialTheme.colorScheme.secondaryContainer
            else if (isMusicSelected) MaterialTheme.colorScheme.tertiaryContainer
            else if (errored) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceContainer,
            animationSpec = MotionScheme.expressive().fastEffectsSpec(),
            label = "noteColor"
        )

        val corner = animateFloatAsState(
            targetValue = if (((isNoteSelected || errored) && !isMusicSelected)) 100f else 25f,
            animationSpec = MotionScheme.expressive().defaultSpatialSpec(),
            label = "corner"
        )

        Box(
            modifier = Modifier.padding(16.dp, 0.dp)
                .width(48.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(corner.value.toInt().coerceIn(0, 100)))
                .background(color.value)
                .clickable(editable) {
                    noteSelected = if (isNoteSelected) -1 else noteIndex
                }
        ) {
            val durationChar = MusicFont.Notation.fromLength(note.rawDuration)
            val char = MusicFont.Notation.setEmphasis(durationChar ?: MusicFont.Notation.N_QUARTER, !note.isInverted)

            MusicFont.Notation.Note(
                note = char,
                dots = note.dots,
                color = if (errored) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface,
                size = 40.dp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    @Composable
    fun MeasureBreak(index: Int, hasTimeSignature: Boolean) {
        Box(
            modifier = Modifier.padding(0.dp, 0.dp, if (hasTimeSignature) 0.dp else 32.dp, 0.dp)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier.width(1.dp)
                    .fillMaxHeight(0.75f)
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.outline)
            )
            Text(
                text = (index + 1).toString(),
                modifier = Modifier.width(1.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Visible,
                maxLines = 1,
                softWrap = false
            )
        }
    }

    @Composable
    fun RowScope.TimeSignatureDisplay(measureIndex: Int) {
        val measure = parsedRhythm.measures[measureIndex]
        val (numerator, denominator) = measure.timeSig

        Box(
            modifier = Modifier.padding(end = 16.dp)
                .fillMaxHeight()
                .align(Alignment.CenterVertically)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable {
                    showTimeSignature = measureIndex
                }
                .padding(10.dp, 0.dp)
        ) {
            Text(
                text = (measureIndex + 1).toString(),
                modifier = Modifier.width(1.dp)
                    .align(Alignment.TopStart),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Visible,
                maxLines = 1,
                softWrap = false
            )
            Box(
                modifier = Modifier.fillMaxHeight(0.45f)
                    .align(Alignment.Center)
            ) {
                MusicFont.Number.TimeSignature(numerator, denominator, MaterialTheme.colorScheme.onSurface)
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun TimeSignatureDialog(measureIndex: Int) {
        val measure = parsedRhythm.measures[measureIndex]
        var timeSignature by remember { mutableStateOf(parsedRhythm.measures[measureIndex].timeSig) }
        AlertDialog(
            onDismissRequest = { showTimeSignature = -1 },
            title = { Text(getString(R.string.editor_set_time_signature_dialog)) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(0.75f)
                            .aspectRatio(1f)
                            .align(Alignment.Center)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialShapes.Bun.toShape(0)
                            )
                    ) {
                        Row(
                            modifier = Modifier.weight(1f)
                                .padding(horizontal = 32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = {
                                    timeSignature = Pair((timeSignature.first - 1).coerceIn(1..32), timeSignature.second)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                                    contentDescription = getString(R.string.generic_subtract),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f)
                                    .fillMaxHeight(0.8f)
                                    .align(Alignment.CenterVertically),
                                contentAlignment = Alignment.Center
                            ) {
                                MusicFont.Number.TimeSignatureLine(
                                    timeSignature.first,
                                    MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = {
                                    timeSignature = Pair((timeSignature.first + 1).coerceIn(1..32), timeSignature.second)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                                    contentDescription = getString(R.string.generic_add),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.weight(1f)
                                .padding(horizontal = 32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = {
                                    timeSignature = Pair(timeSignature.first, (timeSignature.second / 2).coerceIn(1..32))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                                    contentDescription = getString(R.string.generic_subtract),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f)
                                    .fillMaxHeight(0.8f)
                                    .align(Alignment.CenterVertically),
                                contentAlignment = Alignment.Center
                            ) {
                                MusicFont.Number.TimeSignatureLine(
                                    timeSignature.second,
                                    MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = {
                                    timeSignature = Pair(timeSignature.first, (timeSignature.second * 2).coerceIn(1..32))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                                    contentDescription = getString(R.string.generic_add),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    setTimeSignature(measureIndex, measure.timeSig, timeSignature)
                    showTimeSignature = -1
                }) {
                    Text(getString(R.string.generic_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeSignature = -1 }) {
                    Text(getString(R.string.generic_cancel))
                }
            }
        )
    }

    fun setTimeSignature(measureIndex: Int, old: Pair<Int, Int>, new: Pair<Int, Int>) {
        val measure = parsedRhythm.measures[measureIndex]
        val newElements = mutableListOf<RhythmElement>()
        val oldDuration = old.first / old.second.toDouble()
        val newDuration = new.first / new.second.toDouble()
        if(newDuration > oldDuration) { // extend measure
            newElements.addAll(measure.elements)
            var remaining = newDuration - oldDuration
            var restValue = 1
            while (1.0 / restValue > remaining + 1e-6) {
                restValue *= 2
            }
            val newRests = arrayListOf<RhythmNote>()

            while (remaining > 1e-6) {
                val duration = 1.0 / restValue
                if (remaining >= duration - 1e-6) {
                    newRests.add(
                        RhythmNote(
                            display = MusicFont.Notation.convert(restValue, true).toString(),
                            isRest = true,
                            isInverted = false,
                            duration = -duration,
                            dots = 0
                        )
                    )
                    remaining -= duration
                } else {
                    restValue *= 2
                }
            }
            newElements.addAll(newRests.reversed())
        } else if(newDuration < oldDuration) { // shorten measure
            var remaining = newDuration
            for(element in measure.elements) {
                when(element) {
                    is RhythmNote -> {
                        if(remaining - abs(element.duration) >= 0) { // keep
                            newElements.add(element)
                            remaining -= abs(element.duration)
                        } else { // remove
                            break
                        }
                    }
                    is RhythmTuplet -> {
                        val tupleDuration = element.notes.sumOf { abs(it.duration) }
                        if(remaining - tupleDuration >= 0) { // keep
                            newElements.add(element)
                            remaining -= tupleDuration
                        } else { // remove
                            break
                        }
                    }
                }
            }

            if(remaining >= 0) { // add rests to fill measure
                var restValue = 1
                while (1.0 / restValue > remaining + 1e-6) {
                    restValue *= 2
                }
                val newRests = arrayListOf<RhythmNote>()

                while(restValue < 1024 && remaining > 0) {
                    if (1.0 / restValue <= remaining) {
                        newRests.add(
                            RhythmNote(
                                display = MusicFont.Notation.convert(restValue, true).toString(),
                                isRest = true,
                                isInverted = false,
                                duration = 1.0 / restValue,
                                dots = 0
                            )
                        )
                        remaining -= 1.0 / restValue
                    } else {
                        restValue *= 2
                    }
                }
                newElements.addAll(newRests.reversed())
            } else {
                return
            }
        } else { // same length
            newElements.addAll(measure.elements)
        }

        val newMeasure = Measure(
            timeSig = new,
            elements = newElements
        )
        val measures = parsedRhythm.measures.toMutableList()
        measures[measureIndex] = newMeasure
        parsedRhythm = Rhythm(measures)
        metronome.setRhythm(parsedRhythm)
    }

    fun getNewTuplet(defaultRatio: Pair<Int, Int>? = null): RhythmTuplet? {
        var globalIndex = 0
        var measureIndex = 0
        var foundElement: RhythmElement? = null
        for(measure in parsedRhythm.measures) {
            for(element in measure.elements) {
                if(element is RhythmNote) {
                    if (globalIndex == noteSelected) {
                        foundElement = element
                        break
                    }
                    globalIndex++
                } else if(element is RhythmTuplet) {
                    for(note in element.notes) {
                        if (globalIndex == noteSelected) {
                            foundElement = element
                            break
                        }
                        globalIndex++
                        if (foundElement != null) break
                    }
                }
            }
            if (foundElement != null) break
            measureIndex++
        }
        if(foundElement == null) {
            Log.e("RhythmEditorActivity", "No note found at index $noteSelected")
            return null
        }
        val elementDuration = when (foundElement) {
            is RhythmTuplet -> {
                foundElement.notes.sumOf { abs(it.duration) }
            }
            is RhythmNote -> {
                abs(foundElement.duration)
            }
        }
        val dottedModifier = if(foundElement is RhythmNote) {
            1 + (1..foundElement.dots).sumOf { 1.0 / (2.0.pow(it)) }
        } else {
            1.0
        }

        val ratio = defaultRatio ?: if(foundElement is RhythmTuplet) {
            foundElement.ratio
        } else {
            3 to 2
        }

        //convert duration to note
        var value = 1
        while(value < 1024) {
            val noteDuration = 1.0 / value
            if(noteDuration * dottedModifier * ratio.second.toDouble() <= elementDuration) {
                break
            }
            value *= 2
        }

        val tupleElement = RhythmNote(
            display = MusicFont.Notation.convert(value, false).toString(),
            isRest = false,
            isInverted = false,
            rawDuration = 1.0 / value,
            duration = (1.0 / value) * dottedModifier * (ratio.second.toDouble() / ratio.first),
            dots = 0
        )
        return RhythmTuplet(
            ratio = ratio,
            notes = arrayListOf<RhythmNote>().apply {
                repeat(ratio.first) {
                    add(tupleElement)
                }
            }
        )
    }

    private fun getNote(index: Int): RhythmNote? {
        var globalIndex = 0
        for (measure in parsedRhythm.measures) {
            for (element in measure.elements) {
                when(element) {
                    is RhythmNote -> {
                        if (globalIndex == index) {
                            return element
                        }
                        globalIndex++
                    }
                    is RhythmTuplet -> {
                        for (note in element.notes) {
                            if (globalIndex == index) {
                                return note
                            }
                            globalIndex++
                        }
                    }
                }
            }
        }
        return null
    }

    private fun setNote(noteIndex: Int, newNote: RhythmElement, isScaled: Boolean): Rhythm {
        var valueDuration = when(newNote) {
            is RhythmNote -> abs(newNote.duration)
            is RhythmTuplet -> abs(newNote.notes.sumOf { abs(it.duration) })
        }

        var newMeasure: Measure? = null
        var newMeasureIndex = -1

        var globalIndex = 0
        for((measureIndex, measure) in parsedRhythm.measures.withIndex()) {
            if(newMeasure != null) break

            val timeSig = measure.timeSig
            var currentBeat = 0.0

            val newElements = mutableListOf<RhythmElement>()

            for ((index, element) in measure.elements.withIndex()) {
                when (element) {
                    is RhythmNote -> {
                        if (globalIndex == noteIndex) {
                            newElements.add(newNote)

                            if(abs(element.duration) > valueDuration) { // add rests
                                var remaining = abs(element.duration) - valueDuration
                                var restValue = 1
                                while (1.0 / restValue > remaining + 1e-6) {
                                    restValue *= 2
                                }
                                val newRests = arrayListOf<RhythmNote>()

                                while (remaining > 1e-6) {
                                    val duration = 1.0 / restValue
                                    if (remaining >= duration - 1e-6) {
                                        newRests.add(RhythmNote(
                                            display = MusicFont.Notation.convert(restValue, true).toString(),
                                            isRest = true,
                                            isInverted = false,
                                            duration = -duration,
                                            dots = 0
                                        ))
                                        remaining -= duration
                                    } else {
                                        restValue *= 2
                                    }
                                }

                                newElements.addAll(newRests.reversed())

                                // add rest of measure
                                for(extraElement in measure.elements.subList(index + 1, measure.elements.size)) {
                                    newElements.add(extraElement)
                                }

                                newMeasure = Measure(
                                    timeSig = timeSig,
                                    elements = newElements
                                )
                                newMeasureIndex = measureIndex
                                break
                            } else if(abs(element.duration) < valueDuration) { // remove extra notes
                                var remainingDuration = abs(valueDuration)
                                var offset = 0
                                for(extraElement in measure.elements.subList(index, measure.elements.size)) {
                                    when(extraElement) {
                                        is RhythmNote -> {
                                            if(remainingDuration <= 0) { // keep
                                                break
                                            } else { // remove
                                                remainingDuration -= abs(extraElement.duration)
                                                offset++
                                            }
                                        }
                                        is RhythmTuplet -> {
                                            if(remainingDuration <= 0) { // keep
                                                break
                                            } else { // remove
                                                remainingDuration -= abs(extraElement.notes.sumOf { abs(it.duration) })
                                                offset++
                                            }
                                        }
                                    }
                                }
                                if(remainingDuration <= 0) { // add rests to fill measure
                                    remainingDuration *= -1
                                    var restValue = 1
                                    while(restValue < 1024 && remainingDuration > 0) {
                                        if(1.0 / restValue <= remainingDuration) {
                                            newElements.add(RhythmNote(
                                                display = MusicFont.Notation.convert(restValue, true).toString(),
                                                isRest = true,
                                                isInverted = false,
                                                duration = -1.0 / restValue,
                                                dots = 0
                                            ))
                                            remainingDuration -= 1.0 / restValue
                                        }
                                        restValue *= 2
                                    }
                                } else {
                                    return parsedRhythm
                                }

                                // add rest of measure
                                for(extraElement in measure.elements.subList(index + offset, measure.elements.size)) {
                                    newElements.add(extraElement)
                                }

                                newMeasure = Measure(
                                    timeSig = timeSig,
                                    elements = newElements
                                )
                                newMeasureIndex = measureIndex
                                break
                            } else { // same note duration
                                for(extraElement in measure.elements.subList(index + 1, measure.elements.size)) {
                                    newElements.add(extraElement)
                                }

                                newMeasure = Measure(
                                    timeSig = timeSig,
                                    elements = newElements
                                )
                                newMeasureIndex = measureIndex
                                break
                            }
                        } else {
                            newElements.add(element)
                        }
                        currentBeat += abs(element.duration)
                        globalIndex++
                    }

                    is RhythmTuplet -> {
                        val scale = element.ratio.second.toDouble() / element.ratio.first
                        var isFound = false
                        val newTupletElements = mutableListOf<RhythmNote>()
                        var currentTupleBeat = 0.0
                        for ((tupleIndex, tuple) in element.notes.withIndex()) {
                            if(globalIndex == noteIndex) {
                                isFound = true
                                if(newNote is RhythmNote) {
                                    if(isScaled) {
                                        newTupletElements.add(newNote)
                                    } else {
                                        valueDuration *= scale
                                        newTupletElements.add(newNote.copy(
                                            duration = newNote.duration * scale
                                        ))
                                    }
                                    if (abs(tuple.duration) > valueDuration) { // add rests
                                        var remaining = (abs(tuple.duration) - valueDuration) / scale
                                        var restValue = 1
                                        while (1.0 / restValue > remaining + 1e-6) {
                                            restValue *= 2
                                        }

                                        val newRests = arrayListOf<RhythmNote>()

                                        while (remaining > 1e-10) {
                                            val duration = 1.0 / restValue
                                            if (duration <= remaining) {
                                                newRests.add(RhythmNote(
                                                    display = MusicFont.Notation.convert(restValue, true).toString(),
                                                    isRest = true,
                                                    isInverted = false,
                                                    rawDuration = -duration,
                                                    duration = -duration * scale,
                                                    dots = 0
                                                ))
                                                remaining -= duration
                                            } else {
                                                restValue *= 2
                                            }
                                        }
                                        newTupletElements.addAll(newRests.reversed())

                                        // add rest of measure
                                        for(extraElement in element.notes.subList(tupleIndex + 1, element.notes.size)) {
                                            newTupletElements.add(extraElement)
                                        }
                                        break
                                    } else if(abs(tuple.duration) < valueDuration) { // remove extra notes
                                        var remainingDuration = valueDuration
                                        var offset = 0
                                        for(extraElement in element.notes.subList(tupleIndex, element.notes.size)) {
                                            if(remainingDuration <= 0) { // keep
                                                break
                                            } else { // remove
                                                remainingDuration -= abs(extraElement.duration)
                                                offset++
                                            }
                                        }
                                        if(remainingDuration <= 1e-10) {
                                            remainingDuration *= -1
                                            var restValue = 1
                                            while(restValue < 1024 && remainingDuration > 0) {
                                                if((1.0 / restValue) * scale <= remainingDuration + 1e-10) {
                                                    newTupletElements.add(RhythmNote(
                                                        display = MusicFont.Notation.convert(restValue, true).toString(),
                                                        isRest = true,
                                                        isInverted = false,
                                                        rawDuration = -1.0 / restValue,
                                                        duration = (-1.0 / restValue) * scale,
                                                        dots = 0
                                                    ))
                                                    remainingDuration -= (1.0 / restValue) * scale
                                                }
                                                restValue *= 2
                                            }
                                        } else {
                                            return parsedRhythm
                                        }

                                        for(extraElement in element.notes.subList(tupleIndex + offset, element.notes.size)) {
                                            newTupletElements.add(extraElement)
                                        }
                                        break
                                    } else { // same note duration
                                        for(extraElement in element.notes.subList(tupleIndex + 1, element.notes.size)) {
                                            newTupletElements.add(extraElement)
                                        }
                                        break
                                    }
                                }
                            } else {
                                newTupletElements.add(tuple)
                            }
                            currentTupleBeat += abs(tuple.duration)
                            currentBeat += abs(tuple.duration)
                            globalIndex++
                        }

                        if(isFound) {

                            if(newNote is RhythmTuplet) {
                                newElements.add(newNote)
                            } else {
                                newElements.add(
                                    RhythmTuplet(
                                        ratio = element.ratio,
                                        notes = newTupletElements.toList()
                                    )
                                )
                            }

                            // add rest of measure
                            for(extraElement in measure.elements.subList(index + 1, measure.elements.size)) {
                                newElements.add(extraElement)
                            }

                            newMeasure = Measure(
                                timeSig = timeSig,
                                elements = newElements
                            )
                            newMeasureIndex = measureIndex
                            break
                        } else {
                            newElements.add(
                                RhythmTuplet(
                                    ratio = element.ratio,
                                    notes = element.notes
                                )
                            )
                        }
                    }
                }
            }
        }
        if(newMeasure == null) return parsedRhythm

        val newRhythm = Rhythm(
            parsedRhythm.measures.mapIndexed { index, measure ->
                if (index == newMeasureIndex) {
                    newMeasure
                } else {
                    measure
                }
            }
        )

        return newRhythm
    }
}