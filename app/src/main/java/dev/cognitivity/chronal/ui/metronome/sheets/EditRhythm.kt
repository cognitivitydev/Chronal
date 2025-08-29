package dev.cognitivity.chronal.ui.metronome.sheets

import android.content.Intent
import android.view.Window
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.MetronomeState
import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.SimpleRhythm
import dev.cognitivity.chronal.activity.PresetActivity
import dev.cognitivity.chronal.activity.RhythmEditorActivity
import dev.cognitivity.chronal.activity.SimpleEditorActivity
import dev.cognitivity.chronal.rhythm.metronome.Measure
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmElement
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmNote
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmTuplet
import dev.cognitivity.chronal.toPx
import dev.cognitivity.chronal.ui.metronome.windows.ClockBeats
import dev.cognitivity.chronal.ui.metronome.windows.metronome
import dev.cognitivity.chronal.ui.metronome.windows.metronomeSecondary
import dev.cognitivity.chronal.ui.metronome.windows.paused
import dev.cognitivity.chronal.ui.metronome.windows.secondaryEnabled
import dev.cognitivity.chronal.ui.metronome.windows.updateSleepMode
import dev.cognitivity.chronal.ui.metronome.windows.vibratePrimary
import dev.cognitivity.chronal.ui.metronome.windows.vibrateSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditRhythm(primary: Boolean, expanded: Boolean, onDismiss: () -> Unit = {} ) {
    var showSimpleWarning by remember { mutableStateOf(false) }

    var hidden by remember { mutableStateOf(false) }
    if(hidden) return

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
                        metronomeSecondary.active = secondaryEnabled
                        ChronalApp.getInstance().settings.metronomeState.value = MetronomeState(
                            bpm = metronome.bpm, beatValuePrimary = metronome.beatValue,
                            beatValueSecondary = metronomeSecondary.beatValue, secondaryEnabled = secondaryEnabled
                        )

                        CoroutineScope(Dispatchers.Main).launch {
                            ChronalApp.getInstance().settings.save()
                        }
                        if (!secondaryEnabled) {
                            metronomeSecondary.stop()
                        }
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
                        if(primary) {
                            Toast.makeText(context, context.getString(R.string.simple_editor_primary_disable), Toast.LENGTH_SHORT).show()
                            return@Switch
                        }
                        secondaryEnabled = checked
                        metronomeSecondary.active = checked
                        if(!checked) {
                            metronomeSecondary.stop()
                        }
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
        val selectedMetronome = if(primary) ChronalApp.getInstance().metronome else ChronalApp.getInstance().metronomeSecondary
        selectedMetronome.setEditListener(2) {
            simpleRhythm = if(primary) ChronalApp.getInstance().settings.metronomeSimpleRhythm.value
                else ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary.value
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
        val metronome = if(primary) ChronalApp.getInstance().metronome else ChronalApp.getInstance().metronomeSecondary
        val rhythm = metronome.getRhythm()
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
                    metronome.beatValue = 4f
                    metronome.setRhythm(Rhythm(listOf(
                        Measure(rhythm.measures[0].timeSig,
                            arrayListOf<RhythmElement>().apply {
                                repeat(rhythm.measures[0].timeSig.first) {
                                    add(
                                        RhythmNote(
                                            display = MusicFont.Notation.convert(rhythm.measures[0].timeSig.second, false).toString(),
                                            isRest = false,
                                            isInverted = false,
                                            duration = 1.0 / rhythm.measures[0].timeSig.second,
                                            dots = 0
                                        )
                                    )
                                }
                            }
                        )
                    )))

                    ChronalApp.getInstance().settings.metronomeState.value = MetronomeState(
                        bpm = ChronalApp.getInstance().metronome.bpm, beatValuePrimary = ChronalApp.getInstance().metronome.beatValue,
                        beatValueSecondary = metronomeSecondary.beatValue, secondaryEnabled = secondaryEnabled
                    )

                    if(primary) {
                        ChronalApp.getInstance().settings.metronomeRhythm.value = metronome.getRhythm().serialize()
                        ChronalApp.getInstance().settings.metronomeSimpleRhythm.value = SimpleRhythm(rhythm.measures[0].timeSig,
                            rhythm.measures[0].timeSig.second, 0)
                    } else {
                        ChronalApp.getInstance().settings.metronomeRhythmSecondary.value = metronome.getRhythm().serialize()
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

@Composable
fun BoxScope.ClockPreview(primary: Boolean, value: SimpleRhythm) {
    val timeSignature = value.timeSignature
    val metronome = if(primary) ChronalApp.getInstance().metronome else ChronalApp.getInstance().metronomeSecondary
    val trackColor = MaterialTheme.colorScheme.secondaryContainer

    Box(
        modifier = Modifier.size(180.dp)
            .align(Alignment.Center)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val trackSize = 4.dp.toPx()
            val radius = (size.minDimension / 2) - trackSize / 2
            val center = Offset(size.width / 2, size.height / 2)

            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = trackSize)
            )
        }
        if(primary) {
            ClockBeats(remember { Animatable(-1f) }, 4.dp.toPx(),
                metronome.getIntervals().filter { it.measure == 0 },
                majorOffColor = MaterialTheme.colorScheme.primaryContainer,
                minorOffColor = MaterialTheme.colorScheme.onPrimary,
                majorPrimaryColor = MaterialTheme.colorScheme.onPrimaryContainer,
                minorPrimaryColor = MaterialTheme.colorScheme.primary,
                surface = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        } else {
            ClockBeats(remember { Animatable(-1f) }, 4.dp.toPx(),
                metronome.getIntervals().filter { it.measure == 0 },
                majorOffColor = MaterialTheme.colorScheme.tertiaryContainer,
                minorOffColor = MaterialTheme.colorScheme.onTertiary,
                majorPrimaryColor = MaterialTheme.colorScheme.onTertiaryContainer,
                minorPrimaryColor = MaterialTheme.colorScheme.tertiary,
                surface = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
        Box(
            modifier = Modifier.fillMaxHeight(0.5f)
                .align(Alignment.Center)
        ) {
            MusicFont.Number.TimeSignature(timeSignature.first, timeSignature.second,
                color = if(primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
        }
    }
}


fun setRhythm(window: Window, value: SimpleRhythm, primary: Boolean, retry: Boolean = true): Boolean {
    val metronome = if(primary) ChronalApp.getInstance().metronome else ChronalApp.getInstance().metronomeSecondary

    val timeSignature = value.timeSignature
    val subdivision = value.subdivision
    val isTuplet = (subdivision and (subdivision - 1)) != 0
    val noteValue = if(!isTuplet) subdivision else (subdivision / (3f / 2f)).toInt()
    val duration = 1.0 / subdivision
    val measureDuration = timeSignature.first / timeSignature.second.toDouble()

    var remaining = measureDuration
    var emphasizeNext = value.emphasis != 1
    val newMeasure = Measure(timeSignature, arrayListOf<RhythmElement>().apply {
        while(remaining > 1e-6) {
            if(isTuplet) {
                add(RhythmTuplet(
                    ratio = 3 to 2,
                    notes = ArrayList<RhythmNote>().apply {
                        for(i in 0 until 3) {
                            if(remaining <= 0) break
                            val note = MusicFont.Notation.convert(noteValue, false).toString()
                            add(RhythmNote(
                                display = MusicFont.Notation.setEmphasis(note, emphasizeNext),
                                isRest = false,
                                isInverted = !emphasizeNext,
                                duration = duration,
                                dots = 0
                            ))
                            remaining -= duration
                            emphasizeNext = when (value.emphasis) {
                                0 -> true
                                3 -> !emphasizeNext
                                else -> false
                            }
                        }
                    }
                ))
            } else {
                val note = MusicFont.Notation.convert(noteValue, false).toString()
                add(RhythmNote(
                    display = MusicFont.Notation.setEmphasis(note, emphasizeNext),
                    isRest = false,
                    isInverted = !emphasizeNext,
                    duration = duration,
                    dots = 0
                ))
                remaining -= duration
                emphasizeNext = when (value.emphasis) {
                    0 -> true
                    3 -> !emphasizeNext
                    else -> false
                }
            }
        }
    })
    if(remaining < -1e-6) {
        if(!retry) {
            Toast.makeText(context, context.getString(R.string.simple_editor_error_other), Toast.LENGTH_SHORT).show()
            return false
        } else {
            val result = setRhythm(window, value, primary, retry = false)
            if(result) {
                Toast.makeText(context, context.getString(R.string.simple_editor_error_adjusted), Toast.LENGTH_SHORT).show()
            }
            return result
        }
    }

    val newRhythm = Rhythm(listOf(newMeasure))
    if(primary) {
        ChronalApp.getInstance().settings.metronomeRhythm.value = newRhythm.serialize()
        ChronalApp.getInstance().settings.metronomeSimpleRhythm.value = value
    } else {
        ChronalApp.getInstance().settings.metronomeRhythmSecondary.value = newRhythm.serialize()
        ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary.value = value
    }
    CoroutineScope(Dispatchers.IO).launch {
        ChronalApp.getInstance().settings.save()
    }
    paused = true
    updateSleepMode(window)
    ChronalApp.getInstance().metronome.stop()
    ChronalApp.getInstance().metronomeSecondary.stop()
    metronome.setRhythm(newRhythm)
    return true
}