package dev.cognitivity.chronal.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import androidx.lifecycle.ViewModel
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.onsets.ComplexOnsetDetector
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.ui.MorphedShape
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlin.math.round

class BeatDetectorActivity : ComponentActivity() {
    var isRecording by mutableStateOf(false)
    var showMicrophoneDialog by mutableStateOf(false)
    var peakThreshold by mutableDoubleStateOf(0.2)
    var silenceThreshold by mutableDoubleStateOf(-90.0)
    private val microphonePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
        if (permission) {
            isRecording = true
            showMicrophoneDialog = false
            viewModel.startAudio(peakThreshold, silenceThreshold)
        } else {
            isRecording = false
            showMicrophoneDialog = true
        }
    }

    val viewModel: BeatDetectorViewModel = BeatDetectorViewModel()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MetronomeTheme {
                MainContent()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopAudio()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            LaunchedEffect(peakThreshold, silenceThreshold) {
                viewModel.startAudio(peakThreshold, silenceThreshold)
                isRecording = true
            }
        } else {
            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(getString(R.string.beat_detector_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                finish()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = getString(R.string.generic_back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row {
                        Icon(
                            painter = painterResource(R.drawable.outline_mic_none_24),
                            contentDescription = getString(R.string.generic_microphone),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(4.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Text(getString(if(isRecording) R.string.beat_detector_listening else R.string.beat_detector_disabled),
                            modifier = Modifier.align(Alignment.CenterVertically),
                        )
                    }
                    TextButton(
                        onClick = {
                            if(isRecording) {
                                viewModel.onsets.clear()
                                viewModel.lastOnset = 0L
                                viewModel.lastSalience = 0.0
                                viewModel.bpm = 0.0f
                                Toast.makeText(this@BeatDetectorActivity, getString(R.string.beat_detector_reset_notification),
                                    Toast.LENGTH_SHORT).show()
                            } else {
                                if(ContextCompat.checkSelfPermission(this@BeatDetectorActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    viewModel.startAudio(peakThreshold, silenceThreshold)
                                    isRecording = true
                                } else {
                                    Log.d("a", "Requesting microphone permission")
                                    microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                    ) {
                        Text(getString(if(isRecording) R.string.generic_reset else R.string.generic_start))
                    }
                }
                Box(
                    modifier = Modifier.weight(2f)
                        .fillMaxSize()
                ) {
                    DetectedVisualizer(isRecording)
                }
                Box(
                    modifier = Modifier.weight(1f)
                        .fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxHeight(0.25f)
                                .aspectRatio(1f)
                                .align(Alignment.CenterVertically)
                                .padding(8.dp)
                        ) {
                            BpmVisualizer(isRecording)
                        }
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = "${round(viewModel.bpm).toInt()}",
                            fontSize = 96.sp,
                            color = if(isRecording) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Text(
                            modifier = Modifier
                                .offset(y = (-8).dp)
                                .align(Alignment.Bottom),
                            text = getString(R.string.metronome_bpm),
                            fontSize = 48.sp,
                            color = if(isRecording) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceContainerHighest,
                        )
                    }
                }
            }
            if(showMicrophoneDialog) {
                AlertDialog(
                    onDismissRequest = { showMicrophoneDialog = false },
                    confirmButton = @Composable {
                        TextButton(onClick = {
                            showMicrophoneDialog = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = ("package:$packageName").toUri()
                            startActivity(intent)
                        }) {
                            Text(getString(R.string.generic_settings))
                        }
                    },
                    dismissButton = @Composable {
                        TextButton(onClick = {
                            showMicrophoneDialog = false
                        }) {
                            Text(getString(R.string.generic_cancel))
                        }
                    },
                    icon = @Composable {
                        Icon(
                            painter = painterResource(R.drawable.outline_warning_24),
                            contentDescription = getString(R.string.generic_warning),
                        )
                    },
                    title = @Composable {
                        Text(getString(R.string.beat_detector_permission_title))
                    },
                    text = @Composable {
                        Text(getString(R.string.beat_detector_permission_text))
                    }
                )
            }
        }
    }

    @Composable
    fun BoxScope.DetectedVisualizer(isRecording: Boolean) {
        val animatedRotation = remember { Animatable(0f) }
        if(isRecording) {
            LaunchedEffect(Unit) {
                while (true) {
                    animatedRotation.snapTo(0f)
                    animatedRotation.animateTo(
                        targetValue = 360f,
                        animationSpec = tween(30000, easing = LinearEasing)
                    )
                }
            }
        }

        val shapeA = remember {
            RoundedPolygon.star(12, rounding = CornerRounding(0.2f), radius = 1.8f)
        }
        val shapeB = remember {
            RoundedPolygon.circle(12)
        }
        val morph = remember {
            Morph(shapeA, shapeB)
        }

        val morphedShape = MorphedShape(morph, 0.7f, animatedRotation.value)

        Box(
            modifier = Modifier.fillMaxSize(0.75f)
                .aspectRatio(1f)
                .align(Alignment.Center)
                .background(if(isRecording) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.surfaceContainer, morphedShape)
                .border(2.dp, if(isRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, morphedShape)
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_music_cast_24),
                contentDescription = getString(R.string.generic_microphone),
                tint = if(isRecording) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxSize(0.333f)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
            )

            val lastOnset = System.currentTimeMillis() - viewModel.lastOnset
            if(lastOnset < 1000) {
                val progress = EaseOutCubic.transform(lastOnset / 1000f)
                Box(
                    modifier = Modifier.fillMaxSize(0.75f)
                        .scale(1.333f + (progress * 0.25f))
                        .aspectRatio(1f)
                        .align(Alignment.Center)
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 1-progress), morphedShape)
                )
            }
        }
    }

    @Composable
    fun BoxScope.BpmVisualizer(isRecording: Boolean) {
        val animatedRotation = remember { Animatable(0f) }
        if(isRecording) {
            LaunchedEffect(Unit) {
                while (true) {
                    animatedRotation.snapTo(0f)
                    animatedRotation.animateTo(
                        targetValue = 360f,
                        animationSpec = tween(30000, easing = LinearEasing)
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(0.75f)
                .aspectRatio(1f)
                .align(Alignment.Center)
                .background(if(isRecording) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                .border(2.dp, if(isRecording) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
            val bpm = viewModel.bpm
            if(bpm == 0f) return@Box

            val ms = 60f / bpm * 1000f

            val time = System.currentTimeMillis() - viewModel.lastOnset

            val progress = EaseOutCubic.transform((time / ms).mod(1f))
            Box(
                modifier = Modifier.fillMaxSize(0.75f)
                    .scale(1.5f + (progress * 0.75f))
                    .aspectRatio(1f)
                    .rotate(animatedRotation.value)
                    .align(Alignment.Center)
                    .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 1 - progress), CircleShape)
            )
        }
    }
}


class BeatDetectorViewModel : ViewModel() {
    var dispatcher: AudioDispatcher? = null
    private var audioThread: Thread? = null
    private var onsetDetector: ComplexOnsetDetector? = null

    val onsets = mutableStateListOf<Float>()
    var start = 0L
    var lastOnset by mutableLongStateOf(0L)
    var lastSalience by mutableDoubleStateOf(0.0)
    var bpm by mutableFloatStateOf(0.0f)

    fun startAudio(peakThreshold: Double, silenceThreshold: Double) {
        stopAudio()

        if (ActivityCompat.checkSelfPermission(ChronalApp.getInstance(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 4096, 3072)
        onsetDetector = ComplexOnsetDetector(4096, peakThreshold, 0.07, silenceThreshold)

        onsetDetector!!.setHandler { time, salience ->
            onsets += time.toFloat()
            lastSalience = salience
            lastOnset = System.currentTimeMillis()
            bpm = calculateBPM(onsets.toFloatArray())
        }

        dispatcher!!.addAudioProcessor(onsetDetector)
        audioThread = Thread({ dispatcher!!.run() }, "Audio Thread")
        start = System.currentTimeMillis()
        audioThread!!.start()
    }

    fun stopAudio() {
        dispatcher?.stop()
        audioThread?.interrupt()
        audioThread = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}

fun calculateBPM(timestamps: FloatArray): Float {
    if (timestamps.size < 2) return 0f

    val intervals = mutableListOf<Float>()
    for (i in 1 until timestamps.size) {
        val interval = timestamps[i] - timestamps[i - 1]
        if (interval in 0.2f..3f) {
            intervals.add(interval)
        }
    }

    if (intervals.size < 2) return 0f

    val maxK = minOf(6, intervals.size)
    var lowestInertia = Float.MAX_VALUE
    var bestCentroids = listOf<Float>()
    var bestAssignments = listOf<Int>()

    for (k in 1..maxK) {
        val (centroids, assignments, inertia) = runKMeans(intervals, k)
        if (inertia < lowestInertia) {
            lowestInertia = inertia
            bestCentroids = centroids
            bestAssignments = assignments
        }
    }

    val dominantIndex = bestAssignments.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: return 0f
    val dominantInterval = bestCentroids[dominantIndex]

    return 60f / dominantInterval
}

private fun runKMeans(data: List<Float>, k: Int, iterations: Int = 20): Triple<List<Float>, List<Int>, Float> {
    val centroids = data.shuffled().take(k).toMutableList()
    val assignments = MutableList(data.size) { 0 }

    repeat(iterations) {
        for ((i, value) in data.withIndex()) {
            val closest = centroids.indices.minByOrNull { j -> kotlin.math.abs(value - centroids[j]) } ?: 0
            assignments[i] = closest
        }

        for (j in 0 until k) {
            val cluster = data.filterIndexed { index, _ -> assignments[index] == j }
            if (cluster.isNotEmpty()) {
                centroids[j] = cluster.average().toFloat()
            }
        }
    }

    val inertia = data.indices.sumOf { i ->
        val diff = data[i] - centroids[assignments[i]]
        (diff * diff).toDouble()
    }.toFloat()

    return Triple(centroids, assignments, inertia)
}