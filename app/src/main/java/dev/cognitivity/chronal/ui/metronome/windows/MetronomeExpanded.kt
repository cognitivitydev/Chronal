package dev.cognitivity.chronal.ui.metronome.windows

import android.view.Window
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.cognitivity.chronal.toPx
import dev.cognitivity.chronal.ui.metronome.CircularClock
import dev.cognitivity.chronal.ui.metronome.ConductorDisplay
import dev.cognitivity.chronal.ui.metronome.PlayButton
import dev.cognitivity.chronal.ui.metronome.RhythmButtons
import kotlin.math.abs

@Composable
fun MetronomePageExpanded(
    navController: NavHostController,
    window: Window,
) {
    updateSleepMode(window)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(horizontal = 12.dp)
                .fillMaxSize()
                .padding(
                    start = 0.dp,
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
        ) {
            TopBar(navController, Color.Transparent, false, RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp))
            var change = 0
            Row(
                modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            change += dragAmount.toInt()
                            if (abs(change) >= 8) {
                                val adjustment = (change / 8)
                                setBPM(metronome.bpm - adjustment)
                                change %= 8
                            }
                        }
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            paused = !paused

                            if (paused) {
                                metronome.stop()
                                metronomeSecondary.stop()
                            }
                            else {
                                metronome.start()
                                metronomeSecondary.start()
                            }

                            updateSleepMode(window)
                        }
                    )

            ) {
                NavHost(navController,
                    startDestination = "clock",
                    enterTransition = {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = 200,
                                delayMillis = 200,
                                easing = EaseOutCubic
                            )
                        )
                    },
                    exitTransition = {
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = EaseInCubic
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    composable("clock") {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxHeight()
                                    .aspectRatio(1f)
                                    .align(Alignment.Center)
                            ) {
                                CircularClock(true,
                                    trackSize = 6.dp.toPx(),
                                    trackOff = MaterialTheme.colorScheme.secondaryContainer,
                                    trackPrimary = MaterialTheme.colorScheme.primary,
                                    trackSecondary = MaterialTheme.colorScheme.secondary,
                                    majorOffColor = MaterialTheme.colorScheme.primaryContainer,
                                    minorOffColor = MaterialTheme.colorScheme.onPrimary,
                                    majorPrimaryColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    minorPrimaryColor = MaterialTheme.colorScheme.primary,
                                    majorSecondaryColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    minorSecondaryColor = MaterialTheme.colorScheme.secondary,
                                )
                            }
                            if (secondaryEnabled) {
                                Box(
                                    modifier = Modifier.fillMaxHeight(0.8f)
                                        .aspectRatio(1f)
                                        .align(Alignment.Center)
                                ) {
                                    CircularClock(false,
                                        trackSize = 4.dp.toPx(),
                                        trackOff = MaterialTheme.colorScheme.onSecondary,
                                        trackPrimary = MaterialTheme.colorScheme.tertiary,
                                        trackSecondary = MaterialTheme.colorScheme.secondary,
                                        majorOffColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        minorOffColor = MaterialTheme.colorScheme.onTertiary,
                                        majorPrimaryColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        minorPrimaryColor = MaterialTheme.colorScheme.tertiary,
                                        majorSecondaryColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        minorSecondaryColor = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }
                        }
                    }
                    composable("conductor") {
                        ConductorDisplay()
                    }
                }
                Column(
                    modifier = Modifier.weight(1f)
                        .fillMaxSize()
                ) {
                    RhythmButtons(1f, navController)
                    PlayButton(1f)
                }
            }
        }
    }
}
