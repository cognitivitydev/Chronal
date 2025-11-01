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

import kotlin.math.pow

sealed class RhythmAtom : RhythmElement() {
    abstract val baseDuration: Double
    abstract val tupletRatio: Pair<Int, Int>?
    abstract val dots: Int

    fun isRest(): Boolean = this is RhythmRest
    fun getDuration(): Double {
        val tupleModifier = tupletRatio?.let { it.second.toDouble() / it.first.toDouble() } ?: 1.0
        val dotModifier = 1 + (1..dots).sumOf { 1.0 / (2.0.pow(it)) }
        return baseDuration * tupleModifier * dotModifier
    }
    abstract fun getDisplay(): String
}
