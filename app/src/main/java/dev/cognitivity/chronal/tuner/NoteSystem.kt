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

enum class NoteSystem(
    val names: Map<PitchClass, NoteName>
) {
    ENGLISH(
        mapOf(
            PitchClass.C  to NoteName("C"),
            PitchClass.Cs to NoteName("C♯", "D♭"),
            PitchClass.D  to NoteName("D"),
            PitchClass.Ds to NoteName("D♯", "E♭"),
            PitchClass.E  to NoteName("E"),
            PitchClass.F  to NoteName("F"),
            PitchClass.Fs to NoteName("F♯", "G♭"),
            PitchClass.G  to NoteName("G"),
            PitchClass.Gs to NoteName("G♯", "A♭"),
            PitchClass.A  to NoteName("A"),
            PitchClass.As to NoteName("A♯", "B♭"),
            PitchClass.B  to NoteName("B")
        )
    ),

    SOLFEGE_ENGLISH(
        mapOf(
            PitchClass.C  to NoteName("Do"),
            PitchClass.Cs to NoteName("Do♯", "Re♭"),
            PitchClass.D  to NoteName("Re"),
            PitchClass.Ds to NoteName("Re♯", "Mi♭"),
            PitchClass.E  to NoteName("Mi"),
            PitchClass.F  to NoteName("Fa"),
            PitchClass.Fs to NoteName("Fa♯", "Sol♭"),
            PitchClass.G  to NoteName("Sol"),
            PitchClass.Gs to NoteName("Sol♯", "La♭"),
            PitchClass.A  to NoteName("La"),
            PitchClass.As to NoteName("La♯", "Ti♭"),
            PitchClass.B  to NoteName("Ti")
        )
    ),

    SOLFEGE_CHROMATIC(
        mapOf(
            PitchClass.C  to NoteName("Do"),
            PitchClass.Cs to NoteName("Di", "Ra"),
            PitchClass.D  to NoteName("Re"),
            PitchClass.Ds to NoteName("Ri", "Me"),
            PitchClass.E  to NoteName("Mi"),
            PitchClass.F  to NoteName("Fa"),
            PitchClass.Fs to NoteName("Fi", "Se"),
            PitchClass.G  to NoteName("Sol"),
            PitchClass.Gs to NoteName("Si", "Le"),
            PitchClass.A  to NoteName("La"),
            PitchClass.As to NoteName("Li", "Te"),
            PitchClass.B  to NoteName("Ti")
        )
    ),

    SOLFEGE_LATIN(
        mapOf(
            PitchClass.C  to NoteName("Do"),
            PitchClass.Cs to NoteName("Do♯", "Re♭"),
            PitchClass.D  to NoteName("Re"),
            PitchClass.Ds to NoteName("Re♯", "Mi♭"),
            PitchClass.E  to NoteName("Mi"),
            PitchClass.F  to NoteName("Fa"),
            PitchClass.Fs to NoteName("Fa♯", "Sol♭"),
            PitchClass.G  to NoteName("Sol"),
            PitchClass.Gs to NoteName("Sol♯", "La♭"),
            PitchClass.A  to NoteName("La"),
            PitchClass.As to NoteName("La♯", "Si♭"),
            PitchClass.B  to NoteName("Si")
        )
    ),

    GERMAN(
        mapOf(
            PitchClass.C  to NoteName("C"),
            PitchClass.Cs to NoteName("Cis", "Des"),
            PitchClass.D  to NoteName("D"),
            PitchClass.Ds to NoteName("Dis", "Es"),
            PitchClass.E  to NoteName("E"),
            PitchClass.F  to NoteName("F"),
            PitchClass.Fs to NoteName("Fis", "Ges"),
            PitchClass.G  to NoteName("G"),
            PitchClass.Gs to NoteName("Gis", "As"),
            PitchClass.A  to NoteName("A"),
            PitchClass.As to NoteName("Ais", "B"),
            PitchClass.B  to NoteName("H")
        )
    ),

    NASHVILLE(
        mapOf(
            PitchClass.C  to NoteName("1"),
            PitchClass.Cs to NoteName("♯1", "♭2"),
            PitchClass.D  to NoteName("2"),
            PitchClass.Ds to NoteName("♯2", "♭3"),
            PitchClass.E  to NoteName("3"),
            PitchClass.F  to NoteName("4"),
            PitchClass.Fs to NoteName("♯4", "♭5"),
            PitchClass.G  to NoteName("5"),
            PitchClass.Gs to NoteName("♯5", "♭6"),
            PitchClass.A  to NoteName("6"),
            PitchClass.As to NoteName("♯6", "♭7"),
            PitchClass.B  to NoteName("7")
        )
    );

    fun getName(pitch: PitchClass): NoteName {
        return names[pitch] ?: NoteName("?")
    }
    fun getPitch(string: String): PitchClass? {
        val entry = names.entries.firstOrNull { it.value.name == string || it.value.enharmonic == string }
        return entry?.key
    }
}