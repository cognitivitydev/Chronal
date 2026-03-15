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

package dev.cognitivity.chronal.ui.metronome.components

import android.content.Intent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.BeatDetectorActivity
import dev.cognitivity.chronal.activity.EditSounds
import dev.cognitivity.chronal.activity.FullscreenActivity
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.ui.MorphedShape
import dev.cognitivity.chronal.ui.metronome.MetronomeViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayButton(mainActivity: MainActivity, viewModel: MetronomeViewModel, onClick: (Boolean) -> Unit, modifier: Modifier = Modifier) {
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
        targetValue = if(viewModel.playing.value) 0.6f else 1f,
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
        targetValue = if (!viewModel.playing.value) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
        animationSpec = MotionScheme.expressive().defaultSpatialSpec(),
        label = "animatedColor"
    )

    Box(
        modifier = modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
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
                modifier = Modifier.align(Alignment.Center)
                    .offset(
                        x = 32.dp,
                        y = (-32).dp
                    )
            ) {
                DropdownMenu(
                    expanded = viewModel.settingsExpanded.value,
                    onDismissRequest = {
                        viewModel.setSettingsExpanded(false)
                    },
                    modifier = Modifier.zIndex(1f)
                ) {
                    DropdownItem(viewModel, R.string.metronome_option_fullscreen_mode, painterResource(R.drawable.baseline_fullscreen_24)) {
                        mainActivity.runActivity(FullscreenActivity::class.java)
                    }
                    DropdownItem(viewModel, R.string.metronome_option_beat_detector, painterResource(R.drawable.outline_music_cast_24)) {
                        mainActivity.runActivity(BeatDetectorActivity::class.java)
                    }
                    DropdownItem(viewModel, R.string.metronome_option_tap_tempo, painterResource(R.drawable.outline_timer_24)) {
                        viewModel.setShowTapTempo(true)
                    }
                    DropdownItem(viewModel, R.string.metronome_option_change_sounds, painterResource(R.drawable.outline_volume_up_24)) {
                        mainActivity.runActivity(EditSounds::class.java)
                    }
                    DropdownItem(viewModel, R.string.metronome_option_play_audio, painterResource(R.drawable.outline_audio_file_24)) {
                        mainActivity.fileActivity.launch(Intent().apply {
                            action = Intent.ACTION_GET_CONTENT
                            type = "audio/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        })
                    }
                }
            }
        }

        Box(
            modifier = Modifier.wrapContentHeight()
        ) {
            Box(
                modifier = Modifier.size(120.dp, 56.dp)
                    .offset(x = (-60).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .align(Alignment.Center)
                    .clickable {
                        viewModel.setShowTapTempo(true)
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_timer_24),
                    contentDescription = context.getString(R.string.metronome_option_tap_tempo),
                    modifier = Modifier.size(32.dp)
                        .offset(x = (12).dp)
                        .align(Alignment.CenterStart),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier.size(120.dp, 56.dp)
                    .offset(x = 60.dp)
                    .clip(CircleShape)
                    .background(
                        animateColorAsState(
                            targetValue = if(viewModel.settingsExpanded.value) MaterialTheme.colorScheme.surfaceContainerHigh
                            else MaterialTheme.colorScheme.surfaceContainer,
                            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
                            label = "dropdownBackground"
                        ).value
                    )
                    .align(Alignment.Center)
                    .clickable {
                        viewModel.setSettingsExpanded(!viewModel.settingsExpanded.value)
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_more_horiz_24),
                    contentDescription = context.getString(R.string.generic_more_options),
                    modifier = Modifier.size(32.dp)
                        .offset(x = (-12).dp)
                        .align(Alignment.CenterEnd),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier.requiredSizeIn(maxWidth = 96.dp, maxHeight = 96.dp)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
                    .clip(
                        MorphedShape(
                            morph,
                            morphProgress,
                            animatedRotation.value
                        )
                    )
                    .background(animatedColor)
                    .clickable {
                        onClick(!viewModel.playing.value)
                    }
                    .zIndex(2f)
            ) {
                PlayPauseIcon(
                    paused = !viewModel.playing.value,
                    modifier = Modifier.size(48.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun DropdownItem(viewModel: MetronomeViewModel, name: Int, icon: Painter, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(context.getString(name)) },
        onClick = {
            viewModel.setSettingsExpanded(false)
            onClick()
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