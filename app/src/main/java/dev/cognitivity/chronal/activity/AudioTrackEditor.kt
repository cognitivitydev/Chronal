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

package dev.cognitivity.chronal.activity

import android.content.Intent
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.metronome.Metronome
import dev.cognitivity.chronal.metronome.tracks.AudioTrack
import dev.cognitivity.chronal.metronome.tracks.ClickTrack
import dev.cognitivity.chronal.settings.Setting
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.metronome.MetronomeConfigAudioTrack
import dev.cognitivity.chronal.toPx
import dev.cognitivity.chronal.ui.metronome.components.TrackSettingsDropdown
import dev.cognitivity.chronal.ui.metronome.components.TrackSettingsPage
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.abs

class AudioTrackEditor : BaseActivity() {
    private var tempoError by mutableStateOf(false)

    private var trackIndex by mutableIntStateOf(0)
    private var uri by mutableStateOf(Uri.EMPTY)
    private var fileName by mutableStateOf("")

    private lateinit var metronome: Metronome
    private var appMetronome by mutableStateOf(ChronalApp.getInstance().metronome)
    private lateinit var mainTrack: AudioTrack
    private lateinit var initialTrack: MetronomeConfigAudioTrack

    val samplesPerSecond = 64
    var amplitudes by mutableStateOf(FloatArray(0))
    var amplitudeProgress by mutableFloatStateOf(0f)
    var audioDuration by mutableFloatStateOf(0f)

    private val pickAudio = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult

        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        var fileName = uri.lastPathSegment.toString()

        contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            if(cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(index != -1) {
                    fileName = cursor.getString(index)
                }
            }
        }

        this.uri = uri
        this.fileName = fileName
        mainTrack.uri = uri
        mainTrack.fileName = fileName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if(!intent.hasExtra("trackIndex")) {
            finish()
            return
        }

        metronome = Metronome(
            context = this,
            sendNotifications = false,
            tracks = mutableListOf(
                AudioTrack(
                    uri = Uri.EMPTY,
                    fileName = "Unknown",
                    bpm = null
                )
            )
        )

        trackIndex = intent.getIntExtra("trackIndex", 0)
        val track = Settings.getTrack(trackIndex)
        if(track == null) {
            Toast.makeText(this, "Failed to find track at index $trackIndex", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        if(track !is MetronomeConfigAudioTrack) {
            Toast.makeText(this, "Track at index $trackIndex is not an audio track", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        this.uri = track.uri.toUri()
        this.fileName = track.fileName

        mainTrack = appMetronome.tracks[trackIndex] as AudioTrack

        appMetronome = ChronalApp.getInstance().metronome
        metronome.bpm = appMetronome.bpm

        mainTrack.uri = uri
        mainTrack.fileName = fileName
        metronome.tracks[0] = mainTrack
        initialTrack = MetronomeConfigAudioTrack.fromTrack(mainTrack)

        setContent {
            MetronomeTheme {
                MainContent()
            }
        }
    }

    private fun saveAndExit() {
        appMetronome.bpm = metronome.bpm
        Settings.METRONOME_CONFIG.set(
            Settings.METRONOME_CONFIG.get().copy(bpm = appMetronome.bpm)
        )

        Settings.setTrack(trackIndex) {
            (it as MetronomeConfigAudioTrack).copy(
                name = mainTrack.name,
                enabled = mainTrack.enabled,
                color = mainTrack.color,
                uri = mainTrack.uri.toString(),
                fileName = mainTrack.fileName,
                startTrim = mainTrack.startTrim,
                endTrim = mainTrack.endTrim,
                volume = mainTrack.volume,
                loop = mainTrack.loop,
                bpm = mainTrack.bpm
            )
        }
        lifecycleScope.launch {
            Setting.saveAll()
            finish()
        }
    }

    private fun exitWithoutSaving() {
        finish()
    }

    @Composable
    private fun MainContent() {
        val rootNavController = rememberNavController()

        NavHost(
            navController = rootNavController,
            startDestination = "editor",
        ) {
            composable("editor") {
                EditorPage(
                    onEditTrack = {
                        rootNavController.navigate("track_settings")
                    }
                )
            }
            composable("track_settings",
                enterTransition = { scaleIn() + fadeIn() },
                exitTransition = { scaleOut() + fadeOut() }
            ) {
                val metronome = ChronalApp.getInstance().metronome
                val configTrack = MetronomeConfigAudioTrack.fromTrack(mainTrack)
                TrackSettingsPage(
                    track = configTrack,
                    onBack = {
                        rootNavController.popBackStack()
                    },
                    onTrackChange = { updated ->
                        mainTrack.name = updated.name
                        mainTrack.enabled = updated.enabled
                        mainTrack.color = updated.color
                    },
                    canDelete = metronome.tracks.count { it != mainTrack && it.enabled } != 0,
                    onDelete = {
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    private fun EditorPage(onEditTrack: () -> Unit) {
        var speedAdjust by remember { mutableStateOf(mainTrack.bpm != null) }
        var songTempo by remember { mutableFloatStateOf(mainTrack.bpm ?: 120f) }
        var volume by remember { mutableFloatStateOf(mainTrack.volume) }
        var loop by remember { mutableStateOf(mainTrack.loop) }
        var startTrim by remember { mutableFloatStateOf(mainTrack.startTrim) }
        var endTrim by remember { mutableFloatStateOf(mainTrack.endTrim) }

        LaunchedEffect(uri) {
            if(!mainTrack.exists(this@AudioTrackEditor)) return@LaunchedEffect
            withContext(Dispatchers.IO) {
                extractAmplitudes(uri, samplesPerSecond)
            }
        }


        Scaffold(
            topBar = { TopBar(onEditTrack = onEditTrack) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    AudioFilePicker()
                }
                item {
                    SpeedAdjustOption(speedAdjust) {
                        speedAdjust = it
                        mainTrack.bpm = if(speedAdjust) songTempo else null
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = speedAdjust,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        SongTempoOption(songTempo) {
                            songTempo = it
                            mainTrack.bpm = it
                        }
                    }
                }
                item {
                    VolumeOption(volume) {
                        volume = it
                        mainTrack.volume = it
                    }
                }
                item {
                    LoopOption(loop) {
                        loop = it
                        mainTrack.loop = it
                    }
                }
                item {
                    Text(
                        text = stringResource(R.string.audio_track_editor_trim),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                    )
                }
                item {
                    TrimEditor(
                        value = startTrim,
                        onValueChange = {
                            startTrim = it
                            mainTrack.startTrim = it
                        },
                        label = stringResource(R.string.audio_track_editor_trim_start),
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_text_select_jump_to_end_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(end = 12.dp)
                                    .size(24.dp)
                            )
                        }
                    )
                }

                item {
                    TrimEditor(
                        value = if (endTrim == 0f && audioDuration > 0f) {
                            audioDuration
                        } else {
                            audioDuration - endTrim
                        },
                        onValueChange = { absoluteTrimTime ->
                            val newEndTrim = audioDuration - absoluteTrimTime
                            endTrim = newEndTrim
                            mainTrack.endTrim = newEndTrim
                        },
                        label = stringResource(R.string.audio_track_editor_trim_end),
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_text_select_jump_to_beginning_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(24.dp)
                            )
                        }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TopBar(onEditTrack: () -> Unit) {
        var backDropdown by remember { mutableStateOf(false) }
        var settingsDropdown by remember { mutableStateOf(false) }

        TopAppBar(
            title = { Text(mainTrack.name) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            navigationIcon = {
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
                                    contentDescription = stringResource(R.string.generic_save_exit)
                                )
                            },
                            text = { Text(stringResource(R.string.generic_save_exit)) },
                            onClick = {
                                backDropdown = false
                                saveAndExit()
                            },
                            enabled = !tempoError
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.generic_exit_discard)
                                )
                            },
                            text = { Text(stringResource(R.string.generic_exit_discard)) },
                            onClick = {
                                backDropdown = false
                                exitWithoutSaving()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = stringResource(R.string.generic_cancel)
                                )
                            },
                            text = { Text(stringResource(R.string.generic_cancel)) },
                            onClick = {
                                backDropdown = false
                            }
                        )
                    }
                }
                IconButton(
                    onClick = {
                        if(initialTrack == MetronomeConfigAudioTrack.fromTrack(mainTrack)) {
                            finish()
                            return@IconButton
                        }
                        backDropdown = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.generic_back)
                    )
                }
            },
            actions = {
                IconButton(onClick = { settingsDropdown = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.editor_settings)
                    )
                }
                val metronome = ChronalApp.getInstance().metronome
                TrackSettingsDropdown(
                    track = mainTrack,
                    expanded = settingsDropdown,
                    canDelete = metronome.tracks.count { it != mainTrack && it.enabled } != 0,
                    onDismissRequest = { settingsDropdown = false },
                    onEdit = onEditTrack,
                    onDeleteFinish = {
                        finish()
                    },
                    onSwitchEditor = {}
                )
            }
        )
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun AudioFilePicker() {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                Icon(
                    painter = painterResource(
                        if(uri != Uri.EMPTY) R.drawable.outline_audio_file_24
                            else R.drawable.outline_attach_file_off_24
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if(uri == Uri.EMPTY) stringResource(R.string.audio_track_editor_no_file) else fileName,
                    style = MaterialTheme.typography.bodyLargeEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
            Button(
                onClick = {
                    pickAudio.launch(arrayOf("audio/*"))
                }
            ) {
                Text(
                    text = stringResource(R.string.audio_track_editor_file_select)
                )
            }
        }
    }

    @Composable
    private fun SettingContainer(
        iconContent: @Composable () -> Unit,
        title: String,
        description: String?,
        topRounded: Boolean,
        bottomRounded: Boolean,
        content: @Composable RowScope.() -> Unit,
        modifier: Modifier = Modifier
    ) {
        val shape = RoundedCornerShape(
            topStart = if(topRounded) 16.dp else 4.dp,
            topEnd = if(topRounded) 16.dp else 4.dp,
            bottomStart = if(bottomRounded) 16.dp else 4.dp,
            bottomEnd = if(bottomRounded) 16.dp else 4.dp
        )
        Row(
            modifier = modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                .fillMaxWidth()
                .heightIn(min = 80.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            iconContent()
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if(description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }

    @Composable
    private fun SpeedAdjustOption(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        SettingContainer(
            iconContent = {
                Icon(
                    painter = painterResource(R.drawable.outline_sync_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 12.dp)
                        .size(24.dp)
                )
            },
            title = stringResource(R.string.audio_track_editor_speed_adjust),
            description = stringResource(R.string.audio_track_editor_speed_adjust_description),
            topRounded = true, bottomRounded = false,
            content = {
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        onCheckedChange(it)
                    },
                    interactionSource = interactionSource
                )
            },
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onCheckedChange(!checked)
            }
        )
    }

    @Composable
    private fun SongTempoOption(value: Float, onValueChange: (Float) -> Unit) {
        var songTempoInput by remember { mutableStateOf(value.toString()) }
        SettingContainer(
            iconContent = {
                Icon(
                    painter = painterResource(R.drawable.baseline_music_note_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 12.dp)
                        .size(24.dp)
                )
            },
            title = stringResource(R.string.audio_track_editor_tempo),
            description = null,
            topRounded = false, bottomRounded = false,
            content = {
                OutlinedTextField(
                    value = songTempoInput,
                    onValueChange = {
                        songTempoInput = it

                        val newValue = songTempoInput.toFloatOrNull()
                        if(newValue != null && newValue >= ClickTrack.MIN_BPM && newValue <= ClickTrack.MAX_BPM) {
                            onValueChange(newValue)
                            mainTrack.bpm = newValue
                            tempoError = false
                        } else {
                            tempoError = true
                        }
                    },
                    isError = tempoError,
                    modifier = Modifier.padding(start = 16.dp)
                        .width(96.dp),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        )
    }

    @Composable
    private fun VolumeOption(value: Float, onValueChange: (Float) -> Unit) {
        SettingContainer(
            iconContent = {
                Icon(
                    painter = painterResource(R.drawable.baseline_volume_up_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 12.dp)
                        .size(24.dp)
                )
            },
            title = stringResource(R.string.audio_track_editor_volume),
            description = stringResource(R.string.audio_track_editor_volume_value, (value * 100).toInt()),
            topRounded = false, bottomRounded = false,
            content = {
                Slider(
                    value = value,
                    onValueChange = {
                        onValueChange(it)
                    },
                    modifier = Modifier.padding(start = 16.dp)
                        .weight(2f),
                )
            }
        )
    }

    @Composable
    private fun LoopOption(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        SettingContainer(
            iconContent = {
                Icon(
                    painter = painterResource(R.drawable.outline_replay_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 12.dp)
                        .size(24.dp)
                )
            },
            title = stringResource(R.string.audio_track_editor_loop),
            description = null,
            topRounded = false, bottomRounded = true,
            content = {
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        onCheckedChange(it)
                    },
                    interactionSource = interactionSource
                )
            },
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onCheckedChange(!checked)
            }
        )
    }

    private fun formatTime(value: Float): String {
        val totalMillis = (abs(value) * 1000).toInt()

        val minutes = totalMillis / 60000
        val seconds = (totalMillis / 1000) % 60
        val milliseconds = totalMillis % 1000

        return String.format(Locale.US, "%s%02d:%02d.%03d",
            if (value < 0) "-" else "", minutes, seconds, milliseconds
        )
    }

    @Composable
    private fun TrimEditor(
        value: Float,
        onValueChange: (Float) -> Unit,
        label: String,
        icon: @Composable () -> Unit,
    ) {
        val visibleDuration = 1f
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                WaveformVisualizer(
                    modifier = Modifier.fillMaxWidth()
                        .height(80.dp),
                    trimTimestamp = value,
                    onTrimTimestampChanged = {
                        onValueChange(it)
                    },
                    visibleDuration = visibleDuration
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatTime(value - visibleDuration / 2f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatTime(value),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = formatTime(value + visibleDuration / 2f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun WaveformVisualizer(
        trimTimestamp: Float,
        onTrimTimestampChanged: (Float) -> Unit,
        visibleDuration: Float,
        modifier: Modifier = Modifier
    ) {
        val barColor = MaterialTheme.colorScheme.onSurface
        val silentColor = MaterialTheme.colorScheme.outlineVariant
        val trimColor = MaterialTheme.colorScheme.primary

        val currentTrimTimestamp by rememberUpdatedState(trimTimestamp)
        val startTime = currentTrimTimestamp - (visibleDuration / 2f)
        val endTime = currentTrimTimestamp + (visibleDuration / 2f)

        BoxWithConstraints(modifier = modifier) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val centerX = widthPx / 2f

            val barWidthPx = 3.dp.toPx()
            val barGapPx = 2.dp.toPx()
            val totalBarWidthPx = barWidthPx + barGapPx

            val pxPerSecond = widthPx / visibleDuration
            val secondsPerBar = 1f / samplesPerSecond

            val isIncomplete = amplitudes.isEmpty() || (amplitudeProgress < endTime && amplitudeProgress < audioDuration)
            if(isIncomplete) {
                LinearWavyProgressIndicator(
                    progress = { amplitudeProgress / endTime },
                    wavelength = 40.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .align(Alignment.Center)
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaSeconds = -dragAmount / pxPerSecond
                                onTrimTimestampChanged(currentTrimTimestamp + deltaSeconds)
                            }
                        }
                ) {
                    val startSampleIndex = (startTime.coerceAtLeast(0f) * samplesPerSecond).toInt()
                    val endSampleIndex = (endTime * samplesPerSecond).toInt().coerceAtMost(amplitudes.size - 1)

                    if(startSampleIndex <= endSampleIndex) {
                        for(i in startSampleIndex..endSampleIndex) {
                            val sampleTime = i * secondsPerBar
                            val amplitude = amplitudes[i]

                            val x = centerX + ((sampleTime - currentTrimTimestamp) * pxPerSecond)
                            if(x < -totalBarWidthPx || x > widthPx + totalBarWidthPx) continue

                            val barHeight = (heightPx * amplitude).coerceAtLeast(4.dp.toPx())
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, heightPx / 2f - barHeight / 2f),
                                size = Size(barWidthPx, barHeight),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }

                    val startX = centerX + ((0f - currentTrimTimestamp) * pxPerSecond)
                    if(startX > 0f) {
                        drawLine(
                            color = silentColor,
                            start = Offset(x = 0f, y = heightPx / 2f),
                            end = Offset(x = startX.coerceAtMost(widthPx), y = heightPx / 2f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    val endX = centerX + ((audioDuration - currentTrimTimestamp) * pxPerSecond)
                    if(endX < widthPx) {
                        drawLine(
                            color = silentColor,
                            start = Offset(x = endX.coerceAtLeast(0f), y = heightPx / 2f),
                            end = Offset(x = widthPx, y = heightPx / 2f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    drawLine(
                        color = trimColor,
                        start = Offset(x = centerX, y = 0f),
                        end = Offset(x = centerX, y = heightPx),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }
    }

    fun extractAmplitudes(
        uri: Uri,
        samplesPerSecond: Int
    ) {
        val extractor = MediaExtractor()
        extractor.setDataSource(this@AudioTrackEditor, uri, null)

        var trackIndex = -1
        var trackFormat: MediaFormat? = null
        for(i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if(mime.startsWith("audio/")) {
                trackIndex = i
                trackFormat = format
                break
            }
        }
        if(trackIndex == -1 || trackFormat == null) {
            extractor.release()
            return
        }
        extractor.selectTrack(trackIndex)

        val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: return
        val decoder = MediaCodec.createDecoderByType(mime)

        decoder.configure(trackFormat, null, null, 0)
        decoder.start()

        val durationUs = trackFormat.getLong(MediaFormat.KEY_DURATION)
        audioDuration = durationUs / 1_000_000f

        val sampleRate = trackFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = trackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val totalPcmSamplesPerSec = sampleRate * channelCount

        val pcmSamplesPerBucket = (totalPcmSamplesPerSec / samplesPerSecond).coerceAtLeast(1)
        val targetTotalSamples = (audioDuration * samplesPerSecond).toInt().coerceAtLeast(1)

        amplitudes = FloatArray(targetTotalSamples)


        val bufferInfo = MediaCodec.BufferInfo()
        var isEOS = false
        var globalSampleCount = 0L

        while(!isEOS) {
            val inputIndex = decoder.dequeueInputBuffer(10000)
            if(inputIndex >= 0) {
                val inputBuffer = decoder.getInputBuffer(inputIndex) ?: continue
                val sampleSize = extractor.readSampleData(inputBuffer, 0)
                if(sampleSize < 0) {
                    decoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    isEOS = true
                } else {
                    decoder.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                    extractor.advance()
                }
            }

            val outputIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
            if(outputIndex >= 0) {
                val outputBuffer = decoder.getOutputBuffer(outputIndex)
                if(outputBuffer != null && bufferInfo.size > 0) {
                    outputBuffer.position(bufferInfo.offset)
                    val shortBuffer = outputBuffer.asShortBuffer()

                    val step = pcmSamplesPerBucket / 16
                    while(shortBuffer.hasRemaining()) {
                        val sample = shortBuffer.get()
                        val normalized = abs(sample.toFloat()) / Short.MAX_VALUE

                        val bucketIndex = (globalSampleCount / pcmSamplesPerBucket)
                            .toInt()
                            .coerceIn(0, targetTotalSamples - 1)

                        if(normalized > amplitudes[bucketIndex]) {
                            amplitudes[bucketIndex] = normalized
                        }

                        globalSampleCount += step

                        val newPos = (shortBuffer.position() + step - 1).coerceAtMost(shortBuffer.limit())
                        shortBuffer.position(newPos)

                        amplitudeProgress = (globalSampleCount / totalPcmSamplesPerSec.toFloat()).coerceAtMost(audioDuration)
                    }
                }
                decoder.releaseOutputBuffer(outputIndex, false)
            }
        }

        decoder.stop()
        decoder.release()
        extractor.release()

        amplitudeProgress = audioDuration
    }
}