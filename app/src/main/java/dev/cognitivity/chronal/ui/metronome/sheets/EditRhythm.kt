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

package dev.cognitivity.chronal.ui.metronome.sheets

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.MetronomeState
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.SimpleRhythm
import dev.cognitivity.chronal.activity.PresetActivity
import dev.cognitivity.chronal.activity.RhythmEditorActivity
import dev.cognitivity.chronal.activity.SimpleEditorActivity
import dev.cognitivity.chronal.rhythm.metronome.Measure
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmElement
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmNote
import dev.cognitivity.chronal.rhythm.metronome.elements.StemDirection
import dev.cognitivity.chronal.ui.metronome.windows.vibratePrimary
import dev.cognitivity.chronal.ui.metronome.windows.vibrateSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditRhythm(primary: Boolean, expanded: Boolean, onDismiss: () -> Unit = {}) {
    val metronome = ChronalApp.getInstance().metronome
    var showSimpleWarning by remember { mutableStateOf(false) }

    var hidden by remember { mutableStateOf(false) }
    if(hidden) return

    var secondaryEnabled by remember { mutableStateOf(metronome.getTrack(1).enabled) }
    val enabled = if(primary) true else secondaryEnabled
    Column(
        modifier = Modifier.fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Text(
            text = context.getString(if(primary) R.string.simple_editor_primary else R.string.simple_editor_secondary),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp)
        )

        val animatedBackground by animateColorAsState(
            targetValue = if(primary) MaterialTheme.colorScheme.primaryContainer
            else if(secondaryEnabled) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.inverseOnSurface,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "animatedBackground"
        )
        val animatedText by animateColorAsState(
            targetValue = if(primary) MaterialTheme.colorScheme.onPrimaryContainer
            else if(secondaryEnabled) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "animatedText"
        )
        Row(
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally)
        ) {
            val weight = if(expanded) Modifier else Modifier.weight(1f)
            Row(
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    .background(animatedBackground)
                    .then(weight)
                    .clickable {
                        if (primary) {
                            Toast.makeText(context, context.getString(R.string.simple_editor_primary_disable), Toast.LENGTH_SHORT).show()
                            return@clickable
                        }
                        secondaryEnabled = !secondaryEnabled
                        onSwitch(secondaryEnabled)
                    }
            ) {
                Text(context.getString(if(primary) R.string.simple_editor_primary_enabled else R.string.simple_editor_secondary_enable),
                    style = MaterialTheme.typography.titleLarge,
                    color = animatedText,
                    modifier = Modifier.padding(16.dp, 8.dp)
                        .then(weight)
                        .align(Alignment.CenterVertically)
                )
                Switch(
                    checked = if(primary) true else secondaryEnabled,
                    onCheckedChange = { checked ->
                        if (primary) {
                            Toast.makeText(context, context.getString(R.string.simple_editor_primary_disable), Toast.LENGTH_SHORT).show()
                            return@Switch
                        }
                        secondaryEnabled = checked
                        onSwitch(secondaryEnabled)
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = if(primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                        checkedThumbColor = if(primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier.padding(16.dp, 8.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            if(expanded) {
                Box(
                    modifier = Modifier.align(Alignment.CenterVertically)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Vibration(primary, enabled)
                }
            }
        }
        if(!expanded) {
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Vibration(primary, enabled)
            }
        }
        var simpleRhythm by remember {
            mutableStateOf(if(primary) ChronalApp.getInstance().settings.metronomeSimpleRhythm.value else ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary.value)
        }
        val metronome = ChronalApp.getInstance().metronome
        metronome.getTracks().forEach {
            it.setEditListener(2) {
                simpleRhythm = if (primary) ChronalApp.getInstance().settings.metronomeSimpleRhythm.value
                    else ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary.value
            }
        }
        val isAdvanced = simpleRhythm == SimpleRhythm(0 to 0, 0, 0)
        if(isAdvanced) {
            FilledTonalButton(
                modifier = Modifier.heightIn(ButtonDefaults.MediumContainerHeight)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 48.dp),
                contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                onClick = {
                    onDismiss()
                    ChronalApp.getInstance().startActivity(
                        Intent(context, RhythmEditorActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("isPrimary", primary)
                    )
                },
                enabled = enabled
            ) {
                Text(context.getString(R.string.simple_editor_open_advanced),
                    style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (primary) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = if (primary) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    onClick = {
                        onDismiss()
                        ChronalApp.getInstance().startActivity(
                            Intent(context, PresetActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    enabled = enabled
                ) {
                    Text(context.getString(R.string.simple_editor_view_presets))
                }
                Button(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = {
                        showSimpleWarning = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (primary) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = if (primary) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    enabled = enabled
                ) {
                    Text(context.getString(R.string.simple_editor_switch_simple))
                }
            }
        } else {
            Button(
                modifier = Modifier.heightIn(ButtonDefaults.MediumContainerHeight)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 48.dp),
                contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                onClick = {
                    onDismiss()
                    ChronalApp.getInstance().startActivity(
                        Intent(context, SimpleEditorActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("isPrimary", primary)
                    )
                },
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (primary) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.tertiary,
                    contentColor = if (primary) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Text(context.getString(R.string.simple_editor_open_simple),
                    style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(primary) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = if(primary) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    onClick = {
                        onDismiss()
                        ChronalApp.getInstance().startActivity(
                            Intent(context, PresetActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    enabled = enabled
                ) {
                    Text(context.getString(R.string.simple_editor_view_presets))
                }
                FilledTonalButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = {
                        onDismiss()
                        ChronalApp.getInstance().startActivity(
                            Intent(context, RhythmEditorActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra("isPrimary", primary)
                        )
                    },
                    enabled = enabled
                ) {
                    Text(context.getString(R.string.simple_editor_switch_advanced))
                }
            }
        }
    }

    if(showSimpleWarning) {
        val metronome = ChronalApp.getInstance().metronome

        val primaryTrack = metronome.getTrack(0)
        val secondaryTrack = metronome.getTrack(1)

        val selectedTrack = if(primary) primaryTrack else secondaryTrack

        val rhythm = selectedTrack.getRhythm()
        val scope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { showSimpleWarning = false },
            icon = { Icon(painter = painterResource(R.drawable.outline_warning_24), contentDescription = context.getString(R.string.generic_warning)) },
            title = { Text(context.getString(R.string.simple_editor_simple_warning_title)) },
            text = {
                Text(context.getString(R.string.simple_editor_simple_warning_text))
            },
            confirmButton = {
                TextButton(onClick = {
                    showSimpleWarning = false
                    selectedTrack.beatValue = 4f
                    selectedTrack.setRhythm(Rhythm(listOf(
                        Measure(rhythm.measures[0].timeSig,
                            arrayListOf<RhythmElement>().apply {
                                repeat(rhythm.measures[0].timeSig.first) {
                                    add(RhythmNote(
                                        stemDirection = StemDirection.UP,
                                        baseDuration = 1.0 / rhythm.measures[0].timeSig.second,
                                        dots = 0
                                    ))
                                }
                            }
                        )
                    )))

                    ChronalApp.getInstance().settings.metronomeState.value = MetronomeState(
                        bpm = selectedTrack.bpm, beatValuePrimary = primaryTrack.beatValue,
                        beatValueSecondary = secondaryTrack.beatValue, secondaryEnabled = secondaryEnabled
                    )

                    if(primary) {
                        ChronalApp.getInstance().settings.metronomeRhythm.value = selectedTrack.getRhythm().serialize()
                        ChronalApp.getInstance().settings.metronomeSimpleRhythm.value = SimpleRhythm(rhythm.measures[0].timeSig,
                            rhythm.measures[0].timeSig.second, 0)
                    } else {
                        ChronalApp.getInstance().settings.metronomeRhythmSecondary.value = selectedTrack.getRhythm().serialize()
                        ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary.value = SimpleRhythm(rhythm.measures[0].timeSig,
                            rhythm.measures[0].timeSig.second, 0)
                    }
                    scope.launch {
                        ChronalApp.getInstance().settings.save()
                    }
                    onDismiss()
                }) {
                    Text(context.getString(R.string.generic_switch))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimpleWarning = false }) {
                    Text(context.getString(R.string.generic_cancel))
                }
            }
        )
    }
}

@Composable
fun Vibration(primary: Boolean, enabled: Boolean) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            if (!enabled) return@clickable
            if (primary) {
                vibratePrimary = !vibratePrimary
                ChronalApp.getInstance().settings.metronomeVibrations.value = vibratePrimary
            } else {
                vibrateSecondary = !vibrateSecondary
                ChronalApp.getInstance().settings.metronomeVibrationsSecondary.value = vibrateSecondary
            }
        }
    ) {
        Checkbox(
            checked = if (primary) vibratePrimary else vibrateSecondary,
            enabled = enabled,
            interactionSource = interactionSource,
            onCheckedChange = { checked ->
                if (!enabled) return@Checkbox
                if (primary) {
                    vibratePrimary = checked
                    ChronalApp.getInstance().settings.metronomeVibrations.value = checked
                } else {
                    vibrateSecondary = checked
                    ChronalApp.getInstance().settings.metronomeVibrationsSecondary.value = checked
                }
            },
            colors = CheckboxDefaults.colors(
                checkedColor = if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Text(
            text = context.getString(R.string.simple_editor_vibration),
            style = MaterialTheme.typography.titleLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

private fun onSwitch(enabled: Boolean) {
    val metronome = ChronalApp.getInstance().metronome
    val primaryTrack = metronome.getTrack(0)

    metronome.getTrack(1).enabled = enabled

    val secondaryTrack = metronome.getTrack(1)

    ChronalApp.getInstance().settings.metronomeState.value = MetronomeState(
        bpm = primaryTrack.bpm, beatValuePrimary = primaryTrack.beatValue,
        beatValueSecondary = secondaryTrack.beatValue, secondaryEnabled = enabled,
    )

    CoroutineScope(Dispatchers.Main).launch {
        ChronalApp.getInstance().settings.save()
    }
}