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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import dev.cognitivity.chronal.ui.tuner.AudioDialog
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

@Composable
fun TunerPageCompact(
    tuner: Tuner?,
    padding: PaddingValues,
    mainActivity: MainActivity,
) {
    val scope = rememberCoroutineScope()
    var showTuningDialog by remember { mutableStateOf(false) }
    var tuningNote by remember { mutableIntStateOf(-1) }
    val hz = tuner?.hz ?: -1f
    val tune: Pair<String, Float> = if (tuner != null && hz != 0f) {
        frequencyToNote(tuner.hz)
    } else {
        context.getString(R.string.generic_not_applicable) to Float.NaN
    }
    val instrument = ChronalApp.getInstance().settings.primaryInstrument.value

    BoxWithConstraints(
        modifier = Modifier
            .padding(bottom = padding.calculateBottomPadding())
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val isWide = this.maxWidth > this.maxHeight

        if (isWide) {
            //side bar
            Row(
                Modifier.fillMaxSize()
            ) {
                Box(
                    Modifier.fillMaxWidth(0.4f)
                        .fillMaxHeight()
                ) {
                    TopBar(tuner, hz, instrument, wide = true)
                }

                //content
                Box(
                    Modifier
                        .weight(1f)
                        .padding(vertical = 40.dp)
                        .fillMaxHeight()
                ) {
                    PitchGraph(tune.second, tuner)
                    FilledIconToggleButton(
                        checked = playing,
                        onCheckedChange = {
                            showTuningDialog = true
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_volume_up_24),
                            contentDescription = mainActivity.getString(R.string.tuner_play_frequency),
                        )
                    }
                }
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.surface,
                topBar = {
                    TopBar(tuner, hz, instrument, wide = false)
                },
            ) { innerPadding ->
                Box(
                    Modifier
                        .padding(top = innerPadding.calculateTopPadding())
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 40.dp)
                            .align(Alignment.Center)
                    ) {
                        PitchGraph(tune.second, tuner)
                    }
                    FilledIconToggleButton(
                        checked = playing,
                        onCheckedChange = {
                            showTuningDialog = true
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_volume_up_24),
                            contentDescription = mainActivity.getString(R.string.tuner_play_frequency),
                        )
                    }
                }
            }
        }

        if (showTuningDialog) {
            AudioDialog(false, tuningNote,
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

        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            PermissionWarning(padding, mainActivity)
        }
    }
}

@Composable
fun TopBar(tuner: Tuner?, hz: Float, instrument: Instrument, wide: Boolean) {
    Surface(
        color = if(wide) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = if(wide) RoundedCornerShape(topEnd = 24.dp) else RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .fillMaxHeight(if(wide) 1f else 0.225f)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            FlowRow(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Box(
                    modifier = Modifier.padding(4.dp)
                        .align(Alignment.CenterVertically)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                ) {
                    val hertz = context.getString(R.string.tuner_hz, ChronalApp.getInstance().settings.tunerFrequency.value)
                    Text(context.getString(R.string.tuner_tuning_at, hertz),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.align(Alignment.Center)
                            .padding(8.dp, 4.dp)
                    )
                }
                Box(
                    modifier = Modifier.padding(4.dp, 4.dp, 16.dp, 4.dp)
                        .align(Alignment.CenterVertically)
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "${tuner?.probability?.times(100)?.toInt() ?: "0"}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.align(Alignment.Center)
                            .padding(8.dp, 4.dp)
                    )
                }
            }


            Box {
                val showTransposition = ChronalApp.getInstance().settings.transposeNotes.value

                if(wide) {
                    Column(
                        modifier = Modifier.align(Alignment.Center)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(context.getString(R.string.tuner_concert_pitch),
                                modifier = Modifier.fillMaxWidth()
                                    .align(Alignment.CenterHorizontally),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            DrawNote(hz)
                        }
                        if(showTransposition) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                DrawName(instrument.name, instrument.shortened)
                                DrawNote(transposeFrequency(hz, -instrument.transposition))
                            }
                        }
                    }
                } else {
                    if(showTransposition) {
                        Box(
                            Modifier.align(Alignment.Center)
                                .background(MaterialTheme.colorScheme.outline)
                                .width(1.dp)
                                .fillMaxHeight(0.75f)
                        )
                    }
                    Row(
                        Modifier.align(Alignment.Center)
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Column(
                            Modifier.align(Alignment.CenterVertically)
                                .then(if(showTransposition) Modifier.weight(1f) else Modifier.fillMaxWidth(0.5f))
                        ) {
                            Text(context.getString(R.string.tuner_concert_pitch),
                                modifier = Modifier.fillMaxWidth()
                                    .align(Alignment.CenterHorizontally),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            DrawNote(hz)
                        }
                        if(showTransposition) {
                            Column(
                                Modifier.align(Alignment.CenterVertically)
                                    .weight(1f)
                            ) {
                                DrawName(instrument.name, instrument.shortened)
                                DrawNote(transposeFrequency(hz, -instrument.transposition))
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun PitchGraph(cents: Float, tuner: Tuner?) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.6f)
                .fillMaxHeight()
                .align(Alignment.CenterEnd),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            DrawLines(cents.isNaN())
        }
    }
    PitchPointer(cents, tuner)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PitchPointer(cents: Float, tuner: Tuner?) {
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
            animationSpec = MotionScheme.expressive().fastEffectsSpec(),
        )
    }

    val animatedPosition = remember { Animatable(0.5f) }
    LaunchedEffect(cents) {
        animatedPosition.animateTo(
            targetValue = (if (cents.isNaN()) 0.5f else 0.5f + cents / 50 * -0.5f),
            animationSpec = MotionScheme.expressive().fastEffectsSpec(),
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

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Spacer(modifier = Modifier.weight(1f)) // offset by half of 1 line
        BoxWithConstraints(modifier = Modifier.align(Alignment.CenterHorizontally)
            .fillMaxWidth(0.5f)
            .fillMaxHeight()
            .weight(40f),
        ) {
            val density = LocalDensity.current
            val parentHeightPx = this@BoxWithConstraints.constraints.maxHeight.toFloat()
            val yOffsetPx = parentHeightPx * animatedPosition.value

            val yOffsetDp = with(density) { yOffsetPx.toDp() }
            val yOffsetPaddedDp = yOffsetDp.coerceIn(40.dp, parentHeightPx.pxToDp() - 40.dp)

            Box(
                modifier = Modifier
                    .offset(x = -(80).dp, y = yOffsetPaddedDp - 80.dp)
                    .align(Alignment.TopCenter)
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
                modifier = Modifier.offset(x = 80.dp, y = yOffsetDp - 64.dp)
                    .align(Alignment.TopCenter)
                    .size(64.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            val scale = min(size.width, size.height)
                            val matrix = Matrix()
                            matrix.scale(scale, scale)
                            matrix.translate(0.25f,0.5f)
                            matrix.rotateZ(90f)
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
