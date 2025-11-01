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

package dev.cognitivity.chronal.rhythm.metronome.elements

import dev.cognitivity.chronal.MusicFont

data class RhythmNote(
    val stemDirection: StemDirection,
    override val baseDuration: Double,
    override val tupletRatio: Pair<Int, Int>? = null,
    override val dots: Int = 0
) : RhythmAtom() {
    override fun getDisplay(): String {
        var string = MusicFont.Notation.fromLength(baseDuration, false)?.char?.toString() ?: "?"
        if (stemDirection == StemDirection.DOWN) {
            string = MusicFont.Notation.setEmphasis(string, false)
        }
        return string + " ${MusicFont.Notation.DOT.char}".repeat(dots)
    }
}

enum class StemDirection { UP, DOWN }