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

package dev.cognitivity.chronal.ui.metronome

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.settings.Settings
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

fun Modifier.verticalBPMGesture(
    onHold: () -> Unit = {},
    onTap: () -> Unit = {},
    onSwipe: (Int) -> Unit = {},
): Modifier {
    val tapEnabled = Settings.GESTURE_TAP_ENABLED.get()
    val holdEnabled = Settings.GESTURE_HOLD_ENABLED.get()
    val holdDuration = Settings.GESTURE_HOLD_DURATION.get()
    val swipeEnabled = Settings.GESTURE_SWIPE_ENABLED.get()
    val swipeSensitivity = Settings.GESTURE_SWIPE_SENSITIVITY.get()
    val invertedSwipe = Settings.GESTURE_SWIPE_INVERTED.get()

    return pointerInput(Unit) {
        awaitPointerEventScope {
            val dragThreshold = 128f * (1f - swipeSensitivity)

            while (true) {
                val down = awaitPointerEvent().changes.firstOrNull { it.pressed && !it.isConsumed }
                    ?: continue
                val downTime = System.currentTimeMillis()
                var lastY = down.position.y

                var totalDrag = 0f
                var isDrag = false

                var holdTriggered = false
                var vibrating = false

                try {
                    while(true) {
                        val elapsed = System.currentTimeMillis() - downTime
                        val vibrationStart = min(holdDuration / 2, 150)
                        if(holdEnabled && !isDrag && !holdTriggered) {
                            if(elapsed >= holdDuration) {
                                holdTriggered = true
                                vibrationEnd()
                                onHold()
                            } else if(!vibrating && elapsed >= vibrationStart) {
                                vibrating = true
                                val (timings, amplitudes) = vibrationRamp(holdDuration)
                                vibrate(timings, amplitudes)
                            }
                        }
                        // wait 50ms for next event
                        val event = withTimeoutOrNull(50) { awaitPointerEvent() }
                        if(event == null) continue

                        val change = event.changes.find { it.id == down.id } ?: break
                        if(!change.pressed || change.isConsumed) break

                        val dy = change.position.y - lastY
                        lastY = change.position.y

                        val distance = abs(down.position.y - change.position.y)
                        if(swipeEnabled && (distance > dragThreshold/2 || isDrag)) {
                            if(vibrating) {
                                vibrating = false
                                cancelVibration()
                            }
                            isDrag = true
                            totalDrag += dy

                            val amount = (-totalDrag / dragThreshold).toInt()
                            if (amount != 0) {
                                onSwipe(amount * if (invertedSwipe) -1 else 1)
                                totalDrag += amount * dragThreshold
                            }
                        }

                        if(holdTriggered) {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.changes.none { it.id == down.id && it.pressed }) break
                            }
                            break
                        }
                    }
                } finally {
                    if(vibrating) cancelVibration()
                }

                if(!isDrag && !holdTriggered && tapEnabled) {
                    onTap()
                }
            }
        }
    }
}

private fun vibrationRamp(holdDuration: Int): Pair<LongArray, IntArray> {
    val vibrationStart = min(holdDuration / 2, 150)
    val vibrationMillis = holdDuration - vibrationStart
    val steps = 128
    val timings = LongArray(steps) { (vibrationMillis / steps).toLong() }
    val amplitudes = IntArray(steps) { i ->
        val progress = i / (steps - 1f)
        (100f.pow(progress)).toInt()
    }
    return Pair(timings, amplitudes)
}

private fun vibrate(timings: LongArray, amplitudes: IntArray) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.vibrate(
            CombinedVibration.createParallel(
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            )
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }
}

private fun vibrationEnd() {
    cancelVibration()
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val vibrationEffect = VibrationEffect.createWaveform(
            longArrayOf(50, 20, 20, 20, 20),
            intArrayOf(255, 100, 50, 20, 10),
            -1
        )
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.vibrate(
                CombinedVibration.createParallel(vibrationEffect)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(vibrationEffect)
        }
    }
}

private fun cancelVibration(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.cancel()
    } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.cancel()
    }
    return true
}