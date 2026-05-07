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

package dev.cognitivity.chronal.tuner

import dev.cognitivity.chronal.settings.Settings
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round

data class Pitch(
    val pitch: PitchClass,
    val octave: Int,
    val centsOff: Float
) {
    fun toDisplayName(): NoteName {
        if(octave <= 0) return NoteName("-")
        val showOctave = Settings.SHOW_OCTAVE.get()

        val locale = Settings.NOTE_NAMES.get()
        val noteName = NoteSystem.entries[locale].getName(pitch)

        val name = if(showOctave) "${noteName.name}${octave}" else noteName.name
        val enharmonic = if(noteName.enharmonic != null) {
            if(showOctave) "${noteName.enharmonic}${octave}" else noteName.enharmonic
        } else null

        return NoteName(name, enharmonic)
    }

    companion object {
        const val A4_MIDI = 69
        fun getA4(): Int = Settings.TUNER_FREQUENCY.get()

        fun fromFrequency(frequency: Float): Pitch {
            if(frequency <= 0) return Pitch(
                pitch = PitchClass.C,
                octave = 0,
                centsOff = Float.NaN
            )
            val a4 = getA4()

            val nearestMidi = round(A4_MIDI + 12 * log2(frequency / a4)).toInt()
            val nearestFrequency = a4 * 2.0.pow((nearestMidi - A4_MIDI) / 12.0)

            val pitchClass = PitchClass.fromSemitone(nearestMidi)
            val octave = (nearestMidi / 12) - 1
            val centsOff = (1200 * log2(frequency / nearestFrequency)).toFloat()

            return Pitch(
                pitch = pitchClass,
                octave = octave,
                centsOff = centsOff
            )
        }

        fun midiToFrequency(midi: Int): Float {
            val a4Midi = 69
            val a4 = getA4()

            val frequency = if(midi == -1) a4.toFloat()
            else (a4 * 2.0.pow((midi - a4Midi) / 12.0)).toFloat()
            return frequency
        }
        fun midiToFrequency(frequency: Float, semitones: Int): Float {
            return frequency * 2.0.pow(semitones / 12.0).toFloat()
        }
    }
}

enum class PitchClass(val semitone: Int) {
    C(0), Cs(1),
    D(2), Ds(3),
    E(4),
    F(5), Fs(6),
    G(7), Gs(8),
    A(9), As(10),
    B(11);

    companion object {
        fun fromSemitone(semitone: Int) = entries.first { it.semitone == semitone.mod(12) }
    }

    fun toSemitones(octave: Int): Int = semitone + (octave * 12)
}

data class NoteName(
    val name: String,
    val enharmonic: String? = null
)
