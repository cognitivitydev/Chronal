package dev.cognitivity.chronal.activity

import android.media.MediaPlayer
import android.media.SyncParams
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import dev.cognitivity.chronal.rhythm.player.elements.SetTempo
import dev.cognitivity.chronal.round
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


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
        Log.d("a", "${tempFile.absolutePath}")
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
                        mediaPlayer.start()
                    }
                    CoroutineScope(coroutineContext).launch {
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
                            onCheckedChange = {
                                fabExpanded = it
                            },
                        ) {
                            if(!fabExpanded) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = getString(R.string.generic_add),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = getString(R.string.generic_close),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                ) {
                    FloatingActionButtonMenuItem(
                        text = { Text("Set BPM") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_music_note_24),
                                contentDescription = "Set BPM"
                            )
                        },
                        onClick = {
                            showBpmDialog = true
                            fabExpanded = false
                        }
                    )
                    FloatingActionButtonMenuItem(
                        text = { Text("Add pause") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_music_off_24),
                                contentDescription = "Add pause"
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
                modifier = Modifier
                    .fillMaxSize()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    AudioControls()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    RhythmList()
                }
            }

            if(showBpmDialog) {
                var useTime by remember { mutableStateOf(true) }
                val last = rhythm.elements.lastOrNull()
                val lastBpm = if(last is SetTempo) last.tempo else null

                var bpm by remember { mutableIntStateOf(lastBpm ?: 0) }
                var bpmText by remember { mutableStateOf(lastBpm?.toString() ?: "") }

                var length by remember { mutableFloatStateOf(0f) }
                var lengthText by remember { mutableStateOf("") }

                var beatsText by remember { mutableStateOf("") }
                var beats by remember { mutableIntStateOf(-1) }

                AlertDialog(
                    onDismissRequest = { showBpmDialog = false },
                    title = { Text("Set BPM") },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                label = { Text("Tempo") },
                                value = bpmText,
                                onValueChange = {
                                    bpmText = it.filter { char -> char.isDigit() }
                                    bpm = bpmText.toIntOrNull() ?: 0
                                },
                                isError = (bpm <= 0 || bpm >= 500) && bpmText != "",
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                )
                            )

                            Text(
                                text = "Duration type",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )
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
                                            contentDescription = ChronalApp.Companion.context.getString(R.string.generic_selected),
                                        )
                                        Spacer(modifier = Modifier.Companion.width(ToggleButtonDefaults.IconSpacing))
                                    }
                                    Text("Time")
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
                                    contentPadding = ButtonDefaults.ContentPadding
                                ) {
                                    if (!useTime) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = ChronalApp.Companion.context.getString(R.string.generic_selected),
                                        )
                                        Spacer(modifier = Modifier.Companion.width(ToggleButtonDefaults.IconSpacing))
                                    }
                                    Text("Beats")
                                }
                            }

                            if(useTime) {
                                OutlinedTextField(
                                    label = { Text("Time (seconds)") },
                                    value = lengthText,
                                    onValueChange = {
                                        lengthText = it.filter { char -> char.isDigit() || char == '.' }
                                        length = it.toFloatOrNull() ?: 0f
                                    },
                                    isError = length <= 0f && lengthText != "",
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    )
                                )
                            } else {
                                OutlinedTextField(
                                    label = { Text("Beats") },
                                    value = beatsText,
                                    onValueChange = {
                                        beatsText = it.filter { char -> char.isDigit() }
                                        beats = it.toIntOrNull() ?: 0
                                        length = beats.toFloat() * (60f / (lastBpm ?: 1))
                                    },
                                    isError = length <= 0 && beatsText != "",
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if(beats > 0 && bpm > 0 && bpm < 500) {
                                val newRhythm = rhythm.copy(
                                    elements = rhythm.elements.toMutableList().apply {
                                        add(SetTempo(
                                            startTime = last?.endTime ?: 0,
                                            beats = beats,
                                            maxEnd = audioLength,
                                            tempo = bpm
                                        ))
                                    }
                                )
                                rhythm = newRhythm
                            } else if(length > 0 && bpm > 0 && bpm < 500) {
                                val newRhythm = rhythm.copy(
                                    elements = rhythm.elements.toMutableList().apply {
                                        add(SetTempo(
                                            startTime = last?.endTime ?: 0,
                                            endTime = minOf((last?.endTime ?: 0) + (length * 1000L).toLong(), audioLength),
                                            tempo = bpm
                                        ))
                                    }
                                )
                                rhythm = newRhythm
                            } else {
                                if(bpm <= 0 || bpm >= 500) {
                                    Toast.makeText(this, "Invalid BPM", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Invalid length", Toast.LENGTH_SHORT).show()
                                }
                                return@TextButton
                            }
                            showBpmDialog = false
                        }) {
                            Text(getString(R.string.generic_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showBpmDialog = false
                        }) {
                            Text(getString(R.string.generic_cancel))
                        }
                    }
                )
            }

            if(showPauseDialog) {
                var useTime by remember { mutableStateOf(true) }
                val last = rhythm.elements.lastOrNull()
                val lastBpm = if(last is SetTempo) last.tempo else null

                var length by remember { mutableFloatStateOf(0f) }
                var lengthText by remember { mutableStateOf("") }

                var beatsText by remember { mutableStateOf("") }
                var beats by remember { mutableIntStateOf(-1) }

                AlertDialog(
                    onDismissRequest = { showPauseDialog = false },
                    title = { Text("Add pause") },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Duration type",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            if(lastBpm == null) {
                                Text(
                                    text = "Beats are disabled because the previous element is not a tempo change.",
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
                                            contentDescription = ChronalApp.Companion.context.getString(R.string.generic_selected),
                                        )
                                        Spacer(modifier = Modifier.Companion.width(ToggleButtonDefaults.IconSpacing))
                                    }
                                    Text("Time")
                                }
                                ToggleButton(
                                    checked = !useTime,
                                    onCheckedChange = {
                                        if(lastBpm != null) {
                                            useTime = false
                                            length = 0f
                                            beats = -1
                                            lengthText = ""
                                            beatsText = ""
                                        }
                                    },
                                    enabled = lastBpm != null,
                                    shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                    contentPadding = ButtonDefaults.ContentPadding
                                ) {
                                    if (!useTime) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = ChronalApp.Companion.context.getString(R.string.generic_selected),
                                        )
                                        Spacer(modifier = Modifier.Companion.width(ToggleButtonDefaults.IconSpacing))
                                    }
                                    Text("Beats")
                                }
                            }

                            if(useTime) {
                                OutlinedTextField(
                                    label = { Text("Time (seconds)") },
                                    value = lengthText,
                                    onValueChange = {
                                        lengthText = it.filter { char -> char.isDigit() || char == '.' }
                                        length = it.toFloatOrNull() ?: 0f
                                    },
                                    isError = length <= 0f && lengthText != "",
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    )
                                )
                            } else {
                                OutlinedTextField(
                                    label = { Text("Beats") },
                                    value = beatsText,
                                    onValueChange = {
                                        beatsText = it.filter { char -> char.isDigit() }
                                        beats = it.toIntOrNull() ?: 0
                                        length = beats.toFloat() * (60f / (lastBpm ?: 1))
                                    },
                                    isError = length <= 0 && beatsText != "",
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if(beats > 0 && lastBpm != null) {
                                val newRhythm = rhythm.copy(
                                    elements = rhythm.elements.toMutableList().apply {
                                        add(Pause(
                                            startTime = last?.endTime ?: 0,
                                            beats = beats,
                                            maxEnd = audioLength,
                                            tempo = lastBpm
                                        ))
                                    }
                                )
                                rhythm = newRhythm
                            } else if(length > 0) {
                                val newRhythm = rhythm.copy(
                                    elements = rhythm.elements.toMutableList().apply {
                                        add(Pause(
                                            startTime = last?.endTime ?: 0,
                                            endTime = minOf((last?.endTime ?: 0) + (length * 1000L).toLong(), audioLength)
                                        ))
                                    }
                                )
                                rhythm = newRhythm
                            } else {
                                Toast.makeText(this, "Invalid BPM or length", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            showPauseDialog = false
                        }) {
                            Text(getString(R.string.generic_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showPauseDialog = false
                        }) {
                            Text(getString(R.string.generic_cancel))
                        }
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
                        Text("Updating audio...", modifier = Modifier.padding(top = 96.dp))
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
                Spacer(modifier = Modifier.weight(1f))
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
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 16.dp)
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
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
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
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
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
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
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
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
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
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
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
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
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
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
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
                    if(showMore) {
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

                val text = when (element) {
                    is SetTempo -> "Set tempo"
                    is Pause -> "Pause"
                }
                val altText = when (element) {
                    is SetTempo -> "${element.tempo} BPM"
                    is Pause -> "${((element.endTime - element.startTime) / 1000f).round(2)} s"
                }

                val isNow = element.startTime <= progress && element.endTime >= progress
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if(isNow) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Timer(time = element.startTime)
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
                                if(element.beats != null) {
                                    Text(
                                        text = "${element.beats} beats",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = onColorContainer
                                    )
                                }
                                Text(
                                    text = "${((element.endTime - element.startTime) / 1000f).round(2)} seconds",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if(element.beats != null) color else onColorContainer
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(32.dp))
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
}