package dev.cognitivity.chronal.ui.tuner.windows

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Path
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
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
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.Instrument
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.Tuner
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.pxToDp
import dev.cognitivity.chronal.toSp
import dev.cognitivity.chronal.ui.MorphedShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min


var playing by mutableStateOf(false)
val player = SineWavePlayer(440.0)

@Composable
fun TunerPageExpanded(
    tuner: Tuner?,
    mainActivity: MainActivity
) {
    var weight by remember { mutableFloatStateOf(ChronalApp.getInstance().settings.tunerLayout.value) }
    var screenSize by remember { mutableStateOf(IntSize.Zero) }

    val hz = tuner?.hz ?: -1f
    val tune: Pair<String, Float> = if(tuner != null && hz != 0f) {
        frequencyToNote(tuner.hz)
    } else {
        mainActivity.getString(R.string.generic_not_applicable) to Float.NaN
    }
    val instrument = ChronalApp.getInstance().settings.primaryInstrument.value

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
                        ChronalApp.getInstance().settings.tunerLayout.value = weight
                    },
                    onDragStopped = {
                        CoroutineScope(Dispatchers.Default).launch {
                            ChronalApp.getInstance().settings.save()
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
                        if(!playing) player.start() else player.stop()
                        playing = !playing
                    },
                    modifier = Modifier.padding(8.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_volume_up_24),
                        contentDescription = mainActivity.getString(R.string.tuner_play_frequency),
                    )
                }

                if(ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    PermissionWarning(innerPadding, mainActivity)
                }
            }
        }
    }
}

@Composable
fun NoteDisplay(tuner: Tuner?, hz: Float, instrument: Instrument) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(4.dp),
        ) {
            Box(
                modifier = Modifier.padding(2.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(8.dp, 4.dp)
            ) {
                Text(context.getString(R.string.tuner_tuning_at, ChronalApp.getInstance().settings.tunerFrequency.value),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
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

        val showTransposition = ChronalApp.getInstance().settings.transposeNotes.value
        Column(
            modifier = Modifier.padding(8.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
                    .fillMaxWidth()
                    .then(if (showTransposition) Modifier.weight(1f) else Modifier.fillMaxHeight(0.5f))
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp))
                    .padding(8.dp),
            ) {
                DrawName(context.getString(R.string.tuner_concert_pitch), context.getString(R.string.tuner_concert_pitch_short))
                Spacer(modifier = Modifier.height(8.dp))
                DrawNote(transposeFrequency(hz, -instrument.transposition))
            }

            if(showTransposition) {
                Column(
                    modifier = Modifier.padding(8.dp)
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    DrawName(instrument.name, instrument.shortened)
                    Spacer(modifier = Modifier.height(8.dp))
                    DrawNote(hz)
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

    val textColor1 = animateColorAsState(
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

    val textColor2 = animateColorAsState(
        targetValue = if (cents.isNaN()) MaterialTheme.colorScheme.surface
        else if (abs(cents) >= 40) MaterialTheme.colorScheme.onSurfaceVariant
        else if (abs(cents) >= 30) MaterialTheme.colorScheme.secondary
        else if (abs(cents) >= 20) MaterialTheme.colorScheme.tertiary
        else if (abs(cents) >= 5) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.inverseOnSurface,
        animationSpec = MotionScheme.expressive().fastEffectsSpec(),
        label = "textColor2"
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
            val density = LocalDensity.current
            val parentWidthPx = this@BoxWithConstraints.constraints.maxWidth.toFloat()
            val xOffsetPx = parentWidthPx * animatedPosition.value

            val xOffsetDp = with(density) { xOffsetPx.toDp() }
            val xOffsetPaddedDp = xOffsetDp.coerceIn(40.dp, parentWidthPx.pxToDp() - 40.dp)


            Box(
                modifier = Modifier.offset(x = xOffsetPaddedDp - 80.dp, y = -(80).dp)
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
                        color = textColor1.value
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
                        color = textColor2.value
                    )
                }
            }
            Box(
                modifier = Modifier.offset(x = xOffsetDp - 0.dp, y = 80.dp)
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
//                TrianglePointer(animatedColor.value)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
//    CloverShape()
}