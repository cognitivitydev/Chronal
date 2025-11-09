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

package dev.cognitivity.chronal.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.Metronome
import dev.cognitivity.chronal.MetronomeTrack
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.pxToDp
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LatencyActivity : ComponentActivity() {
    val metronome = Metronome(sendNotifications = false).apply {
        addTrack(0, MetronomeTrack(
            Rhythm.deserialize("{4/4}Q;Q;Q;Q;"),
            bpm = 120f,
            beatValue = 4f,
        ))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MetronomeTheme {
                MainContent()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        metronome.stop()
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun MainContent() {
        val clicks = remember { arrayListOf<Long>() }
        var lastTick by remember { mutableLongStateOf(0L) }
        var clickOffset by remember { mutableStateOf(Offset(0f, 0f)) }
        var average by remember { mutableIntStateOf(-1) }

        val tertiary = MaterialTheme.colorScheme.tertiary
        val tertiaryTransparent = MaterialTheme.colorScheme.tertiary.copy(alpha = 0f)
        val metronomeColor = remember { Animatable(tertiaryTransparent) }
        val metronomeSize = remember { Animatable(0f) }

        val scope = rememberCoroutineScope()

        metronome.getTrack(0).setUpdateListener(0) {
            lastTick = System.currentTimeMillis()

            scope.launch {
                delay(average.toLong())
                metronomeColor.snapTo(tertiary)
                metronomeColor.animateTo(
                    targetValue = tertiaryTransparent,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = EaseOutCubic
                    )
                )
            }
            scope.launch {
                delay(average.toLong())
                metronomeSize.snapTo(100f)
                metronomeSize.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = EaseOutCubic
                    )
                )
            }
        }
        metronome.start()


        val primary = MaterialTheme.colorScheme.primary
        val primaryTransparent = MaterialTheme.colorScheme.primary.copy(alpha = 0f)
        val animatedColor = remember { Animatable(primaryTransparent) }
        val animatedSize = remember { Animatable(0f) }
        val animatedRotation = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            animatedRotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing))
            )
        }

        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(getString(R.string.latency_title),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(getString(R.string.latency_info),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                finish()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = getString(R.string.generic_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    )
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = if (average < 0) "" else getString(R.string.latency_average, average),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    FilledTonalButton(
                        onClick = {
                            clicks.clear()
                            average = -1
                        },
                        enabled = average >= 0
                    ) {
                        Text(getString(R.string.generic_reset))
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = {
                            ChronalApp.getInstance().settings.visualLatency.value = average
                            clicks.clear()
                            average = -1
                            scope.launch {
                                ChronalApp.getInstance().settings.save()
                            }
                        },
                        enabled = average >= 0,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_save_24),
                            contentDescription = getString(R.string.generic_save)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(getString(R.string.generic_save))
                    }
                }
            },
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onPress = { offset ->
                    if(lastTick == 0L) return@detectTapGestures
                    val difference = System.currentTimeMillis() - lastTick
                    lastTick = 0L
                    clicks.add(difference)
                    average = ((((clicks.sum() / clicks.size) + 5) / 10) * 10).toInt()

                    clickOffset = offset

                    scope.launch {
                        animatedColor.snapTo(primary)
                        animatedColor.animateTo(
                            targetValue = primaryTransparent,
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = EaseOutCubic
                            )
                        )
                    }
                    scope.launch {
                        animatedSize.snapTo(0f)
                        animatedSize.animateTo(
                            targetValue = 1000f,
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = EaseOutCubic
                            )
                        )
                    }
                })
            },
        ) { innerPadding ->
            val size = animatedSize.value.pxToDp()
            val color = animatedColor.value
            Box(
                modifier = Modifier.size(size)
                    .aspectRatio(1f)
                    .offset(clickOffset.x.pxToDp(), clickOffset.y.pxToDp())
                    .offset(x = (-size / 2), y = (-size / 2))
                    .rotate(animatedRotation.value)
                    .background(color.copy(alpha = color.alpha * 0.2f), MaterialShapes.Cookie12Sided.toShape(0))
                    .border(2.dp, color, MaterialShapes.Cookie12Sided.toShape(0))
            )

            if(average == -1) return@Scaffold
            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding)
            ) {
                val metronomeSizeDp = metronomeSize.value.pxToDp()
                Box(
                    modifier = Modifier.align(Alignment.BottomStart)
                        .padding(16.dp)
                        .size(metronomeSizeDp)
                        .aspectRatio(1f)
                        .offset(x = (100f.pxToDp() - metronomeSizeDp) / 2f, y = -(100f.pxToDp() - metronomeSizeDp) / 2f)
                        .background(metronomeColor.value.copy(alpha = metronomeColor.value.alpha * 0.2f), CircleShape)
                        .border(2.dp, metronomeColor.value, CircleShape)
                )
            }
        }
    }
}