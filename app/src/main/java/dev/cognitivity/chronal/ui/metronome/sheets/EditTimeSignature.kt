package dev.cognitivity.chronal.ui.metronome.sheets

import android.view.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.SimpleRhythm
import dev.cognitivity.chronal.toSp
import dev.cognitivity.chronal.ui.WavyHorizontalLine
import dev.cognitivity.chronal.ui.WavyVerticalLine
import kotlin.math.pow

@Composable
fun EditTimeSignature(window: Window, primary: Boolean, expanded: Boolean) {
    val setting = if(primary) ChronalApp.getInstance().settings.metronomeSimpleRhythm else ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary
    var value by remember { mutableStateOf(setting.value) }
    if(expanded) {
        Row {
            Box(Modifier.align(Alignment.CenterVertically)) {
                ClockPreview(primary, value)
            }
            Column(
                modifier = Modifier.weight(1f)
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
                modifier = Modifier.fillMaxHeight()
                    .padding(8.dp, 32.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
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
            Box(
                modifier = Modifier.padding(bottom = 32.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
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
                modifier = Modifier.fillMaxWidth()
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
fun ColumnScope.EditBeatCount(primary: Boolean, value: SimpleRhythm, onUpdate: (Int) -> Unit = {}) {
    Text(
        text = context.getString(R.string.metronome_edit_rhythm_beat_count),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.align(Alignment.CenterHorizontally)
            .padding(0.dp, 0.dp, 0.dp, 16.dp)
    )
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        items(value.timeSignature.first) { i ->
            BeatShape(i+1)
        }
    }
    Row(
        modifier = Modifier.align(Alignment.CenterHorizontally)
    ) {
        Box(
            modifier = Modifier.padding(8.dp)
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
                modifier = Modifier.padding(16.dp, 8.dp)
                    .align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier.size(40.dp)
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
            modifier = Modifier.padding(8.dp)
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
                modifier = Modifier.padding(16.dp, 8.dp)
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
        modifier = Modifier.align(Alignment.CenterHorizontally)
            .padding(0.dp, 0.dp, 0.dp, 16.dp)
    )

    Row(
        modifier = Modifier.align(Alignment.CenterHorizontally)
            .padding(horizontal = 16.dp)
    ) {
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
                modifier = Modifier.padding(8.dp)
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
                    modifier = Modifier.offset((-6).dp, 24.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
    Row(
        modifier = Modifier.align(Alignment.CenterHorizontally)
            .padding(horizontal = 16.dp)
    ) {
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
                modifier = Modifier.padding(8.dp)
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
                    modifier = Modifier.offset((-6).dp, 24.dp)
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
        modifier = Modifier.padding(8.dp)
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
                modifier = Modifier.fillMaxSize()
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
            modifier = Modifier.fillMaxSize()
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