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

    private val listenerUpdate = mutableMapOf<Int, (Beat) -> Unit>()
    private val listenerPause = mutableMapOf<Int, (Boolean) -> Unit>()
    private val listenerEdit = mutableMapOf<Int, (Rhythm) -> Unit>()

    fun setUpdateListener(id: Int, listener: (Beat) -> Unit) { listenerUpdate[id] = listener }
    fun setPauseListener(id: Int, listener: (Boolean) -> Unit) { listenerPause[id] = listener }
    fun setEditListener(id: Int, listener: (Rhythm) -> Unit) { listenerEdit[id] = listener }

    fun onUpdate(beat: Beat) { for (l in listenerUpdate.values) l(beat) }
    fun onPause(paused: Boolean) { for (l in listenerPause.values) l(paused) }
    fun onEdit(rhythm: Rhythm) { for (l in listenerEdit.values) l(rhythm) }
}
