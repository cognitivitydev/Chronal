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

package dev.cognitivity.chronal.metronome.modifiers

import androidx.compose.runtime.derivedStateOf
import dev.cognitivity.chronal.metronome.Metronome
import dev.cognitivity.chronal.metronome.MetronomeModifier
import dev.cognitivity.chronal.metronome.tracks.ClickTrack
import dev.cognitivity.chronal.rhythm.metronome.Beat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Gradually changes the tempo over a specified duration (in beats, measures, or milliseconds). Used by [Accelerando] and [Ritardando].
 */
open class TempoChange(
    metronome: Metronome,
    private val duration: TempoChangeDuration,
    private val tempoRate: Float,
    minBpm: Float = ClickTrack.MIN_BPM,
    maxBpm: Float = ClickTrack.MAX_BPM,
) : MetronomeModifier(metronome) {
    override val isEnabled = derivedStateOf {
        metronome.bpm in range && !metronome.countInActive.value
    }

    private val range: ClosedFloatingPointRange<Float> = minBpm..maxBpm
    private val scope = CoroutineScope(Dispatchers.Default)
    private var tempoJob: Job? = null
    private var elapsedBeats: Long = -1

    override fun onStart() {
        if(!duration.isMillis()) return
        tempoJob?.cancel()
        tempoJob = scope.launch {
            val timestamp = metronome.timestamp
            while(true) {
                delay(duration.value)
                if(!metronome.playing || timestamp != metronome.timestamp) return@launch

                metronome.bpm = (metronome.bpm + tempoRate).coerceIn(range)
            }
        }
    }

    override fun onStop() {
        tempoJob?.cancel()
        tempoJob = null
        elapsedBeats = -1
    }

    override fun onTick(track: ClickTrack, beat: Beat) {
        if(!isEnabled.value) return

        if(!duration.isBeatsOrMeasures()) return
        if(metronome.tracks.indexOf(track) != 0) return
        if(beat.index != 0) return

        when(duration) {
            is TempoChangeDuration.Beats -> {
                val timestamp = metronome.timestamp
                val measure = track.getRhythm().measures[beat.measure]

                tempoJob?.cancel()
                tempoJob = scope.launch {
                    repeat(measure.timeSig.first) {
                        if(++elapsedBeats >= duration.value) {
                            metronome.bpm = (metronome.bpm + tempoRate).coerceIn(range)
                            elapsedBeats = 0
                        }

                        val beatDelay = ((1f / measure.timeSig.second) * 60000 / metronome.bpm * track.beatValue).toLong()
                        delay(beatDelay)
                        if (!metronome.playing || timestamp != metronome.timestamp) return@launch
                    }
                }
            }
            is TempoChangeDuration.Measures -> {
                if(++elapsedBeats >= duration.value) {
                    metronome.bpm = (metronome.bpm + tempoRate).coerceIn(range)
                    elapsedBeats = 0
                }
            }
        }
    }
}

open class TempoChangeDuration(val value: Long) {
    class Millis(value: Long) : TempoChangeDuration(value)
    class Beats(value: Long) : TempoChangeDuration(value)
    class Measures(value: Long) : TempoChangeDuration(value)

    fun isMillis() = this is Millis
    fun isBeatsOrMeasures() = this is Beats || this is Measures
}