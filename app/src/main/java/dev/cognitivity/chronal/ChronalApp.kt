/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2025-2026  cognitivity
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

package dev.cognitivity.chronal

import android.app.Application
import android.content.Context
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.notifications.PracticeReminderScheduler
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.settings.Setting
import dev.cognitivity.chronal.settings.Settings
import kotlinx.coroutines.runBlocking
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

class ChronalApp : Application() {
    lateinit var metronome: Metronome
    var tuner: Tuner? = null
    val developmentBuild = true

    override fun onCreate() {
        super.onCreate()
        application = this
        Setting.init(applicationContext)

        runBlocking {
            Setting.loadAll()

            val state = Settings.METRONOME_STATE.get()

            metronome = Metronome()
            metronome.addTrack(0, MetronomeTrack(
                rhythm = Rhythm.deserialize(Settings.METRONOME_RHYTHM.get()),
                bpm = state.bpm,
                beatValue = state.beatValuePrimary,
            ))
            val secondaryTrack = MetronomeTrack(
                rhythm = Rhythm.deserialize(Settings.METRONOME_RHYTHM_SECONDARY.get()),
                bpm = state.bpm,
                beatValue = state.beatValueSecondary,
            )
            secondaryTrack.enabled = false
            metronome.addTrack(1, secondaryTrack)

            PracticeReminderScheduler.initialize(applicationContext)
        }
    }

    fun isInitialized(): Boolean {
        return ::metronome.isInitialized
    }

    companion object {
        lateinit var application: Application

        fun getInstance(): ChronalApp {
            return application as ChronalApp
        }

        val context: Context
            get() = application.applicationContext

    }
}

fun Double.floor(decimals: Int = 2): Double = floor(this * 10.0.pow(decimals)) / 10.0.pow(decimals)
fun Float.floor(decimals: Int = 2): Float = floor(this * 10f.pow(decimals)) / 10f.pow(decimals)

fun Double.ceil(decimals: Int = 2): Double = ceil(this * 10.0.pow(decimals)) / 10.0.pow(decimals)
fun Float.ceil(decimals: Int = 2): Float = ceil(this * 10f.pow(decimals)) / 10f.pow(decimals)

fun Double.round(decimals: Int = 2) = (this * 10.0.pow(decimals)).roundToInt() / 10.0.pow(decimals)
fun Float.round(decimals: Int = 2) = (this * 10f.pow(decimals)).roundToInt() / 10f.pow(decimals)

fun Dp.toPx(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.value,
        ChronalApp.context.resources.displayMetrics
    )
}

fun Float.pxToDp(): Dp {
    return ceil(this / ChronalApp.context.resources.displayMetrics.density).dp
}

@Composable
fun Dp.toSp(): TextUnit {
    return with(LocalDensity.current) { this@toSp.toSp() }
}

@Composable
fun TextUnit.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}

