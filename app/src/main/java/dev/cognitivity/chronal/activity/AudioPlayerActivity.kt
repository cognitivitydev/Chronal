package dev.cognitivity.chronal.activity

import android.media.MediaPlayer
import android.media.SyncParams
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.rhythm.player.PlayerRhythm
import dev.cognitivity.chronal.rhythm.player.elements.Pause
import dev.cognitivity.chronal.rhythm.player.elements.PlayerRhythmElement
import dev.cognitivity.chronal.rhythm.player.elements.SetTempo
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.math.ceil


class AudioPlayerActivity : ComponentActivity() {
    var rhythm by mutableStateOf(PlayerRhythm(listOf()))
    var updatingRhythm by mutableStateOf(false)
    var mediaUri: Uri = Uri.EMPTY
    var metronomeUri: Uri = Uri.EMPTY

    var playing by mutableStateOf(false)
    var audioLength by mutableLongStateOf(25200L)
    var progress by mutableLongStateOf(0L)

    var mediaVolume by mutableFloatStateOf(0.5f)
    var metronomeVolume by mutableFloatStateOf(0.5f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val uri = intent.getStringExtra("file")?.toUri()
        if (uri == null) {
            finish()
            return
        }
        mediaUri = uri

        updateRhythm()

        setContent {
            MetronomeTheme {
                MainContent()
            }
        }
    }

    fun updateRhythm() {
        playing = false

        val tempFile = File.createTempFile("temp_metronome", ".wav", cacheDir)
        rhythm.toWav(tempFile)

        metronomeUri = Uri.fromFile(tempFile)
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun MainContent() {
        var fabExpanded by remember { mutableStateOf(false) }
        var showBpmDialog by remember { mutableStateOf(false) }
        var showPauseDialog by remember { mutableStateOf(false) }

        val mediaPlayer = remember {
            MediaPlayer.create(this, mediaUri).apply {
                setVolume(mediaVolume, mediaVolume)
                setOnCompletionListener {
                    playing = false
                }
                syncParams.syncSource = SyncParams.SYNC_SOURCE_SYSTEM_CLOCK
                syncParams.audioAdjustMode = SyncParams.AUDIO_ADJUST_MODE_STRETCH
            }
        }
        audioLength = mediaPlayer.duration.toLong()

        val metronomePlayer = remember {
            MediaPlayer.create(this, metronomeUri).apply {
                setVolume(metronomeVolume, metronomeVolume)
                syncParams.syncSource = SyncParams.SYNC_SOURCE_SYSTEM_CLOCK
                syncParams.audioAdjustMode = SyncParams.AUDIO_ADJUST_MODE_STRETCH
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                mediaPlayer.release()
                metronomePlayer.release()
            }
        }

        LaunchedEffect(rhythm) {
            if (updatingRhythm) return@LaunchedEffect
            updatingRhythm = true

            CoroutineScope(Dispatchers.IO).launch {
                updateRhythm()

                mediaPlayer.reset()
                mediaPlayer.setDataSource(this@AudioPlayerActivity, mediaUri)
                mediaPlayer.prepare()
                mediaPlayer.seekTo(progress.toInt())
                audioLength = mediaPlayer.duration.toLong()

                metronomePlayer.reset()
                metronomePlayer.setDataSource(this@AudioPlayerActivity, metronomeUri)
                metronomePlayer.prepare()
                metronomePlayer.seekTo(progress.toInt())

                updatingRhythm = false
            }
        }

        LaunchedEffect(playing) {
            if (playing) {
                if (!mediaPlayer.isPlaying) {
                    CoroutineScope(coroutineContext).launch {
                        mediaPlayer.seekTo(progress.toInt())
                        mediaPlayer.start()
                    }
                    CoroutineScope(coroutineContext).launch {
                        metronomePlayer.seekTo(progress.toInt())
                        metronomePlayer.start()
                    }
                }
                val startProgress = progress
                val start = System.currentTimeMillis()
                while (playing) {
                    val elapsed = System.currentTimeMillis() - start
                    progress = (startProgress + elapsed).coerceAtMost(audioLength)
                    if (progress >= audioLength) {
                        playing = false
                    }
                    delay(10L)
                }
                playing = false
            } else {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    metronomePlayer.pause()
                    mediaPlayer.seekTo(progress.toInt())
                    metronomePlayer.seekTo(progress.toInt())
                }
            }
        }

        LaunchedEffect(mediaVolume) {
            mediaPlayer.setVolume(mediaVolume, mediaVolume)
        }
        LaunchedEffect(metronomeVolume) {
            metronomePlayer.setVolume(metronomeVolume, metronomeVolume)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(getString(R.string.audio_player_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                finish()
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = getString(R.string.generic_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            },
            floatingActionButton = {
                if((rhythm.elements.lastOrNull()?.endTime ?: 0L) >= audioLength) return@Scaffold
                FloatingActionButtonMenu(
                    expanded = fabExpanded,
                    button = {
                        ToggleFloatingActionButton(
                            checked = fabExpanded,
                            onCheckedChange = { fabExpanded = !fabExpanded },
                        ) {
                            val imageVector by remember {
                                derivedStateOf {
                                    if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                                }
                            }
                            Icon(
                                painter = rememberVectorPainter(imageVector),
                                contentDescription = if(fabExpanded) getString(R.string.generic_close) else getString(R.string.generic_add),
                                modifier = Modifier.animateIcon({ checkedProgress }),
                            )
                        }
                    }
                ) {
                    FloatingActionButtonMenuItem(
                        text = { Text(getString(R.string.audio_player_add_tempo)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_music_note_24),
                                contentDescription = getString(R.string.audio_player_add_tempo)
                            )
                        },
                        onClick = {
                            showBpmDialog = true
                            fabExpanded = false
                        }
                    )
                    FloatingActionButtonMenuItem(
                        text = { Text(getString(R.string.audio_player_add_pause)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_music_off_24),
                                contentDescription = getString(R.string.audio_player_add_pause)
                            )
                        },
                        onClick = {
                            showPauseDialog = true
                            fabExpanded = false
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(
                        PaddingValues(
                            start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                            top = innerPadding.calculateTopPadding(),
                            end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                            bottom = 0.dp,
                        )
                    ),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp)
                ) {
                    AudioControls()
                }
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    RhythmList()
                }
            }

            if(showBpmDialog) {
                ElementDialog(
                    index = rhythm.elements.size,
                    isTempo = true,
                    onDismiss = { showBpmDialog = false },
                    onConfirm = { new ->
                        showBpmDialog = false
                        val newRhythm = rhythm.copy(
                            elements = rhythm.elements.toMutableList().apply {
                                add(new)
                            }
                        )
                        rhythm = newRhythm
                    }
                )
            }

            if(showPauseDialog) {
                ElementDialog(
                    index = rhythm.elements.size,
                    isTempo = false,
                    onDismiss = { showPauseDialog = false },
                    onConfirm = { new ->
                        showPauseDialog = false
                        val newRhythm = rhythm.copy(
                            elements = rhythm.elements.toMutableList().apply {
                                add(new)
                            }
                        )
                        rhythm = newRhythm
                    }
                )
            }

            if(updatingRhythm) {
                Dialog(
                    onDismissRequest = { },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ContainedLoadingIndicator()
                        Text(getString(R.string.audio_player_updating),
                            modifier = Modifier.padding(top = 96.dp)
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun AudioControls() {
        var showMore by remember { mutableStateOf(false) }
        var showVolume by remember { mutableStateOf(false) }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Timer(time = progress)
                Spacer(Modifier.weight(1f))
                Timer(time = audioLength)
            }
            Slider(
                value = progress.toFloat() / audioLength.toFloat(),
                onValueChange = {
                    playing = false
                    progress = (it * audioLength).toLong()
                },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                ToggleButton(
                    checked = showVolume,
                    onCheckedChange = {
                        showVolume = it
                    },
                    shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                    modifier = Modifier.minimumInteractiveComponentSize()
                        .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Narrow)),
                    contentPadding = PaddingValues(0.dp), // its messed up otherwise, idk
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_volume_up_24),
                        contentDescription = getString(R.string.audio_player_volume_controls),
                        modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
                    )
                }

                FilledIconButton(
                    onClick = {
                        playing = false
                        progress = 0L
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                        .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Narrow)),
                    shape = IconButtonDefaults.mediumRoundShape,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_skip_previous_24),
                        contentDescription = getString(R.string.audio_player_skip_start),
                        modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
                    )
                }

                FilledIconButton(
                    onClick = {
                        playing = false
                        progress = (progress - 5000L).coerceAtLeast(0L)
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                        .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Uniform)),
                    shape = IconButtonDefaults.mediumRoundShape,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_replay_5_24),
                        contentDescription = getString(R.string.audio_player_back_5),
                        modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
                    )
                }

                ToggleButton(
                    checked = playing,
                    onCheckedChange = {
                        if(progress >= audioLength) {
                            playing = false
                            progress = 0L
                        }
                        playing = it
                    },
                    shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                    modifier = Modifier.minimumInteractiveComponentSize()
                        .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide)),
                    contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                ) {
                    Icon(
                        painter = painterResource(if (playing) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24),
                        contentDescription = getString(if(playing) R.string.generic_pause else R.string.generic_play),
                        modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
                    )
                }

                FilledIconButton(
                    onClick = {
                        progress = (progress + 5000L).coerceAtMost(audioLength)
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                        .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Uniform)),
                    shape = IconButtonDefaults.mediumRoundShape,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_forward_5_24),
                        contentDescription = getString(R.string.audio_player_forward_5),
                        modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
                    )
                }

                FilledIconButton(
                    onClick = {
                        playing = false
                        progress = audioLength
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                        .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Narrow)),
                    shape = IconButtonDefaults.mediumRoundShape,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_skip_next_24),
                        contentDescription = getString(R.string.audio_player_skip_end),
                        modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
                    )
                }

                FilledIconButton(
                    onClick = {
                        showMore = !showMore
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                        .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Narrow)),
                    shape = IconButtonDefaults.mediumRoundShape,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = getString(R.string.generic_more_options),
                        modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
                    )
                    //dropdown
                    DropdownMenu(
                        expanded = showMore,
                        onDismissRequest = { showMore = false },
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.outline_volume_up_24),
                                    contentDescription = getString(R.string.audio_player_volume_controls)
                                )
                            },
                            text = { Text(getString(R.string.audio_player_volume_controls)) },
                            onClick = {
                                showVolume = !showVolume
                                showMore = false
                            }
                        )
                    }
                }
            }

            if(showVolume) {
                Text(
                    text = getString(R.string.audio_player_media_volume),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Slider(
                    value = mediaVolume,
                    onValueChange = {
                        mediaVolume = it
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = getString(R.string.audio_player_metronome_volume),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Slider(
                    value = metronomeVolume,
                    onValueChange = {
                        metronomeVolume = it
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    @Composable
    fun Timer(time: Long) {
        val hours = time / 3600000
        val minutes = (time % 3600000) / 60000
        val seconds = (time % 60000) / 1000
        val ms = (time % 1000) / 10
        Text(
            text = if(hours > 0) {
                String.format(Locale.US, "%02d:%02d:%02d.%02d", hours, minutes, seconds, ms)
            } else {
                String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, ms)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun RhythmList() {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            items(rhythm.elements.size) { index ->
                var edit by remember { mutableStateOf(false) }

                val element = rhythm.elements[index]

                val color = when (element) {
                    is SetTempo -> MaterialTheme.colorScheme.primary
                    is Pause -> MaterialTheme.colorScheme.secondary
                }
                val onColor = when (element) {
                    is SetTempo -> MaterialTheme.colorScheme.onPrimary
                    is Pause -> MaterialTheme.colorScheme.onSecondary
                }
                val colorContainer = when (element) {
                    is SetTempo -> MaterialTheme.colorScheme.primaryContainer
                    is Pause -> MaterialTheme.colorScheme.secondaryContainer
                }
                val onColorContainer = when (element) {
                    is SetTempo -> MaterialTheme.colorScheme.onPrimaryContainer
                    is Pause -> MaterialTheme.colorScheme.onSecondaryContainer
                }

                val text = getString(
                    when (element) {
                        is SetTempo -> R.string.audio_player_set_bpm
                        is Pause -> R.string.audio_player_pause
                    }
                )
                val altText = when (element) {
                    is SetTempo -> getString(R.string.audio_player_bpm_alt, element.tempo)
                    is Pause -> getString(R.string.audio_player_seconds_alt, ((element.endTime - element.startTime) / 1000f))
                }

                val isNow = element.startTime <= progress && element.endTime >= progress
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isNow) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow)
                        .clickable {
                            edit = true
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Timer(time = element.startTime)
                        if(isNow) {
                            val text = if(element.beats != null) {
                                val tempo = if (element is SetTempo) element.tempo else (rhythm.elements.getOrNull(index - 1) as? SetTempo)?.tempo ?: 1
                                val currentBeats = ceil((progress - element.startTime) / (60.0 / tempo * 1000)).toInt()
                                getString(R.string.audio_player_length_beats, currentBeats)
                            } else {
                                getString(R.string.audio_player_length_seconds, (progress - element.startTime) / 1000f)
                            }
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = color
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.clip(CircleShape)
                                    .background(colorContainer)
                            ) {
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = onColorContainer,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        .align(Alignment.CenterVertically)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier.defaultMinSize(minWidth = 96.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                ) {
                                    Text(
                                        text = altText,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = onColor,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(0.8f)
                                    .align(Alignment.CenterHorizontally)
                                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                                    .background(onColor)
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (element.beats != null) {
                                    Text(
                                        text = getString(R.string.audio_player_length_beats, element.beats),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = color
                                    )
                                }
                                Text(
                                    text = getString(R.string.audio_player_length_seconds,
                                        ((element.endTime - element.startTime) / 1000f)),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = color
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                }

                if (edit) {
                    ElementDialog(
                        index = index,
                        isTempo = element is SetTempo,
                        onDismiss = { edit = false },
                        onConfirm = { new ->
                            edit = false
                            val newElements = rhythm.elements.toMutableList()
                            newElements[index] = new
                            for (i in (index + 1) until newElements.size) {
                                val prev = newElements[i - 1]
                                if(newElements[i] is SetTempo) {
                                    val tempo = newElements[i] as SetTempo
                                    newElements[i] = SetTempo(
                                        startTime = prev.endTime,
                                        endTime = minOf(tempo.endTime + tempo.endTime - tempo.startTime, tempo.endTime),
                                        beats = tempo.beats ?: 0,
                                        tempo = tempo.tempo
                                    )
                                } else if(newElements[i] is Pause) {
                                    val pause = newElements[i] as Pause
                                    newElements[i] = Pause(
                                        startTime = prev.endTime,
                                        endTime = minOf(pause.endTime + pause.endTime - pause.startTime, pause.endTime),
                                        beats = pause.beats ?: 0
                                    )
                                }
                            }
                            rhythm = rhythm.copy(elements = newElements)
                        }
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                            .height(1.dp)
                            .padding(horizontal = 16.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Timer(time = rhythm.elements.lastOrNull()?.endTime ?: 0)
                    Box(
                        modifier = Modifier.weight(1f)
                            .height(1.dp)
                            .padding(horizontal = 16.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun ElementDialog(index: Int, isTempo: Boolean, onDismiss: () -> Unit, onConfirm: (PlayerRhythmElement) -> Unit) {
        val element = rhythm.elements.getOrNull(index)

        var useTime by remember { mutableStateOf(element?.beats == null) }
        val last = if(index == 0) null else rhythm.elements[index - 1]
        val lastBpm = if(last is SetTempo) last.tempo else null

        var bpm by remember { mutableIntStateOf(if(element is SetTempo) element.tempo else lastBpm ?: 0) }
        var bpmText by remember { mutableStateOf(if(bpm == 0) "" else bpm.toString()) }

        var length by remember { mutableFloatStateOf(if(element != null) (element.endTime - element.startTime) / 1000f else 0f) }
        var lengthText by remember { mutableStateOf(if(length == 0f) "" else length.toString()) }

        var beats by remember { mutableIntStateOf(element?.beats ?: -1) }
        var beatsText by remember { mutableStateOf(beats.toString()) }

        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(getString(if(element != null) R.string.audio_player_edit_element
                else if(isTempo) R.string.audio_player_add_tempo else R.string.audio_player_add_pause)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if(isTempo) {
                        OutlinedTextField(
                            label = { Text(getString(R.string.audio_player_element_tempo)) },
                            value = bpmText,
                            onValueChange = {
                                bpmText = it.filter { char -> char.isDigit() }
                                bpm = bpmText.toIntOrNull() ?: 0
                            },
                            isError = (bpm <= 0 || bpm >= 500) && bpmText != "",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = getString(R.string.audio_player_element_duration_type),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    if(lastBpm == null && !isTempo) {
                        Text(
                            text = getString(R.string.audio_player_element_beats_disabled),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Row(
                        modifier = Modifier.padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                    ) {
                        ToggleButton(
                            checked = useTime,
                            onCheckedChange = {
                                useTime = true
                                length = 0f
                                beats = -1
                                lengthText = ""
                                beatsText = ""
                            },
                            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            if (useTime) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = ChronalApp.context.getString(R.string.generic_selected),
                                )
                                Spacer(modifier = Modifier.width(ToggleButtonDefaults.IconSpacing))
                            }
                            Text(getString(R.string.audio_player_element_duration_time))
                        }
                        ToggleButton(
                            checked = !useTime,
                            onCheckedChange = {
                                useTime = false
                                length = 0f
                                beats = -1
                                lengthText = ""
                                beatsText = ""
                            },
                            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                            contentPadding = ButtonDefaults.ContentPadding,
                            enabled = lastBpm != null || isTempo
                        ) {
                            if (!useTime) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = ChronalApp.context.getString(R.string.generic_selected),
                                )
                                Spacer(modifier = Modifier.width(ToggleButtonDefaults.IconSpacing))
                            }
                            Text(getString(R.string.audio_player_element_duration_beats))
                        }
                    }

                    if(useTime) {
                        OutlinedTextField(
                            label = { Text(getString(R.string.audio_player_element_time_seconds)) },
                            value = lengthText,
                            onValueChange = {
                                lengthText = it.filter { char -> char.isDigit() || char == '.' }
                                length = it.toFloatOrNull() ?: 0f
                            },
                            isError = length <= 0f && lengthText != "",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true
                        )
                    } else {
                        OutlinedTextField(
                            label = { Text(getString(R.string.audio_player_element_duration_beats)) },
                            value = beatsText,
                            onValueChange = {
                                beatsText = it.filter { char -> char.isDigit() }
                                beats = it.toIntOrNull() ?: 0
                                length = beats.toFloat() * (60f / (lastBpm ?: 1))
                            },
                            isError = length <= 0 && beatsText != "",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if(isTempo) {
                        if(beats > 0 && bpm > 0 && bpm < 500) {
                            onConfirm(
                                SetTempo(
                                    startTime = last?.endTime ?: 0,
                                    beats = beats,
                                    maxEnd = audioLength,
                                    tempo = bpm
                                )
                            )
                        } else if(length > 0 && bpm > 0 && bpm < 500) {
                            onConfirm(
                                SetTempo(
                                    startTime = last?.endTime ?: 0,
                                    endTime = minOf((last?.endTime ?: 0) + (length * 1000L).toLong(), audioLength),
                                    tempo = bpm
                                )
                            )
                        } else {
                            if(bpm <= 0 || bpm >= 500) {
                                Toast.makeText(this, getString(R.string.audio_player_element_error_tempo), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, getString(R.string.audio_player_element_error_duration), Toast.LENGTH_SHORT).show()
                            }
                            return@TextButton
                        }
                    } else {
                        if(beats > 0 && lastBpm != null) {
                            onConfirm(
                                Pause(
                                    startTime = last?.endTime ?: 0,
                                    beats = beats,
                                    maxEnd = audioLength,
                                    tempo = lastBpm
                                )
                            )
                        } else if(length > 0) {
                            onConfirm(
                                Pause(
                                    startTime = last?.endTime ?: 0,
                                    endTime = minOf((last?.endTime ?: 0) + (length * 1000L).toLong(), audioLength)
                                )
                            )
                        } else {
                            if(beats <= 0) {
                                Toast.makeText(this, getString(R.string.audio_player_element_error_beats), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, getString(R.string.audio_player_element_error_duration), Toast.LENGTH_SHORT).show()
                            }
                            return@TextButton
                        }
                    }
                }) {
                    Text(getString(R.string.generic_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismiss()
                }) {
                    Text(getString(R.string.generic_cancel))
                }
            }
        )
    }
}