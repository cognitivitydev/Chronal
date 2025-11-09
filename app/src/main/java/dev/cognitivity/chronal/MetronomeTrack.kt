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

package dev.cognitivity.chronal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import dev.cognitivity.chronal.rhythm.metronome.Beat
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmAtom
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmTuplet

class MetronomeTrack(private var rhythm: Rhythm, bpm: Float = 60f, var beatValue: Float = 4f) {
    var bpm by mutableFloatStateOf(bpm)

    var enabled: Boolean = true

    private var intervals: List<Beat> = calculateIntervals(rhythm)

    var index: Int = -1
    var nextScheduledTime: Long = 0L

    fun setRhythm(newRhythm: Rhythm) {
        this.rhythm = newRhythm
        this.intervals = calculateIntervals(rhythm)
        this.index = -1
        this.nextScheduledTime = 0L

        onEdit(newRhythm)
    }
    fun getRhythm() = rhythm

    fun getIntervals() = intervals

    fun calculateIntervals(rhythm: Rhythm): List<Beat> {
        val intervals = mutableListOf<Beat>()
        for ((measureIndex, measure) in rhythm.measures.withIndex()) {
            var index = 0
            for (element in measure.elements) {
                when (element) {
                    is RhythmAtom -> {
                        intervals.add(Beat(element, measureIndex, index))
                        index++
                    }
                    is RhythmTuplet -> {
                        for (note in element.notes) {
                            intervals.add(Beat(note, measureIndex, index))
                            index++
                        }
                    }
                }
            }
        }
        return intervals
    }

    private val listenerUpdate = mutableMapOf<Int, (Beat) -> Unit>()
    private val listenerPause = mutableMapOf<Int, (Boolean) -> Unit>()
    private val listenerEdit = mutableMapOf<Int, (Rhythm) -> Unit>()

    fun setUpdateListener(id: Int, listener: (Beat) -> Unit) {
        this.listenerUpdate[id] = listener
    }

    fun setPauseListener(id: Int, listener: (Boolean) -> Unit) {
        this.listenerPause[id] = listener
    }

    fun setEditListener(id: Int, listener: (Rhythm) -> Unit) {
        this.listenerEdit[id] = listener
    }

    fun onUpdate(beat: Beat) {
        for (listener in listenerUpdate.values) { listener(beat) }
    }
    fun onPause(paused: Boolean) {
        for (listener in listenerPause.values) { listener(paused) }
    }
    fun onEdit(rhythm: Rhythm) {
        for (listener in listenerEdit.values) { listener(rhythm) }
    }
}
