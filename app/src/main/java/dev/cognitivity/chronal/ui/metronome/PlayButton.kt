package dev.cognitivity.chronal.ui.metronome

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.rectangle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.BeatDetectorActivity
import dev.cognitivity.chronal.activity.EditSounds
import dev.cognitivity.chronal.activity.FullscreenActivity
import dev.cognitivity.chronal.ui.MorphedShape
import dev.cognitivity.chronal.ui.metronome.windows.activity
import dev.cognitivity.chronal.ui.metronome.windows.dropdownExpanded
import dev.cognitivity.chronal.ui.metronome.windows.metronome
import dev.cognitivity.chronal.ui.metronome.windows.metronomeSecondary
import dev.cognitivity.chronal.ui.metronome.windows.paused
import dev.cognitivity.chronal.ui.metronome.windows.showTempoTapper

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColumnScope.PlayButton(weight: Float) {
    val shapeA = remember {
        RoundedPolygon.star(9, rounding = CornerRounding(0.2f), radius = 1.8f)
    }
    val shapeB = remember {
        RoundedPolygon.circle(9)
    }
    val morph = remember {
        Morph(shapeA, shapeB)
    }

    val morphProgress by animateFloatAsState(
        targetValue = if (!paused) 0.6f else 1f,
        animationSpec = MotionScheme.expressive().defaultSpatialSpec(),
        label = "morphProgress"
    )

    val infiniteTransition = rememberInfiniteTransition("infiniteTransition")
    val animatedRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animatedRotation"
    )

    val animatedColor by animateColorAsState(
        targetValue = if (paused) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.tertiary,
        animationSpec = MotionScheme.expressive().defaultSpatialSpec(),
        label = "animatedColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.CenterHorizontally)
            .weight(weight)
            .zIndex(12f),
        contentAlignment = Alignment.Center
    ) {
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shapes = MaterialTheme.shapes.copy(
                extraSmall = RoundedCornerShape(16.dp),
            )
        ) {
            Box(
                modifier = Modifier.align(Alignment.Center).offset(
                    x = 32.dp,
                    y = (-32).dp
                )
            ) {
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = {
                        dropdownExpanded = false
                    },
                    modifier = Modifier.zIndex(1f)
                ) {
                    DropdownItem(R.string.metronome_option_fullscreen_mode, painterResource(R.drawable.baseline_fullscreen_24)) {
                        activity.runActivity(FullscreenActivity::class.java)
                    }
                    DropdownItem(R.string.metronome_option_beat_detector, painterResource(R.drawable.outline_music_cast_24)) {
                        activity.runActivity(BeatDetectorActivity::class.java)
                    }
                    DropdownItem(R.string.metronome_option_tap_tempo, painterResource(R.drawable.outline_timer_24)) {
                        showTempoTapper = true
                    }
                    DropdownItem(R.string.metronome_option_change_sounds, painterResource(R.drawable.outline_volume_up_24)) {
                        activity.runActivity(EditSounds::class.java)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        ) {
            Spacer(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
            )

            Spacer(
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxHeight()
            )
        }
        Box(
            modifier = Modifier
                .size(120.dp, 56.dp)
                .offset(x = (-60).dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .align(Alignment.Center)
                .clickable {
                    showTempoTapper = true
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.outline_timer_24),
                contentDescription = context.getString(R.string.metronome_option_tap_tempo),
                modifier = Modifier
                    .size(32.dp)
                    .offset(x = (12).dp)
                    .align(Alignment.CenterStart),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Box(
            modifier = Modifier
                .size(120.dp, 56.dp)
                .offset(x = 60.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    animateColorAsState(
                        targetValue = if (dropdownExpanded) MaterialTheme.colorScheme.surfaceContainerHigh
                            else MaterialTheme.colorScheme.surfaceContainer,
                        animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
                        label = "dropdownBackground"
                    ).value
                )
                .align(Alignment.Center)
                .clickable {
                    dropdownExpanded = !dropdownExpanded
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_more_horiz_24),
                contentDescription = context.getString(R.string.generic_more_options),
                modifier = Modifier
                    .size(32.dp)
                    .offset(x = (-12).dp)
                    .align(Alignment.CenterEnd),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Box(
            modifier = Modifier
                .clip(
                    MorphedShape(
                        morph,
                        morphProgress,
                        animatedRotation.value
                    )
                )
                .size(96.dp)
                .align(Alignment.Center)
                .background(animatedColor)
                .clickable {
                    paused = !paused

                    if (paused) {
                        metronome.stop()
                        metronomeSecondary.stop()
                    }
                    else {
                        metronome.start()
                        metronomeSecondary.start()
                    }
                }
                .zIndex(2f)
        ) {
            PlayPauseIcon(
                paused = paused,
                modifier = Modifier.size(48.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayPauseIcon(paused: Boolean, playColor: Color = MaterialTheme.colorScheme.onTertiary, pauseColor: Color = MaterialTheme.colorScheme.onSurfaceVariant, modifier: Modifier = Modifier) {
    val animatedColor by animateColorAsState(
        targetValue = if (paused) pauseColor else playColor,
        animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
        label = "animatedColor"
    )

    val playProgress = remember { Animatable(0f) }

    LaunchedEffect(paused) {
        playProgress.animateTo(
            targetValue = if (paused) 0f else 1f,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
        )
    }

    Box(
        modifier = Modifier.then(modifier)
            .drawWithCache {
                val barWidth = size.width / 4.5f
                val barHeight = size.height / 1.333f

                val square = RoundedPolygon.rectangle(
                    width = barWidth,
                    height = barHeight,
                    centerX = barWidth * 1.5f - 9,
                    centerY = barHeight / 1.5f,
                    rounding = CornerRounding(0f)
                )

                val square2 = RoundedPolygon.rectangle(
                    width = barWidth,
                    height = barHeight,
                    centerX = barWidth * 3.5f - 9,
                    centerY = barHeight / 1.5f,
                    rounding = CornerRounding(0f)
                )

                val triangle = RoundedPolygon(
                    numVertices = 3,
                    radius = size.minDimension / 2f,
                    centerX = size.width / 2f - 10,
                    centerY = size.height / 2f,
                    rounding = CornerRounding(
                        size.minDimension / 10f,
                        smoothing = 0.1f
                    )
                )

                val triangle2 = RoundedPolygon(
                    numVertices = 3,
                    radius = size.minDimension / 3f,
                    centerX = size.width / 2f - 10,
                    centerY = size.height / 2f,
                    rounding = CornerRounding(
                        size.minDimension / 10f,
                        smoothing = 0.1f
                    )
                )

                val morphPath = Morph(start = triangle, end = square)
                    .toPath(progress = playProgress.value)
                    .asComposePath()

                val morphPath2 = Morph(start = triangle2, end = square2)
                    .toPath(progress = playProgress.value)
                    .asComposePath()

                onDrawBehind {
                    drawPath(morphPath, color = animatedColor)
                    drawPath(morphPath2, color = animatedColor)
                }
            }
    )
}

@Composable
fun DropdownItem(name: Int, icon: Painter, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(context.getString(name)) },
        onClick = {
            onClick()
//            dropdownExpanded = false
        },
        leadingIcon = {
            Icon(
                painter = icon,
                contentDescription = context.getString(name),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    )
}