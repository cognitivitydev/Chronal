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

import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.round
import dev.cognitivity.chronal.toSp
import dev.cognitivity.chronal.ui.metronome.MetronomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

private enum class PrecisionOption(val label: String, val decimals: Int?) {
    Auto(context.getString(R.string.metronome_input_tap_precision_auto), null),
    Zero(context.getString(R.string.metronome_input_tap_precision_0), 0),
    One(context.getString(R.string.metronome_input_tap_precision_1), 1),
    Two(context.getString(R.string.metronome_input_tap_precision_2), 2),
}

private data class DisplayBpm(val bpm: Float, val whole: String, val fraction: String?)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalTextApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TapTab(viewModel: MetronomeViewModel) {
    var bpm by remember { mutableFloatStateOf(0f) }
    var finished by remember { mutableStateOf(false) }
    var precisionOption by remember { mutableStateOf(PrecisionOption.Auto) }
    val scope = rememberCoroutineScope()
    val intervals by viewModel.intervals.collectAsState()

    val decimals = getDecimals(intervals, precisionOption)
    val displayBpm = formatBpm(bpm.round(decimals), decimals)

    PrecisionSelectorRow(
        selected = precisionOption,
        onSelected = { precisionOption = it },
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp, 20.dp, 8.dp, 8.dp))
            .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
    )

    TapTempoPad(viewModel, displayBpm, finished,
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp, 8.dp, 20.dp, 20.dp))
            .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        finished = false
        handleTap(
            viewModel = viewModel,
            onBpmUpdated = {
                bpm = it

                scope.launch {
                    val tapAtScheduleTime = viewModel.lastTapTime.value
                    delay((60000L / bpm * 5).toLong())
                    if (tapAtScheduleTime == viewModel.lastTapTime.value) {
                        val decimals = getDecimals(intervals, precisionOption)
                        viewModel.setBpm(bpm.round(decimals))

                        viewModel.setIntervals(emptyList())
                        viewModel.setLastTapTime(0)
                        bpm = 0f
                        finished = true
                        delay(2000L)
                        if(viewModel.lastTapTime.value == 0L) finished = false
                    }
                }
            }
        )
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibration = VibrationEffect.createOneShot(2, 255)
            vibratorManager.vibrate(CombinedVibration.createParallel(vibration))
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(2)
        }
    }
}

private fun handleTap(viewModel: MetronomeViewModel, onBpmUpdated: (Float) -> Unit) {
    val now = System.nanoTime()
    val previousTap = viewModel.lastTapTime.value
    if (previousTap != 0L) viewModel.addInterval(now - previousTap)
    viewModel.setLastTapTime(now)

    val rawBpm = calculateBpm(viewModel.intervals.value) ?: return
    onBpmUpdated(rawBpm)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalTextApi::class)
@Composable
private fun TapTempoPad(viewModel: MetronomeViewModel, displayBpm: DisplayBpm, finished: Boolean, modifier: Modifier = Modifier, onTap: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val secondary = MaterialTheme.colorScheme.tertiary
    val onSecondary = MaterialTheme.colorScheme.onTertiary
    val fastEffectsSpec = MaterialTheme.motionScheme.slowEffectsSpec<Color>()

    val animatedBackground = remember { Animatable(primary) }
    val animatedForeground = remember { Animatable(onPrimary) }

    val lastTapTime by viewModel.lastTapTime.collectAsState()
    LaunchedEffect(lastTapTime) {
        if(viewModel.lastTapTime.value == 0L) return@LaunchedEffect
        animatedBackground.snapTo(secondary)
        animatedBackground.animateTo(primary, animationSpec = fastEffectsSpec)
    }
    LaunchedEffect(lastTapTime) {
        if(viewModel.lastTapTime.value == 0L) return@LaunchedEffect
        animatedForeground.snapTo(onSecondary)
        animatedForeground.animateTo(onPrimary, animationSpec = fastEffectsSpec)
    }

    BoxWithConstraints(modifier = modifier) {
        val screenHeight = LocalWindowInfo.current.containerDpSize.height
        val size = minOf(320.dp, maxWidth, screenHeight * 0.425f)

        Surface(
            color = animatedBackground.value,
            shape = MaterialShapes.Cookie6Sided.toShape(0),
            modifier = Modifier.align(Alignment.Center)
                .size(size)
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        onTap()
                    })
                },
        ) {
            val fontSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (size / 4).toSp()
            } else {
                val visualLength = displayBpm.whole.length + (displayBpm.fraction?.length ?: 0) * 0.5f
                val widthScale = 1f - ((visualLength - 2).coerceAtLeast(0f) * 0.25f)
                (size / 4).toSp() * widthScale
            }

            TapTempoContent(displayBpm, fontSize, animatedForeground.value, viewModel, finished)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun TapTempoContent(displayBpm: DisplayBpm, fontSize: TextUnit, color: Color, viewModel: MetronomeViewModel, finished: Boolean) {
    val active = displayBpm.bpm != 0f
    val tapping = viewModel.lastTapTime.collectAsState().value != 0L

    val roundedFontFamily = FontFamily(
        Font(
            resId = R.font.google_sans_flex_variable,
            variationSettings = FontVariation.Settings(FontVariation.Setting("ROND", 100f))
        )
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if(finished) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(80.dp)
            )
        } else {
            if(active) {
                Row {
                    Text(
                        text = displayBpm.whole,
                        fontFamily = roundedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = color,
                        modifier = Modifier.alignByBaseline()
                    )
                    displayBpm.fraction?.let { fraction ->
                        Text(
                            text = ".${fraction}",
                            fontFamily = roundedFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = fontSize / 2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = color.copy(alpha = 0.85f),
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }
            } else {
                Icon(
                    painter = painterResource(R.drawable.baseline_touch_triple_24),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(64.dp)
                )
            }
            Text(
                text = context.getString(
                    if(active) R.string.metronome_bpm
                    else if(tapping) R.string.metronome_input_tap_continue
                    else R.string.metronome_input_tap_start
                ),
                fontFamily = roundedFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = fontSize / 4,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PrecisionSelectorRow(
    selected: PrecisionOption,
    onSelected: (PrecisionOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = context.getString(R.string.metronome_input_tap_precision),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        val options = PrecisionOption.entries
        var expanded by remember { mutableStateOf(false) }
        val textFieldState = rememberTextFieldState(selected.label)

        if (textFieldState.text.toString() != selected.label) {
            textFieldState.setTextAndPlaceCursorAtEnd(selected.label)
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .widthIn(max = 120.dp),
                state = textFieldState,
                readOnly = true,
                lineLimits = TextFieldLineLimits.SingleLine,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                shape = RoundedCornerShape(6.dp)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        selected = selected == option,
                        onClick = {
                            onSelected(option)
                            expanded = false
                        },
                        shapes = MenuDefaults.itemShape(index, options.size),
                        colors = MenuDefaults.selectableItemColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        text = { Text(option.label, style = MaterialTheme.typography.bodyLarge) },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        selectedLeadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun formatBpm(bpm: Float, decimals: Int): DisplayBpm {
    val text = if (decimals == 0) {
        bpm.round(0).toInt().toString()
    } else {
        String.format(Locale.US, "%.${decimals}f", bpm)
    }

    return DisplayBpm(
        bpm = bpm,
        whole = text.substringBefore('.'),
        fraction = text.substringAfter('.', "").takeIf { it.isNotEmpty() }
    )
}

private fun calculateBpm(intervals: List<Long>): Float? {
    if (intervals.size < 2) return null
    val filtered = filteredStdDev(intervals)
    val mean = filtered.average()
    if (mean <= 0.0) return null

    val rawBpm = (60_000_000_000.0 / mean).toFloat()
    return rawBpm
}

private fun stdDev(data: List<Long>): Double {
    if (data.isEmpty()) return 0.0
    val avg = data.average()
    return sqrt(data.map { (it - avg).pow(2) }.average())
}

private fun filteredStdDev(data: List<Long>): List<Long> {
    if (data.size < 4) return data
    val stdDev = stdDev(data)
    if (stdDev == 0.0) return data
    val avg = data.average()
    return data.filter { abs((it - avg) / stdDev) < 2.0 }
}

private fun getDecimals(intervals: List<Long>, precisionOption: PrecisionOption): Int {
    if(precisionOption.decimals != null) return precisionOption.decimals

    val confidence = calculateConfidence(intervals) ?: return 0

    return when {
        confidence <= 0.3 -> 2
        confidence <= 0.5 -> 1
        else -> 0
    }
}

private fun calculateConfidence(intervals: List<Long>): Double? {
    val filtered = filteredStdDev(intervals)
    if (filtered.size < 2) return null

    val filteredSeconds = filtered.map { it / 1e9 }
    val mean = filteredSeconds.average()
    if (mean <= 0.0) return null

    val stdDev = sqrt(filteredSeconds.map { (it - mean).pow(2) }.average())
    val confidence = stdDev / sqrt(filteredSeconds.size.toDouble())
    return 60.0 * confidence / mean.pow(2)
}
