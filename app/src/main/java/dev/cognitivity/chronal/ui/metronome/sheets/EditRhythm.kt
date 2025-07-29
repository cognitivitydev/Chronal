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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.MetronomeState
import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.SimpleRhythm
import dev.cognitivity.chronal.activity.editor.Measure
import dev.cognitivity.chronal.activity.editor.Rhythm
import dev.cognitivity.chronal.activity.editor.RhythmEditorActivity
import dev.cognitivity.chronal.activity.editor.RhythmElement
import dev.cognitivity.chronal.activity.editor.RhythmNote
import dev.cognitivity.chronal.activity.editor.RhythmTuplet
import dev.cognitivity.chronal.toPx
import dev.cognitivity.chronal.toSp
import dev.cognitivity.chronal.ui.WavyHorizontalLine
import dev.cognitivity.chronal.ui.WavyVerticalLine
import dev.cognitivity.chronal.ui.metronome.windows.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditRhythm(primary: Boolean, expanded: Boolean, advancedButtonListener: () -> Unit = {} ) {
    var showSimpleWarning by remember { mutableStateOf(false) }

    var hidden by remember { mutableStateOf(false) }
    if(hidden) return

    val enabled = if(primary) true else secondaryEnabled
    Column(
        modifier = Modifier.fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Text(
            text = context.getString(if(primary) R.string.metronome_edit_rhythm_primary else R.string.metronome_edit_rhythm_secondary),
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
            modifier = Modifier
                .padding(16.dp, 16.dp, 16.dp, 8.dp)
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally)
        ) {
            val weight = if(expanded) Modifier else Modifier.weight(1f)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(animatedBackground)
                    .then(weight)
                    .clickable {
                        if (primary) {
                            Toast.makeText(context, context.getString(R.string.metronome_edit_rhythm_primary_disable), Toast.LENGTH_SHORT).show()
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
                Text(context.getString(if(primary) R.string.metronome_edit_rhythm_primary_enabled else R.string.metronome_edit_rhythm_secondary_enable),
                    style = MaterialTheme.typography.titleLarge,
                    color = animatedText,
                    modifier = Modifier
                        .padding(16.dp, 8.dp)
                        .then(weight)
                        .align(Alignment.CenterVertically)
                )
                Switch(
                    checked = if(primary) true else secondaryEnabled,
                    onCheckedChange = { checked ->
                        if(primary) {
                            Toast.makeText(context, context.getString(R.string.metronome_edit_rhythm_primary_disable), Toast.LENGTH_SHORT).show()
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
                    modifier = Modifier
                        .padding(16.dp, 8.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            if(expanded) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
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
                modifier = Modifier
                    .heightIn(ButtonDefaults.MediumContainerHeight)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 64.dp),
                contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                onClick = {
                    advancedButtonListener()
                    ChronalApp.getInstance().startActivity(
                        Intent(context, RhythmEditorActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("isPrimary", primary)
                    )
                },
                enabled = enabled
            ) {
                Text(context.getString(R.string.metronome_edit_rhythm_open_advanced),
                    style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)
                )
            }
            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp),
                onClick = {
                    showSimpleWarning = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(primary) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = if(primary) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onTertiaryContainer
                ),
                enabled = enabled
            ) {
                Text(context.getString(R.string.metronome_edit_rhythm_switch_simple))
            }
        } else {
            Row(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
            ) {
                Text(context.getString(R.string.metronome_edit_rhythm_time_signature),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                )
                Spacer(modifier = Modifier.padding(24.dp, 0.dp))
                Text(context.getString(R.string.metronome_edit_rhythm_beat),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f, matchHeightConstraintsFirst = true)
                            .align(Alignment.Center)
                            .clip(MaterialShapes.Bun.toShape(0))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable {
                                if (primary) {
                                    showTimeSignaturePrimary = true
                                } else {
                                    showTimeSignatureSecondary = true
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(0.75f)
                                .align(Alignment.Center)
                        ) {
                            MusicFont.Number.TimeSignature(simpleRhythm.timeSignature.first, simpleRhythm.timeSignature.second,
                                MaterialTheme.colorScheme.onSurface, lineSpacing = 16.dp)
                        }

                    }
                }
                WavyVerticalLine(
                    modifier = Modifier
                        .padding(24.dp, 0.dp)
                        .height(128.dp)
                        .align(Alignment.CenterVertically)
                )
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f, matchHeightConstraintsFirst = true)
                            .align(Alignment.Center)
                            .clip(MaterialShapes.Cookie4Sided.toShape(0))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable {
                                if (primary) {
                                    showSubdivisionPrimary = true
                                } else {
                                    showSubdivisionSecondary = true
                                }
                            }
                    ) {
                        val subdivision = simpleRhythm.subdivision
                        val isTuplet = (subdivision and (subdivision - 1)) != 0
                        val noteValue = if(!isTuplet) subdivision else (subdivision / (3f / 2f)).toInt()
                        val char = MusicFont.Notation.convert(noteValue)
                        val offset = MusicFont.Notation.entries.find { it.char == char }?.offset ?: Offset(0f, 0f)
                        Box(
                            modifier = Modifier.size(96.dp)
                                .align(Alignment.Center)
                        ) {
                            Text(
                                text = char.toString(),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.bravuratext)),
                                    fontSize = 96.dp.toSp()
                                ),
                                modifier = Modifier.align(Alignment.Center)
                                    .offset(96.dp * offset.x, 96.dp * offset.y)
                                    .offset(0.dp, if(isTuplet) 8.dp else 0.dp)
                            )
                        }
                        if(isTuplet) {
                            Row(
                                modifier = Modifier.align(Alignment.TopCenter)
                                    .padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier.height(1.dp)
                                        .padding(horizontal = 8.dp)
                                        .weight(1f)
                                        .align(Alignment.CenterVertically)
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )
                                Text(
                                    text = "3",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                        .padding(4.dp)
                                )
                                Box(
                                    modifier = Modifier.height(1.dp)
                                        .padding(horizontal = 8.dp)
                                        .weight(1f)
                                        .align(Alignment.CenterVertically)
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }
                    }
                }
            }
            FilledTonalButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = {
                    advancedButtonListener()
                    ChronalApp.getInstance().startActivity(
                        Intent(context, RhythmEditorActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("isPrimary", primary)
                    )
                },
                enabled = enabled
            ) {
                Text(context.getString(R.string.metronome_edit_rhythm_switch_advanced))
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
            title = { Text(context.getString(R.string.metronome_edit_rhythm_simple_warning_title)) },
            text = {
                Text(context.getString(R.string.metronome_edit_rhythm_simple_warning_text))
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
                    advancedButtonListener()
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
        modifier = Modifier
            .clickable(
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
            text = context.getString(R.string.metronome_edit_rhythm_vibration),
            style = MaterialTheme.typography.titleLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun EditTimeSignature(window: Window, primary: Boolean, expanded: Boolean) {
    val setting = if(primary) ChronalApp.getInstance().settings.metronomeSimpleRhythm else ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary
    var value by remember { mutableStateOf(setting.value) }
    if(expanded) {
        Row {
            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                ClockPreview(primary, value)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 16.dp)
            ) {
                EditBeatCount(primary, value) {
                    val oldValue = value
                    value = value.copy(timeSignature = Pair(it, value.timeSignature.second), subdivision = value.timeSignature.second)
                    if(!setRhythm(window, value, primary)) {
                        value = oldValue
                    }
                }
            }
            WavyVerticalLine(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp, 32.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 16.dp)
            ) {
                EditBeatType(primary, value) {
                    val oldValue = value
                    value = value.copy(timeSignature = Pair(value.timeSignature.first, it), subdivision = value.timeSignature.second)
                    if(!setRhythm(window, value, primary)) {
                        value = oldValue
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier
                .padding(bottom = 32.dp)
                .align(Alignment.CenterHorizontally)) {
                ClockPreview(primary, value)
            }
            EditBeatCount(primary, value) {
                val oldValue = value
                value = value.copy(timeSignature = Pair(it, value.timeSignature.second))
                if(!setRhythm(window, value, primary)) {
                    value = oldValue
                }
            }
            WavyHorizontalLine(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp, 16.dp)
            )
            EditBeatType(primary, value) {
                val oldValue = value
                value = value.copy(timeSignature = Pair(value.timeSignature.first, it))
                if(!setRhythm(window, value, primary)) {
                    value = oldValue
                }
            }
        }
    }
}

@Composable
fun BoxScope.ClockPreview(primary: Boolean, value: SimpleRhythm) {
    val timeSignature = value.timeSignature
    val metronome = if(primary) ChronalApp.getInstance().metronome else ChronalApp.getInstance().metronomeSecondary
    val trackColor = MaterialTheme.colorScheme.secondaryContainer

    Box(
        modifier = Modifier
            .size(180.dp)
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
            drawBeats(remember { Animatable(-1f) }, 4.dp.toPx(),
                metronome.getIntervals().filter { it.measure == 0 },
                majorOffColor = MaterialTheme.colorScheme.primaryContainer,
                minorOffColor = MaterialTheme.colorScheme.onPrimary,
                majorPrimaryColor = MaterialTheme.colorScheme.onPrimaryContainer,
                minorPrimaryColor = MaterialTheme.colorScheme.primary,
                surface = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        } else {
            drawBeats(remember { Animatable(-1f) }, 4.dp.toPx(),
                metronome.getIntervals().filter { it.measure == 0 },
                majorOffColor = MaterialTheme.colorScheme.tertiaryContainer,
                minorOffColor = MaterialTheme.colorScheme.onTertiary,
                majorPrimaryColor = MaterialTheme.colorScheme.onTertiaryContainer,
                minorPrimaryColor = MaterialTheme.colorScheme.tertiary,
                surface = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
        Box(modifier = Modifier
            .fillMaxHeight(0.5f)
            .align(Alignment.Center)
        ) {
            MusicFont.Number.TimeSignature(timeSignature.first, timeSignature.second, MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun ColumnScope.EditBeatCount(primary: Boolean, value: SimpleRhythm, onUpdate: (Int) -> Unit = {}) {
    Text(
        text = context.getString(R.string.metronome_edit_rhythm_beat_count),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(0.dp, 0.dp, 0.dp, 16.dp)
    )
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        items(value.timeSignature.first) { i ->
//                Box(
//                    modifier = Modifier.padding(8.dp).size(40.dp).background(MaterialTheme.colorScheme.onSurface)
//                ) {
            BeatShape(i+1)
        }
    }
    Row(
        modifier = Modifier.align(Alignment.CenterHorizontally)
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .width(64.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(100))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable {
                    if (value.timeSignature.first > 1) {
                        onUpdate(value.timeSignature.first - 1)
                    }
                }
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_remove_24),
                contentDescription = context.getString(R.string.generic_subtract),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .padding(16.dp, 8.dp)
                    .align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterVertically)
                .clip(RoundedCornerShape(100))
                .background(if (primary) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = "${value.timeSignature.first}",
                color = if(primary) MaterialTheme.colorScheme.onTertiaryContainer
                else MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .padding(8.dp)
                .width(64.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(100))
                .background(if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
                .clickable {
                    onUpdate(value.timeSignature.first + 1)
                }
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = context.getString(R.string.generic_add),
                tint = if(primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier
                    .padding(16.dp, 8.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun ColumnScope.EditBeatType(primary: Boolean, value: SimpleRhythm, onUpdate: (Int) -> Unit = {}) {
    Text(
        text = "Beat Type",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(0.dp, 0.dp, 0.dp, 16.dp)
    )

    Row(modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .padding(horizontal = 16.dp)) {
        for(i in 0..1) {
            val isSelected = value.timeSignature.second == 2.0.pow(i.toDouble()).toInt()
            val color = if (isSelected) {
                if(primary) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.tertiaryContainer
            } else MaterialTheme.colorScheme.secondaryContainer
            val textColor = if (isSelected) {
                if(primary) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onTertiaryContainer
            } else MaterialTheme.colorScheme.onSecondaryContainer
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
                    .background(color, RoundedCornerShape(8.dp))
                    .clickable {
                        onUpdate(2.0.pow(i.toDouble()).toInt())
                    }
            ) {
                Text(
                    text = MusicFont.Notation.convert(2.0.pow(i.toDouble()).toInt()).toString(),
                    color = textColor,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.bravuratext)),
                        fontSize = 64.dp.toSp()
                    ),
                    modifier = Modifier
                        .offset((-6).dp, 24.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
    Row(modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .padding(horizontal = 16.dp)) {
        for(i in 2..4) {
            val isSelected = value.timeSignature.second == 2.0.pow(i.toDouble()).toInt()
            val color = if (isSelected) {
                if(primary) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.tertiaryContainer
            } else MaterialTheme.colorScheme.secondaryContainer
            val textColor = if (isSelected) {
                if(primary) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onTertiaryContainer
            } else MaterialTheme.colorScheme.onSecondaryContainer
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
                    .background(color, RoundedCornerShape(8.dp))
                    .clickable {
                        onUpdate(2.0.pow(i.toDouble()).toInt())
                    }
            ) {
                Text(
                    text = MusicFont.Notation.convert(2.0.pow(i.toDouble()).toInt()).toString(),
                    color = textColor,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.bravuratext)),
                        fontSize = 64.dp.toSp()
                    ),
                    modifier = Modifier
                        .offset((-6).dp, 24.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BeatShape(beat: Int) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(40.dp)
    ) {
        val color = MaterialTheme.colorScheme.onSurface
        val shape = when(beat) {
            1 -> MaterialShapes.Circle.toShape(0)
            2 -> MaterialShapes.Pill.toShape(0)
            else -> null
        }
        if(shape == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color,
                        RoundedPolygon(numVertices = beat, rounding = CornerRounding(radius = .2f)).normalized()
                            .toShape(-90)
                    )
            ) {
                Text(
                    text = "$beat",
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            return
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color, shape)
        ) {
            Text(
                text = "$beat",
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun EditSubdivision(window: Window, primary: Boolean, expanded: Boolean) {
    val setting = if(primary) ChronalApp.getInstance().settings.metronomeSimpleRhythm else ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary
    var value by remember { mutableStateOf(setting.value) }

    if(!expanded) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(4.dp)
            ) {
                ClockPreview(primary, value)
            }

            LazyHorizontalGrid(
                modifier = Modifier.fillMaxWidth().height(192.dp),
                rows = GridCells.Fixed(2),
            ) {
                items(10) { i ->
                    SubdivisionNote(window, primary, value, i) {
                        value = it
                    }
                }
            }
            EmphasisSelector(window, primary, value) {
                value = it
            }
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.align(Alignment.CenterVertically)
                    .padding(4.dp)
            ) {
                ClockPreview(primary, value)
            }

            LazyVerticalGrid(
                modifier = Modifier.width(192.dp)
                    .fillMaxHeight(),
                columns = GridCells.Fixed(2),
            ) {
                items(10) { i ->
                    SubdivisionNote(window, primary, value, i) {
                        value = it
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxHeight()
                    .width(IntrinsicSize.Min)
            ) {
                EmphasisSelector(window, primary, value) {
                    value = it
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SubdivisionNote(window: Window, primary: Boolean, value: SimpleRhythm, i: Int, onUpdate: (SimpleRhythm) -> Unit = {}) {
    val isTuplet = i >= 6
    val noteValue = if(isTuplet) 2.0.pow(i - 4.0).toInt() else 2.0.pow(i.toDouble()).toInt()
    val tupletValue = if(isTuplet) 3 else 2
    val duration = (noteValue * (tupletValue.toDouble() / 2)).toInt()
    val selected = value.subdivision == duration

    val containerColor = if(selected) {
        if(primary) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.tertiaryContainer
    } else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if(selected) {
        if(primary) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onTertiaryContainer
    } else MaterialTheme.colorScheme.onSecondaryContainer
    Box(
        modifier = Modifier.padding(8.dp)
            .fillMaxHeight()
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .background(containerColor, RoundedCornerShape(8.dp))
            .clickable {
                val oldValue = value
                var newValue = value.copy(subdivision = duration)
                if(!setRhythm(window, newValue, primary, retry = false)) {
                    newValue = oldValue
                }
                onUpdate(newValue)
            },
    ) {
        val char = MusicFont.Notation.convert(noteValue)
        val offset = MusicFont.Notation.entries.find { it.char == char }?.offset ?: Offset(0f, 0f)
        val size = if(isTuplet) 48.dp else 64.dp
        Text(
            text = char.toString(),
            color = textColor,
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.bravuratext)),
                fontSize = size.toSp()
            ),
            modifier = Modifier.align(Alignment.Center)
                .offset(size * offset.x, size * offset.y)
                .offset(0.dp, if(isTuplet) 8.dp else 0.dp)
        )

        if(isTuplet) {
            Row(modifier = Modifier.align(Alignment.TopCenter)) {
                Box(
                    modifier = Modifier.height(1.dp)
                        .padding(horizontal = 4.dp)
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .background(textColor)
                )
                Text(
                    text = "3",
                    color = textColor,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                        .padding(4.dp)
                )
                Box(
                    modifier = Modifier.height(1.dp)
                        .padding(horizontal = 4.dp)
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .background(textColor)
                )
            }
        }
    }
}

@Composable
fun ColumnScope.EmphasisSelector(window: Window, primary: Boolean, value: SimpleRhythm, onUpdate: (SimpleRhythm) -> Unit = {}) {
    Text(context.getString(R.string.metronome_emphasis),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        @Composable
        fun EmphasisButton(
            label: Int,
            selected: Boolean,
            onClick: () -> Unit
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = if (primary) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.tertiary,
                    ),
                    interactionSource = interactionSource
                )
                Text(context.getString(label),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        EmphasisButton(R.string.metronome_emphasis_all, value.emphasis == 0) {
            val newValue = value.copy(emphasis = 0)
            onUpdate(newValue)
            setRhythm(window, newValue, primary, retry = false)
        }
        EmphasisButton(R.string.metronome_emphasis_none, value.emphasis == 1) {
            val newValue = value.copy(emphasis = 1)
            onUpdate(newValue)
            setRhythm(window, newValue, primary, retry = false)
        }
        EmphasisButton(R.string.metronome_emphasis_first, value.emphasis == 2) {
            val newValue = value.copy(emphasis = 2)
            onUpdate(newValue)
            setRhythm(window, newValue, primary, retry = false)
        }
        EmphasisButton(R.string.metronome_emphasis_alternate, value.emphasis == 3) {
            val newValue = value.copy(emphasis = 3)
            onUpdate(newValue)
            setRhythm(window, newValue, primary, retry = false)
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
            Toast.makeText(context, context.getString(R.string.metronome_edit_rhythm_error_other), Toast.LENGTH_SHORT).show()
            return false
        } else {
            val result = setRhythm(window, value, primary, retry = false)
            if(result) {
                Toast.makeText(context, context.getString(R.string.metronome_edit_rhythm_error_adjusted), Toast.LENGTH_SHORT).show()
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