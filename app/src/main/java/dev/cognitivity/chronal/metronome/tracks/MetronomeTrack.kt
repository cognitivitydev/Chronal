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

package dev.cognitivity.chronal.metronome.tracks

import dev.cognitivity.chronal.settings.types.json.metronome.MetronomeConfigAudioTrack
import dev.cognitivity.chronal.settings.types.json.metronome.MetronomeConfigClickTrack
import dev.cognitivity.chronal.settings.types.json.metronome.MetronomeConfigTrack
import dev.cognitivity.chronal.settings.types.json.metronome.TrackColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow

open class MetronomeTrack(
    open var name: String,
    open var color: TrackColor,
    open var enabled: Boolean,
) {
    companion object {
        fun fromSetting(setting: MetronomeConfigTrack): MetronomeTrack {
            return when (setting) {
                is MetronomeConfigClickTrack -> ClickTrack.fromSetting(setting)
                is MetronomeConfigAudioTrack -> AudioTrack.fromSetting(setting)
                else -> throw IllegalArgumentException("Unknown track type: ${setting::class.java.simpleName}")
            }
        }
    }

    private val _pauseEvents = MutableStateFlow(true)
    val pauseEvents: SharedFlow<Boolean> = _pauseEvents.asStateFlow()

    fun onPause(paused: Boolean) {
        _pauseEvents.tryEmit(paused)
    }
}