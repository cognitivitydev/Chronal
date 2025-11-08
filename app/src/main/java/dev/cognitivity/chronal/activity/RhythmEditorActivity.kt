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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import dev.cognitivity.chronal.MetronomeTrack
import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.SimpleRhythm
import dev.cognitivity.chronal.rhythm.metronome.Measure
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.rhythm.metronome.atoms
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmAtom
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmElement
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmNote
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmRest
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmTuplet
import dev.cognitivity.chronal.rhythm.metronome.elements.StemDirection
import dev.cognitivity.chronal.ui.metronome.PlayPauseIcon
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

class RhythmEditorActivity : ComponentActivity() {

    enum class NoteInputState {
        UP, DOWN, REST
    }

    private var errors = mutableStateListOf<String>()
    private var shownError by mutableStateOf(false)

    private var selectedNote by mutableIntStateOf(0)
    private var isSelected by mutableStateOf(false)
    private var musicSelected by mutableIntStateOf(-1)

    private var noteInputTuplet by mutableStateOf(false)
    private var noteInputDots by mutableIntStateOf(0)
    private var noteInputState by mutableStateOf(NoteInputState.UP)
    private var noteInputDuration by mutableStateOf(4)

    private var rhythm by mutableStateOf("{4/4}Q;q;q;q;")
    private var parsedRhythm by mutableStateOf(Rhythm.deserialize(rhythm))
    private var backupRhythm by mutableStateOf(parsedRhythm)

    private val metronome = Metronome(sendNotifications = false).apply {
        addTrack(0, MetronomeTrack(
            rhythm = parsedRhythm,
            bpm = 120,
            beatValue = 4f
        ))
    }
    private var appMetronome by mutableStateOf(ChronalApp.getInstance().metronome)
    private var mainTrack = metronome.getTrack(0)
    private var appTrack = appMetronome.getTrack(0)

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
        appTrack = appMetronome.getTrack(if(isPrimary) 0 else 1)

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

        appMetronome = ChronalApp.getInstance().metronome
        mainTrack.bpm = appTrack.bpm
        mainTrack.beatValue = appTrack.beatValue

        mainTrack.setRhythm(parsedRhythm)
        mainTrack.setUpdateListener(2) { beat ->
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
                                    is RhythmAtom -> 1
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
        mainTrack.setPauseListener(2) { isPaused ->
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
                // top row
                Box(
                    modifier = Modifier.fillMaxSize()
                        .weight(4f)
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        ))
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
                                            mainTrack.setRhythm(parsedRhythm)
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
                                                    add(RhythmRest(
                                                        baseDuration = 1.0 / timeSignature.second,
                                                        dots = 0
                                                    ))
                                                }
                                            }
                                            measures.add(Measure(
                                                timeSig = timeSignature,
                                                elements = elements
                                            ))
                                            parsedRhythm = Rhythm(measures)
                                            mainTrack.setRhythm(parsedRhythm)
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

                var atom: RhythmAtom? = null
                var isTuplet = false
                var maxDots = 0
                var remainingDuration = 0.0

                var globalIndex = 0
                for (measure in parsedRhythm.measures) {
                    val measureDuration = measure.timeSig.first / measure.timeSig.second.toDouble()
                    remainingDuration = measureDuration

                    for (element in measure.elements) {
                        when (element) {
                            is RhythmAtom -> {
                                if (globalIndex == selectedNote) {
                                    atom = element
                                    isTuplet = false
                                    globalIndex++
                                    break
                                }
                                globalIndex++
                                remainingDuration -= element.getDuration()
                            }

                            is RhythmTuplet -> {
                                val tupletDuration = element.getDuration()
                                var remainingTupletDuration = tupletDuration
                                for (i in 0 until element.notes.size) {
                                    if (globalIndex == selectedNote) {
                                        atom = element.notes[i]
                                        isTuplet = true
                                        globalIndex++
                                        remainingDuration = remainingTupletDuration
                                        break
                                    }
                                    globalIndex++
                                    remainingDuration -= element.notes[i].getDuration()
                                    remainingTupletDuration -= element.notes[i].getDuration()
                                }
                                if (isTuplet) break
                            }
                        }
                    }
                }
                noteInputDuration = if(atom == null) 0 else (1 / atom.baseDuration).roundToInt()
                noteInputTuplet = isTuplet
                noteInputDots = atom?.dots ?: 0

                if(atom != null) {
                    // calculate max dots
                    for(i in 0..2) {
                        val dotDuration = atom.baseDuration * (1 + (1..i).sumOf { 1.0 / (2.0.pow(it)) })
                        Log.d("a", "$dotDuration <= ${remainingDuration - atom.getDuration()} (${remainingDuration} - ${atom.getDuration()})")
                        if(dotDuration <= remainingDuration) {
                            maxDots = i
                        }
                    }
                }

                // input row
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .weight(4f)
                        .padding(vertical = 4.dp)
                ) {
                    InputRow(maxDots, remainingDuration,
                        modifier = Modifier.fillMaxHeight()
                            .align(Alignment.Center)
                            .horizontalScroll(rememberScrollState())
                            .padding(4.dp)
                    )
                }

                // bottom row
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .weight(2f)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        ))
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
                                    mainTrack.setRhythm(parsedRhythm)
                                    mainTrack.bpm = appTrack.bpm
                                    mainTrack.beatValue = appTrack.beatValue

                                    ChronalApp.getInstance().settings.metronomeState.value = MetronomeState(
                                        bpm = mainTrack.bpm,
                                        beatValuePrimary = if(isPrimary) mainTrack.beatValue else appMetronome.getTrack(0).beatValue,
                                        beatValueSecondary = if(isPrimary) appMetronome.getTrack(1).beatValue else mainTrack.beatValue,
                                        secondaryEnabled = !isPrimary && appMetronome.getTrack(1).enabled
                                    )

                                    if (isPrimary) {
                                        ChronalApp.getInstance().settings.metronomeRhythm.value = parsedRhythm.serialize()
                                        ChronalApp.getInstance().settings.metronomeSimpleRhythm.value = SimpleRhythm(0 to 0, 0, 0)
                                    } else {
                                        ChronalApp.getInstance().settings.metronomeRhythmSecondary.value = parsedRhythm.serialize()
                                        ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary.value = SimpleRhythm(0 to 0, 0, 0)
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
                    Row(
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .weight(1f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            enabled = false,
                            onClick = {
                                // TODO undo
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_undo_24),
                                contentDescription = getString(R.string.editor_undo),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            enabled = false,
                            onClick = {
                                // TODO redo
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_redo_24),
                                contentDescription = getString(R.string.editor_redo),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
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
                            note = MusicFont.Notation.getBeatValue(mainTrack.beatValue).first,
                            dots = if(MusicFont.Notation.getBeatValue(mainTrack.beatValue).second) 1 else 0,
                            modifier = Modifier.align(Alignment.CenterVertically),
                            color = MaterialTheme.colorScheme.onSurface,
                            size = 32.dp,
                        )
                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )
                        Text(
                            "= ${mainTrack.bpm}",
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
                                    mainTrack.setRhythm(parsedRhythm)
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
                            mainTrack.setRhythm(parsedRhythm)
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
        if(showBpm) {
            EditBpmDialog()
        }
        if(showTimeSignature != -1) {
            TimeSignatureDialog(showTimeSignature)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InputRow(maxDots: Int, largestDuration: Double, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // editor buttons
            Row(
                modifier = Modifier.fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeSignatureInput(
                        modifier = Modifier.width(64.dp)
                            .weight(1f)
                    )
                    TupletInput(
                        modifier = Modifier.width(64.dp)
                            .weight(1f)
                    )
                }
                DotInput(maxDots,
                    modifier = Modifier.width(64.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(16.dp))
                        .padding(4.dp)
                )
                StateInput(
                    modifier = Modifier.width(64.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(16.dp))
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // input buttons
            Row(
                modifier = Modifier.fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (i in 0..4) {
                    Column(
                        modifier = Modifier.width(IntrinsicSize.Max)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val dotModifier = 1 + (1..noteInputDots).sumOf { 1.0 / (2.0.pow(it)) }
                        val topValue = (2.0).pow(i * 2).toInt()
                        val topDuration = (1.0 / topValue) * dotModifier
                        NoteButton(topValue, largestDuration >= topDuration)

                        val bottomValue = (2.0).pow(i * 2 + 1).toInt()
                        val bottomDuration = (1.0 / bottomValue) * dotModifier
                        NoteButton(bottomValue, largestDuration >= bottomDuration)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun TimeSignatureInput(modifier: Modifier = Modifier) {
        var globalIndex = 0
        var measureIndex = -1
        for ((index, measure) in parsedRhythm.measures.withIndex()) {
            for (element in measure.elements) {
                globalIndex += when (element) {
                    is RhythmAtom -> 1
                    is RhythmTuplet -> element.notes.size
                }
            }
            if (globalIndex > selectedNote) {
                measureIndex = index
                break
            }
        }
        val timeSig = parsedRhythm.measures.getOrNull(measureIndex)?.timeSig ?: (4 to 4)

        val selected = showTimeSignature == measureIndex

        val animatedColor = animateColorAsState(
            targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "animatedColor"
        )
        val animatedOnColor = animateColorAsState(
            targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "animatedOnColor"
        )

        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(animatedColor.value)
                .clickable {
                    showTimeSignature = if(showTimeSignature == -1) measureIndex else -1
                }
        ) {
            Box(
                modifier = Modifier.align(Alignment.Center)
                    .fillMaxHeight(0.8f)
            ) {
                MusicFont.Number.TimeSignature(timeSig.first, timeSig.second, animatedOnColor.value)
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun TupletInput(modifier: Modifier = Modifier) {
        val selected = noteInputTuplet
        var showDialog by remember { mutableStateOf(false) }

        val animatedColor = animateColorAsState(
            targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "animatedColor"
        )
        val animatedOnColor = animateColorAsState(
            targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "animatedOnColor"
        )

        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(animatedColor.value)
                .clickable {
                    if(!noteInputTuplet) {
                        noteInputTuplet = true
                        showDialog = true
                    } else {
                        noteInputTuplet = false
                        removeTuplet()
                    }
                }
        ) {
            Icon(
                painter = painterResource(R.drawable.tuplet),
                contentDescription = getString(R.string.editor_tuplet_add),
                tint = animatedOnColor.value,
                modifier = Modifier.align(Alignment.Center)
                    .aspectRatio(1f)
                    .padding(4.dp)
            )
        }

        if(showDialog) {
            TupletDialog { showDialog = false }
        }
    }

    private fun removeTuplet() {
        val foundAtom = parsedRhythm.atoms().firstOrNull { it.index == selectedNote }?.value
        if (foundAtom == null) return

        var foundMeasureIndex = -1
        var foundElementIndex = -1
        var foundTupletInnerIndex: Int? = null

        loop@ for ((measureIndex, measure) in parsedRhythm.measures.withIndex()) {
            for ((elementIndex, element) in measure.elements.withIndex()) {
                when (element) {
                    is RhythmTuplet -> {
                        for ((nIndex, note) in element.notes.withIndex()) {
                            if (note === foundAtom) {
                                foundMeasureIndex = measureIndex
                                foundElementIndex = elementIndex
                                foundTupletInnerIndex = nIndex

                                // move selection to first note in tuplet
                                selectedNote -= foundTupletInnerIndex

                                break@loop
                            }
                        }
                    }
                    is RhythmAtom -> {
                        if (element === foundAtom) {
                            foundMeasureIndex = measureIndex
                            foundElementIndex = elementIndex
                            break@loop
                        }
                    }
                }
            }
        }

        if (foundMeasureIndex == -1 || foundElementIndex == -1 || foundTupletInnerIndex == null) return

        val selectedTuplet = parsedRhythm.measures[foundMeasureIndex].elements[foundElementIndex] as? RhythmTuplet
            ?: return

        val duration = selectedTuplet.getDuration()
        // get dots
        for (i in 0..2) {
            val dotModifier = 1 + (1..i).sumOf { 1.0 / (2.0.pow(it)) }
            val dottedDuration = duration / dotModifier

            val intValue = (1.0 / dottedDuration).toInt()
            if (intValue.toDouble() == 1.0 / dottedDuration) {
                // found a valid dot
                val note = selectedTuplet.notes.first()
                val newNote = if(note.isRest()) {
                    RhythmRest(
                        baseDuration = duration / dotModifier,
                        dots = i
                    )
                } else {
                    RhythmNote(
                        stemDirection = (note as RhythmNote).stemDirection,
                        baseDuration = duration / dotModifier,
                        dots = i
                    )
                }
                val measure = parsedRhythm.measures[foundMeasureIndex]
                val newElements = measure.elements.toMutableList()
                newElements[foundElementIndex] = newNote
                val newMeasure = Measure(
                    timeSig = measure.timeSig,
                    elements = newElements
                )
                val newRhythm = Rhythm(parsedRhythm.measures.toMutableList().apply {
                    this[foundMeasureIndex] = newMeasure
                })
                parsedRhythm = newRhythm
                rhythm = newRhythm.serialize()
                mainTrack.setRhythm(parsedRhythm)
                return
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun DotInput(maxDots: Int, modifier: Modifier = Modifier) {

        @Composable
        fun DotInputButton(dots: Int, enabled: Boolean, modifier: Modifier = Modifier) {
            val selected = noteInputDots == dots

            val animatedColor = animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
                    else if (enabled) MaterialTheme.colorScheme.surfaceContainerHigh
                    else MaterialTheme.colorScheme.surface,
                animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
                label = "animatedColor"
            )
            val animatedOnColor = animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else if(enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
                label = "animatedOnColor"
            )

            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(animatedColor.value)
                    .clickable(enabled || selected) {
                        noteInputDots = if(noteInputDots != dots) dots else 0

                        val oldElement = parsedRhythm.getNoteAt(selectedNote)
                        val newElement = if(oldElement is RhythmRest) oldElement.copy(dots = noteInputDots)
                            else (oldElement as RhythmNote).copy(dots = noteInputDots)

                        parsedRhythm = parsedRhythm.replaceNote(selectedNote, newElement, isScaled = true)
                        rhythm = parsedRhythm.serialize()
                        mainTrack.setRhythm(parsedRhythm)
                        isSelected = true
                    }
            ) {
                MusicFont.Notation.NoteCentered(MusicFont.Notation.N_QUARTER, dots,
                    modifier = Modifier.align(Alignment.Center),
                    color = animatedOnColor.value,
                    size = 48.dp
                )
            }
        }

        Column(
            modifier = modifier,
        ) {
            DotInputButton(1, maxDots >= 1,
                modifier = Modifier.weight(1f)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            DotInputButton(2, maxDots >= 2,
                modifier = Modifier.weight(1f)
                    .fillMaxWidth()
            )
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun StateInput(modifier: Modifier = Modifier) {

        @Composable
        fun StateInputButton(state: NoteInputState, modifier: Modifier = Modifier) {
            val display = when(state) {
                NoteInputState.UP ->   MusicFont.Notation.N_QUARTER
                NoteInputState.DOWN -> MusicFont.Notation.I_QUARTER
                NoteInputState.REST -> MusicFont.Notation.R_QUARTER
            }
            val selected = noteInputState == state

            val animatedColor = animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
                label = "animatedColor"
            )
            val animatedOnColor = animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
                label = "animatedOnColor"
            )

            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(animatedColor.value)
                    .clickable {
                        noteInputState = if(noteInputState == state) {
                            if(state == NoteInputState.REST) NoteInputState.UP
                                else NoteInputState.REST
                        } else {
                            state
                        }
                        // set selected note to type
                        val oldElement = parsedRhythm.getNoteAt(selectedNote)
                        val newElement = if(noteInputState == NoteInputState.REST) {
                            RhythmRest(
                                baseDuration = oldElement!!.baseDuration,
                                dots = oldElement.dots,
                                tupletRatio = oldElement.tupletRatio
                            )
                        } else {
                            val stemDirection = if(noteInputState == NoteInputState.UP) StemDirection.UP else StemDirection.DOWN
                            if(oldElement is RhythmRest) {
                                RhythmNote(
                                    stemDirection = stemDirection,
                                    baseDuration = oldElement.baseDuration,
                                    dots = oldElement.dots,
                                    tupletRatio = oldElement.tupletRatio
                                )
                            } else {
                                (oldElement as RhythmNote).copy(stemDirection = stemDirection)
                            }
                        }

                        parsedRhythm = parsedRhythm.replaceNote(selectedNote, newElement, isScaled = true)
                        rhythm = parsedRhythm.serialize()
                        mainTrack.setRhythm(parsedRhythm)
                        isSelected = true
                    }
            ) {
                MusicFont.Notation.NoteCentered(display,
                    modifier = Modifier.align(Alignment.Center),
                    color = animatedOnColor.value,
                    size = 32.dp
                )
            }
        }

        Column(
            modifier = modifier,
        ) {
            StateInputButton(NoteInputState.UP,
                modifier = Modifier.weight(1f)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            StateInputButton(NoteInputState.DOWN,
                modifier = Modifier.weight(1f)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            StateInputButton(NoteInputState.REST,
                modifier = Modifier.weight(1f)
                    .fillMaxWidth()
            )
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun ColumnScope.NoteButton(value: Int, enabled: Boolean) {
        val rest = noteInputState == NoteInputState.REST
        val emphasized = noteInputState == NoteInputState.UP
        val selected = noteInputDuration == value && isSelected

        val animatedColor = animateColorAsState(
            targetValue = if(selected) MaterialTheme.colorScheme.primaryContainer
                else if(enabled) MaterialTheme.colorScheme.surfaceContainerHigh
                else MaterialTheme.colorScheme.surfaceContainerLow,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "animatedColor"
        )
        val animatedOnColor = animateColorAsState(
            targetValue = if(selected) MaterialTheme.colorScheme.onPrimaryContainer
                else if(enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "animatedOnColor"
        )

        Box(
            modifier = Modifier.weight(1f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(animatedColor.value)
                .clickable(enabled) {
                    val newNote = if(rest) {
                        RhythmRest(
                            baseDuration = 1.0 / value,
                            dots = noteInputDots
                        )
                    } else {
                        RhythmNote(
                            stemDirection = if (emphasized) StemDirection.UP else StemDirection.DOWN,
                            baseDuration = 1.0 / value,
                            dots = noteInputDots
                        )
                    }
                    parsedRhythm = parsedRhythm.replaceNote(selectedNote, newNote, isScaled = false)
                    rhythm = parsedRhythm.serialize()
                    mainTrack.setRhythm(parsedRhythm)
                    isSelected = true
                    if(parsedRhythm.getNoteAt(selectedNote + 1) != null) {
                        selectedNote += 1
                    }
                }
        ) {
            // show text for large icons
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
                        .background(animatedOnColor.value.copy(alpha = 0.8f))
                        .align(Alignment.Center)
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun TimeSignatureDialog(measureIndex: Int) {
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
                                    timeSignature =
                                        Pair((timeSignature.first - 1).coerceIn(1..32), timeSignature.second)
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
                                    timeSignature =
                                        Pair((timeSignature.first + 1).coerceIn(1..32), timeSignature.second)
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
                                    timeSignature =
                                        Pair(timeSignature.first, (timeSignature.second / 2).coerceIn(1..32))
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
                                    timeSignature =
                                        Pair(timeSignature.first, (timeSignature.second * 2).coerceIn(1..32))
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
                    parsedRhythm = parsedRhythm.setTimeSignature(measureIndex, timeSignature)
                    mainTrack.setRhythm(parsedRhythm)
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

    @Composable
    fun TupletDialog(onDismiss: () -> Unit = {}) {
        var numerator by remember { mutableIntStateOf(3) }
        var tuplet by remember { mutableStateOf(parsedRhythm.createTupletAt(selectedNote, numerator)!!) }

        Dialog(
            onDismissRequest = {
                onDismiss()
                noteInputTuplet = false
            },
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
                        TupletHeader(tuplet,
                            modifier = Modifier.padding(horizontal = 8.dp)
                                .matchParentSize()
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        FilledTonalIconButton(onClick = {
                            if(numerator > 1) {
                                numerator--
                                tuplet = parsedRhythm.createTupletAt(selectedNote, numerator) ?: return@FilledTonalIconButton
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_remove_24),
                                contentDescription = getString(R.string.generic_subtract)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = {
                            onDismiss()
                            parsedRhythm = parsedRhythm.replaceNote(selectedNote, tuplet, isScaled = false)
                            mainTrack.setRhythm(parsedRhythm)
                        }) {
                            Text(getString(R.string.generic_confirm))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        FilledTonalIconButton(onClick = {
                            numerator++
                            tuplet = parsedRhythm.createTupletAt(selectedNote, numerator) ?: return@FilledTonalIconButton
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


    @Composable
    fun EditBpmDialog() {
        var beatValue by remember { mutableFloatStateOf(mainTrack.beatValue) }
        var bpm by remember { mutableIntStateOf(mainTrack.bpm) }
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
                                val string = MusicFont.Notation.getBeatValueString(value)
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
                            note = MusicFont.Notation.getBeatValue(beatValue).first,
                            dots = if(MusicFont.Notation.getBeatValue(beatValue).second) 1 else 0,
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
                            mainTrack.beatValue = beatValue
                            mainTrack.bpm = bpm
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
    fun DrawRhythm(
        rhythm: Rhythm, indexOffset: Int = 0, measureOffset: Int = 0,
        allErrored: Boolean = false, updateBackup: Boolean = false, editable: Boolean = true, complete: Boolean = true
    ) {
        var anyError = false
        var globalIndex = indexOffset
        var previousTimeSig = 0 to 0

        rhythm.measures.forEachIndexed { measureIndex, measure ->
            var measureErrored = false
            if(measure.timeSig.first != 0 && measure.timeSig.second != 0) {
                if ((measure.timeSig.first <= 0 || measure.timeSig.second <= 0) && editable) {
                    val error = getString(
                        R.string.editor_error_invalid_time_signature, measureIndex + measureOffset + 1,
                        "${measure.timeSig.first}/${measure.timeSig.second}"
                    )
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
                        "${measure.timeSig.first}/${measure.timeSig.second}"
                    )
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
                        is RhythmAtom -> it.getDuration()
                        is RhythmTuplet -> it.getDuration()
                    }
                }
                if(measureDuration != measureLength && editable) {
                    val comparison = if(measureLength > measureDuration) ">" else "<"
                    val error = getString(
                        R.string.editor_error_invalid_length,
                        measureIndex + measureOffset + 1,
                        "${measure.timeSig.first}/${measure.timeSig.second}",
                        "$measureLength $comparison $measureDuration"
                    )
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
                    "${measure.timeSig.first}/${measure.timeSig.second}"
                )
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
                            is RhythmAtom -> {
                                var errored = false
                                if (element.getDisplay().contains("?") && editable) {
                                    val errorDisplay = element.getDisplay().replace(MusicFont.Notation.DOT.char, '.')
                                    val error = getString(
                                        R.string.editor_error_invalid_note,
                                        globalIndex,
                                        errorDisplay,
                                        element.baseDuration
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
                                            allErrored = allErrored,
                                            editable = editable,
                                            complete = false
                                        )
                                        globalIndex += element.notes.size
                                    }
                                    TupletHeader(element,
                                        modifier = Modifier.padding(8.dp, 0.dp)
                                            .matchParentSize()
                                    )
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

    @Composable
    fun TupletHeader(tuplet: RhythmTuplet, modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
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

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun NoteText(note: RhythmAtom, noteIndex: Int, errored: Boolean = false, editable: Boolean = true) {
        val isNoteSelected = noteIndex == selectedNote && isSelected
        val isMusicSelected = noteIndex == musicSelected

        val color = animateColorAsState(
            targetValue = if (!editable) Color.Transparent
            else if (isNoteSelected) MaterialTheme.colorScheme.secondaryContainer
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
                .background(if(isMusicSelected) MaterialTheme.colorScheme.tertiaryContainer else color.value)
                .clickable(editable) {
                    if(isNoteSelected) {
                        isSelected = false
                    } else {
                        selectedNote = noteIndex
                        isSelected = true
                    }
                }
        ) {
            val durationChar = MusicFont.Notation.fromLength(note.baseDuration, note.isRest())
            val stemDirection = if(note is RhythmNote) note.stemDirection else StemDirection.UP
            val char = MusicFont.Notation.setEmphasis(
                durationChar ?: MusicFont.Notation.N_QUARTER,
                stemDirection == StemDirection.UP
            )

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
}
