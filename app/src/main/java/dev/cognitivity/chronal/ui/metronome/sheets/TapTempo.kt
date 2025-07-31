package dev.cognitivity.chronal.ui.metronome.sheets

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.MetronomeState
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.ceil
import dev.cognitivity.chronal.ui.metronome.windows.intervals
import dev.cognitivity.chronal.ui.metronome.windows.lastTapTime
import dev.cognitivity.chronal.ui.metronome.windows.metronome
import dev.cognitivity.chronal.ui.metronome.windows.metronomeSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TapTempo() {
    var newBpm by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = context.getString(R.string.metronome_option_tap_tempo),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = newBpm.toString(),
                    fontSize = 96.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    modifier = Modifier
                        .offset(y = (-8).dp)
                        .align(Alignment.Bottom),
                    text = context.getString(R.string.metronome_bpm),
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(8.dp),
            )
        }
        val scope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        val previousBpm = newBpm
                        val currentTime = System.nanoTime()
                        lastTapTime?.let { last ->
                            val interval = currentTime - last
                            intervals.add(interval)

                            val filtered = filteredStdDev(intervals)
                            val isOutlier = !filtered.contains(interval)

                            val average = filtered.average()
                            newBpm = (60_000_000_000 / average).toInt()

                            var newProgress = progress.value
                            if (intervals.size <= 4) {
                                newProgress += 0.05f
                            }
                            if (newBpm == previousBpm) {
                                newProgress += 0.04f
                            } else if (newBpm != 0 && abs(newBpm - previousBpm) > 1) {
                                newProgress -= min((abs(newBpm - previousBpm) * 0.001f).ceil(2), 0.2f)
                            }
                            if (isOutlier) {
                                newProgress -= 0.02f
                            }
                            scope.launch {
                                progress.animateTo(
                                    targetValue = newProgress.coerceIn(0f, 1.1f),
                                    animationSpec = MotionScheme.expressive().slowSpatialSpec(),
                                )
                            }
                        }
                        lastTapTime = currentTime
                        if (intervals.size > 64) {
                            intervals.removeAt(0)
                        }

                        if (newBpm != 0) {
                            scope.launch {
                                val last = lastTapTime
                                delay(60000L / newBpm * 5)
                                if (last == lastTapTime) {
                                    metronome.bpm = newBpm
                                    metronomeSecondary.bpm = newBpm

                                    ChronalApp.getInstance().settings.metronomeState.value = MetronomeState(
                                        bpm = newBpm, beatValuePrimary = metronome.beatValue,
                                        beatValueSecondary = metronomeSecondary.beatValue, secondaryEnabled = metronomeSecondary.active
                                    )

                                    scope.launch {
                                        ChronalApp.getInstance().settings.save()
                                    }
                                    newBpm = 0
                                    intervals.clear()
                                    lastTapTime = 0
                                    scope.launch {
                                        progress.animateTo(
                                            targetValue = 0f,
                                            animationSpec = MotionScheme.expressive().slowSpatialSpec(),
                                        )
                                    }
                                }
                            }
                        }
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.outline_touch_app_24),
                contentDescription = context.getString(R.string.metronome_option_tap_tempo),
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .padding(16.dp)
                    .size(64.dp)
            )
        }
    }
}

fun filteredStdDev(data: MutableList<Long>): List<Long> {
    val mean = data.average()
    val stdDev = sqrt(data.sumOf { (it - mean) * (it - mean) } / data.size)
    val zScores = data.map { (it - mean) / stdDev }
    val filtered = data.filterIndexed { index, _ -> abs(zScores[index]) < 1.0 }
    return filtered
}