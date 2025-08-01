package dev.cognitivity.chronal.ui.metronome

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.rhythm.metronome.Beat
import dev.cognitivity.chronal.activity.vibratorManager
import dev.cognitivity.chronal.ui.metronome.windows.drawBeats
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.CircularClock(primary: Boolean, trackSize: Float, trackOff: Color, trackPrimary: Color, trackSecondary: Color,
                           majorOffColor: Color, minorOffColor: Color, majorPrimaryColor: Color, minorPrimaryColor: Color,
                           majorSecondaryColor: Color, minorSecondaryColor: Color
) {
    val metronome = if(primary) ChronalApp.getInstance().metronome else ChronalApp.getInstance().metronomeSecondary
    var rhythm by remember { mutableStateOf(metronome.getRhythm()) }
    var intervals by remember { mutableStateOf(metronome.getIntervals()) }
    metronome.setEditListener(1) {
        rhythm = it
        intervals = metronome.getIntervals()
    }

    val progress = remember { Animatable(0f) }
    var trackColorType by remember { mutableIntStateOf(0) }
    var progressColorType by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    var currentMeasureDuration by remember { mutableFloatStateOf(0f) }
    var loopIndex by remember { mutableIntStateOf(0) }
    var currentMeasure by remember { mutableIntStateOf(0) }

    val updateListener = { beat: Beat ->
        val timestamp = metronome.timestamp

        coroutineScope.launch {
            delay(ChronalApp.getInstance().settings.visualLatency.value.toLong())
            if(!metronome.playing || timestamp != metronome.timestamp) return@launch
            if((!ChronalApp.getInstance().settings.metronomeVibrations.value && primary)
                || (!ChronalApp.getInstance().settings.metronomeVibrationsSecondary.value && !primary)) return@launch

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && vibratorManager != null) {
                val vibration = if(beat.isHigh) VibrationEffect.createOneShot(10, 255) else VibrationEffect.createOneShot(3, 255)
                vibratorManager!!.vibrate(CombinedVibration.createParallel(vibration))
            } else {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(if(beat.isHigh) 10 else 3)
            }
        }
        coroutineScope.launch {
            delay(ChronalApp.getInstance().settings.visualLatency.value.toLong())
            if(!metronome.playing || timestamp != metronome.timestamp) return@launch

            if(beat.measure == 0 && beat.index == 0) loopIndex++
            currentMeasure = beat.measure
            if(beat.index == 0) currentMeasureDuration = 0f
            val measure = rhythm.measures[beat.measure]
            val measureDuration = measure.timeSig.first / measure.timeSig.second.toFloat()
            val next = currentMeasureDuration + abs(beat.duration).toFloat()

            progress.snapTo(currentMeasureDuration / measureDuration)

            if(rhythm.measures.size == 1) {
                if(loopIndex % 2 == 1) {
                    trackColorType = 0
                    progressColorType = 1
                } else {
                    trackColorType = 1
                    progressColorType = 0
                }
            } else {
                if(beat.measure == 0) {
                    trackColorType = 0
                    progressColorType = 1
                } else if(beat.measure == rhythm.measures.size - 1) {
                    if(beat.measure % 2 != 0) {
                        trackColorType = 1
                        progressColorType = 0
                    } else {
                        trackColorType = 2
                        progressColorType = 0
                    }
                } else if(beat.measure % 2 != 0) {
                    trackColorType = 1
                    progressColorType = 2
                } else {
                    trackColorType = 2
                    progressColorType = 1
                }
            }

            currentMeasureDuration += abs(beat.duration).toFloat()

            progress.animateTo(
                targetValue = next / measureDuration,
                animationSpec = tween(
                    durationMillis = (abs(beat.duration) * 60000 / metronome.bpm * metronome.beatValue).toInt(),
                    easing = LinearEasing
                )
            )
        }
    }
    metronome.setUpdateListener(0) { updateListener(it) }

    val pauseListener = { paused: Boolean ->
        loopIndex = 0
        currentMeasure = 0
        if(paused) {
            currentMeasureDuration = 0f

            if(trackColorType != 0 && progressColorType != 0) trackColorType = 0
            val animateForward = trackColorType != 0
            coroutineScope.launch {
                progress.animateTo(
                    targetValue = if(animateForward) 1f else 0f,
                    animationSpec = MotionScheme.expressive().slowEffectsSpec(),
                )
                progress.snapTo(0f)
                trackColorType = 0
                progressColorType = 1
            }
        }
    }
    metronome.setPauseListener(0) { pauseListener(it) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center)
    ) {
        Box(modifier = Modifier
            .aspectRatio(1f)
            .align(Alignment.Center)
        ) {
            val surface = MaterialTheme.colorScheme.surface

            val trackColor = when(trackColorType) {
                0 -> trackOff
                1 -> trackPrimary
                else -> trackSecondary
            }
            val progressColor = when(progressColorType) {
                0 -> trackOff
                1 -> trackPrimary
                else -> trackSecondary
            }
            val majorOff = when(trackColorType) {
                0 -> majorOffColor
                1 -> majorPrimaryColor
                else -> majorSecondaryColor
            }
            val minorOff = when(trackColorType) {
                0 -> minorOffColor
                1 -> minorPrimaryColor
                else -> minorSecondaryColor
            }
            val majorPrimary = when(progressColorType) {
                0 -> majorOffColor
                1 -> majorPrimaryColor
                else -> majorSecondaryColor
            }
            val minorPrimary = when(progressColorType) {
                0 -> minorOffColor
                1 -> minorPrimaryColor
                else -> minorSecondaryColor
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = (size.minDimension / 2) - trackSize / 2
                val center = Offset(size.width / 2, size.height / 2)

                drawCircle(
                    color = trackColor,
                    radius = radius,
                    center = center,
                    style = Stroke(width = trackSize)
                )

                val arcAngle = (progress.value * 360f) - 90f
                if(arcAngle == -90f) return@Canvas // hide outlines when not playing

                drawCircle(
                    color = surface,
                    radius = trackSize * 1.5f,
                    center = Offset(center.x, center.y - radius)
                )
                drawCircle(
                    color = surface,
                    radius = trackSize * 1.75f,
                    center = Offset(
                        center.x + radius * cos(Math.toRadians(arcAngle.toDouble())).toFloat(),
                        center.y + radius * sin(Math.toRadians(arcAngle.toDouble())).toFloat()
                    )
                )

                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = progress.value * 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = trackSize)
                )

                drawCircle(
                    color = progressColor,
                    radius = trackSize / 2,
                    center = Offset(center.x, center.y - radius)
                )
                drawCircle(
                    color = progressColor,
                    radius = trackSize / 2,
                    center = Offset(
                        center.x + radius * cos(Math.toRadians(arcAngle.toDouble())).toFloat(),
                        center.y + radius * sin(Math.toRadians(arcAngle.toDouble())).toFloat()
                    )
                )
            }
            drawBeats(progress, trackSize, intervals.filter { it.measure == currentMeasure }, majorOff, minorOff, majorPrimary, minorPrimary)
        }
        TempoChanger()
    }
}
