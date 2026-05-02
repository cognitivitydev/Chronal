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

package dev.cognitivity.chronal.metronome

import dev.cognitivity.chronal.rhythm.metronome.Beat
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmAtom
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmTuplet
import dev.cognitivity.chronal.settings.types.json.MetronomeConfigTrack
import dev.cognitivity.chronal.settings.types.json.SimpleRhythm
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.cognitivity.chronal.metronome.sound.SoundPack
import dev.cognitivity.chronal.settings.types.json.TrackColor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class MetronomeTrack(
    var name: String = "New track",
    private var rhythm: Rhythm,
    var beatValue: Float = 4f,
    var vibrate: Boolean = true,
    enabled: Boolean = true,
    var simpleRhythm: SimpleRhythm = SimpleRhythm(4 to 4, 4, 2),
    var color: TrackColor = TrackColor.Primary,
    var soundPack: SoundPack = SoundPack.default(),
) {
    companion object {
        const val MIN_BPM = 1f
        const val MAX_BPM = 16000f

        fun fromSetting(setting: MetronomeConfigTrack): MetronomeTrack {
            return MetronomeTrack(
                name = setting.name,
                rhythm = Rhythm.deserialize(setting.rhythm),
                simpleRhythm = setting.simpleRhythm,
                beatValue = setting.beatValue,
                vibrate = setting.vibrate,
                enabled = setting.enabled,
                color = setting.color,
                soundPack = SoundPack.byId(setting.soundPackId) ?: SoundPack.default(),
            )
        }
    }

    var enabled by mutableStateOf(enabled)

    private var intervals: List<Beat> = calculateIntervals(rhythm)

    var index: Int = -1

    var nextBeatSample: Long = 0L
    var sampleRemainder: Double = 0.0

    fun setRhythm(newRhythm: Rhythm) {
        this.rhythm = newRhythm
        this.intervals = calculateIntervals(rhythm)
        this.index = -1
        this.nextBeatSample = 0L
        this.sampleRemainder = 0.0

        onEdit(newRhythm)
    }

    fun getRhythm() = rhythm
    fun getIntervals() = intervals

    fun calculateIntervals(rhythm: Rhythm): List<Beat> {
        val list = mutableListOf<Beat>()
        for ((measureIndex, measure) in rhythm.measures.withIndex()) {
            var i = 0
            for (element in measure.elements) {
                when (element) {
                    is RhythmAtom -> {
                        list.add(Beat(element, measureIndex, i))
                        i++
                    }
                    is RhythmTuplet -> {
                        for (note in element.notes) {
                            list.add(Beat(note, measureIndex, i))
                            i++
                        }
                    }
                }
            }
        }
        return list
    }

    private val _updateEvents = MutableSharedFlow<Beat>(replay = 1)
    val updateEvents: SharedFlow<Beat> = _updateEvents.asSharedFlow()

    private val _pauseEvents = MutableStateFlow(false)
    val pauseEvents: SharedFlow<Boolean> = _pauseEvents.asStateFlow()

    private val _editEvents = MutableSharedFlow<Rhythm>(replay = 1)
    val editEvents: SharedFlow<Rhythm> = _editEvents.asSharedFlow()

    fun onUpdate(beat: Beat) {
        _updateEvents.tryEmit(beat)
    }
    fun onPause(paused: Boolean) {
        _pauseEvents.tryEmit(paused)
    }
    fun onEdit(rhythm: Rhythm) {
        _editEvents.tryEmit(rhythm)
    }
}
