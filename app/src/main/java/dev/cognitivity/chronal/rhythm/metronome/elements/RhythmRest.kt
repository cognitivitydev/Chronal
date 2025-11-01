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

data class RhythmRest(
    override val baseDuration: Double,
    override val tupletRatio: Pair<Int, Int>? = null,
    override val dots: Int = 0
) : RhythmAtom() {
    init {
        require(baseDuration > 0) { "Base duration must be positive - ${baseDuration}" }
    }
    override fun getDisplay(): String {
        val char = MusicFont.Notation.fromLength(baseDuration, true)?.char ?: '?'
        return char + " ${MusicFont.Notation.DOT.char}".repeat(dots)
    }
}