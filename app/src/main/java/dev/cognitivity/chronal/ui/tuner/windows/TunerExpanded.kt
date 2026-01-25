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
import android.content.pm.PackageManager
import android.graphics.Path
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.Tuner
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.Instrument
import dev.cognitivity.chronal.toSp
import dev.cognitivity.chronal.ui.MorphedShape
import dev.cognitivity.chronal.ui.tuner.AudioDialog
import dev.cognitivity.chronal.ui.tuner.TunerGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min


@Composable
fun TunerPageExpanded(
    tuner: Tuner?,
    mainActivity: MainActivity
) {
    val scope = rememberCoroutineScope()
    var weight by remember { mutableFloatStateOf(Settings.TUNER_LAYOUT.get()) }
    var screenSize by remember { mutableStateOf(IntSize.Zero) }

    var showTuningDialog by remember { mutableStateOf(false) }
    var tuningNote by remember { mutableIntStateOf(-1) }
    val hz = tuner?.hz ?: -1f
    val tune: Pair<String, Float> = if(tuner != null && hz != 0f) {
        frequencyToNote(tuner.hz)
    } else {
        mainActivity.getString(R.string.generic_not_applicable) to Float.NaN
    }
    val instrument = Settings.PRIMARY_INSTRUMENT.get()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Row(
            modifier = Modifier.padding(horizontal = 12.dp)
                .fillMaxSize()
                .padding(
                    start = 0.dp,
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .onGloballyPositioned { layoutCoordinates ->
                    screenSize = layoutCoordinates.size
                }
        ) {
            Box(
                modifier = Modifier.weight(weight)
            ) {
                NoteDisplay(tuner, hz, instrument)
            }
            VerticalDragHandle(
                modifier = Modifier.align(Alignment.CenterVertically).draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        weight = (weight + delta/screenSize.width).coerceIn(0.3f, 0.5f)
                        Settings.TUNER_LAYOUT.set(weight)
                    },
                    onDragStopped = {
                        CoroutineScope(Dispatchers.Default).launch {
                            Settings.TUNER_LAYOUT.save()
                        }
                    }
                ).systemGestureExclusion()
            )
            Box(
                modifier = Modifier.weight(1f - weight)
            ) {
                PitchGraphHorizontal(tune.second, tuner)

                FilledIconToggleButton(
                    checked = playing,
                    onCheckedChange = {
                        showTuningDialog = true
                    },
                    modifier = Modifier.padding(8.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_volume_up_24),
                        contentDescription = mainActivity.getString(R.string.tuner_play_frequency),
                    )
                }

                if(showTuningDialog) {
                    AudioDialog(true, tuningNote,
                        onChange = {
                            tuningNote = it
                            scope.launch {
                                player.setFrequency(transposeFrequency(getA4().toFloat(), it - 69).toDouble())
                            }
                        },
                        onConfirm = {
                            player.start()
                            playing = true
                        },
                        onStop = {
                            player.stop()
                            playing = false
                        },
                        onDismiss = {
                            showTuningDialog = false
                        }
                    )
                }

                if(ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    PermissionWarning(innerPadding, mainActivity)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NoteDisplay(tuner: Tuner?, hz: Float, instrument: Instrument) {
    var fullscreen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
    ) {
        FlowRow(
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .padding(start = 4.dp, top = 8.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Box(
                modifier = Modifier.padding(2.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(8.dp, 4.dp)
            ) {
                val hertz = context.getString(R.string.tuner_hz, Settings.TUNER_FREQUENCY.get())
                Text(context.getString(R.string.tuner_tuning_at, hertz),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Box(
                modifier = Modifier.padding(2.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                    .padding(8.dp, 4.dp)
            ) {
                Text("${tuner?.probability?.times(100)?.toInt() ?: "0"}%",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        val showTransposition = Settings.TRANSPOSE_NOTES.get()
        Column(
            modifier = Modifier.padding(8.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            val graphWeight by animateFloatAsState(
                targetValue = if (fullscreen) 1f else 0.75f,
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "graphWeight"
            )
            val noteWeight by animateFloatAsState(
                targetValue = if (fullscreen) 0.01f else 1f,
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "noteWeight"
            )

            Box(
                modifier = Modifier.padding(8.dp)
                    .fillMaxWidth()
                    .weight(graphWeight)
                    .clip(RoundedCornerShape(16.dp))
                    .clipToBounds()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable {
                        fullscreen = !fullscreen
                    }
            ) {
                TunerGraph(tuner, fullscreen)
            }

            if(noteWeight != 0.01f) {
                Column(
                    modifier = Modifier.padding(8.dp)
                        .fillMaxWidth()
                        .weight(noteWeight)
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp))
                        .padding(4.dp),
                ) {
                    DrawName(context.getString(R.string.tuner_concert_pitch), context.getString(R.string.tuner_concert_pitch_short))
                    Spacer(modifier = Modifier.height(8.dp))
                    DrawNote(hz)
                }

                if(showTransposition) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                            .fillMaxWidth()
                            .weight(noteWeight)
                            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp))
                            .padding(4.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        DrawName(instrument.name, instrument.shortened)
                        Spacer(modifier = Modifier.height(8.dp))
                        DrawNote(transposeFrequency(hz, -instrument.transposition))
                    }
                }
            }
        }
    }
}

@Composable
fun PitchGraphHorizontal(cents: Float, tuner: Tuner?) {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp)
                .align(Alignment.Center)
        ) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DrawLines(cents.isNaN())
            }
            PitchPointerHorizontal(cents, tuner)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PitchPointerHorizontal(cents: Float, tuner: Tuner?) {
    val shapeA = remember {
        RoundedPolygon.star(12, rounding = CornerRounding(0.2f), radius = 1.8f)
    }
    val shapeB = remember {
        RoundedPolygon.circle(12)
    }
    val morph = remember {
        Morph(shapeA, shapeB)
    }

    val animatedColor = animateColorAsState(
        targetValue = if (cents.isNaN()) MaterialTheme.colorScheme.surface
        else if (abs(cents) >= 40) MaterialTheme.colorScheme.surfaceContainerHighest
        else if (abs(cents) >= 30) MaterialTheme.colorScheme.secondaryContainer
        else if (abs(cents) >= 20) MaterialTheme.colorScheme.tertiaryContainer
        else if (abs(cents) >= 5) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = MotionScheme.expressive().fastEffectsSpec(),
        label = "animatedColor"
    )

    val textColor = animateColorAsState(
        targetValue =
            if (cents.isNaN()) MaterialTheme.colorScheme.surface
            else if (abs(cents) >= 40) MaterialTheme.colorScheme.onSurface
            else if (abs(cents) >= 30) MaterialTheme.colorScheme.onSecondaryContainer
            else if (abs(cents) >= 20) MaterialTheme.colorScheme.onTertiaryContainer
            else if (abs(cents) >= 5) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.surface,
        animationSpec = MotionScheme.expressive().fastEffectsSpec(),
        label = "textColor1"
    )

    val morphProgress = remember { Animatable(1f) }
    LaunchedEffect(cents) {
        morphProgress.animateTo(
            targetValue = abs(if (cents.isNaN()) 0f else cents) / 50 * -0.75f + 1,
            animationSpec = MotionScheme.expressive().fastSpatialSpec(),
        )
    }

    val animatedPosition = remember { Animatable(0.5f) }
    LaunchedEffect(cents) {
        animatedPosition.animateTo(
            targetValue = (if (cents.isNaN()) 0.5f else 0.5f + cents / 50 * 0.5f),
            animationSpec = MotionScheme.expressive().fastSpatialSpec(),
        )
    }

    val animatedRotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            animatedRotation.snapTo(animatedRotation.value.mod(360f))
            val currentRotation = animatedRotation.value

            val nextRotation = if (tuner == null) currentRotation
            else {
                val elapsed = (System.currentTimeMillis() - tuner.lastUpdate).coerceIn(0L, 5000L).toFloat() / 5000f
                val easing = EaseInExpo.transform(1f - elapsed)
                val change = easing * 30f
                currentRotation + change
            }

            animatedRotation.animateTo(
                targetValue = nextRotation,
                animationSpec = tween(
                    durationMillis = 50,
                    easing = LinearEasing
                )
            )
        }
    }

    Row(
        Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Spacer(modifier = Modifier.weight(1f)) // offset by half of 1 line
        BoxWithConstraints(modifier = Modifier.align(Alignment.CenterVertically)
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .weight(40f)
        ) {
            val lineOffset = this.maxWidth / 21
            val maxWidth = this.maxWidth - lineOffset
            val xOffset = maxWidth * animatedPosition.value + lineOffset / 2
            val xOffsetPadded = xOffset.coerceIn(40.dp, maxWidth - 40.dp)

            Box(
                modifier = Modifier.offset(x = xOffsetPadded - 80.dp, y = -(80).dp)
                    .align(Alignment.CenterStart)
                    .clip(
                        MorphedShape(
                            morph,
                            morphProgress.value,
                            animatedRotation.value
                        )
                    )
                    .requiredSize(160.dp)
                    .background(animatedColor.value)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = (if(cents.toInt() < 0) "" else "+") + cents.toInt().toString(),
                        fontSize = 42.sp,
                        color = textColor.value
                    )
                    val text: String = if(cents.isNaN()) ""
                    else if(cents.toInt() > 0) "\uEA66"
                    else if(cents.toInt() < 0) "\uEA64"
                    else "\uEA65"
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally).offset(
                            y = -(if (text.contains("♯")) sharpOffset
                            else if (text.contains("♭")) flatOffset
                            else 0.dp)
                        ),
                        text = text,
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.bravuratext)),
                            fontSize = 48.dp.toSp(),
                            fontWeight = FontWeight.Medium,
                        ),
                        color = textColor.value
                    )
                }
            }
            Box(
                modifier = Modifier.offset(x = xOffset - 0.dp, y = 80.dp)
                    .align(Alignment.CenterStart)
                    .size(64.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            val scale = min(size.width, size.height)
                            val matrix = Matrix()
                            matrix.scale(scale, scale)
                            matrix.translate(0.5f,0.25f)
                            matrix.rotateZ(180f)
                            val path = MaterialShapes.Arrow.toPath(Path()).asComposePath()
                            path.transform(matrix)
                            drawPath(path, animatedColor.value.copy(alpha = 0.5f))
                        }
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}