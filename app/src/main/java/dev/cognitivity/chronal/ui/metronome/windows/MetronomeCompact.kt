package dev.cognitivity.chronal.ui.metronome.windows

import android.view.Window
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
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
fun MetronomePageCompact(
    navController: NavHostController,
    window: Window,
    padding: PaddingValues
) {
    updateSleepMode(window)

    Scaffold(
        modifier = Modifier.padding(bottom = padding.calculateBottomPadding()).fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                TopBar(navController, MaterialTheme.colorScheme.surfaceContainerLow, true, RoundedCornerShape(0))
            }
        },
    ) { innerPadding ->
        var change = 0
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
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
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RhythmButtons(0.5f, navController)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
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
                        }
                    ) {
                        composable("clock") {
                            Box(
                                modifier = Modifier.fillMaxSize()
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
                                        modifier = Modifier.fillMaxHeight()
                                            .padding(24.dp)
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
                }
                PlayButton(0.5f)
            }
        }
    }
}
