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

package dev.cognitivity.chronal.rhythm.player.elements

class Pause(
    startTime: Long,
    endTime: Long,
    beats: Int? = null
) : PlayerRhythmElement(startTime, endTime, beats) {
    constructor(startTime: Long, beats: Int, tempo: Int, maxEnd: Long = Long.MAX_VALUE) : this(
        startTime,
        endTime = minOf(startTime + (beats * (60.0 / tempo * 1000)).toLong(), maxEnd),
        beats
    )

    override fun writePeriod(sound: FloatArray): FloatArray {
        val sampleRate = 48000
        val duration = (endTime - startTime) / 1000.0
       return FloatArray((sampleRate * duration).toInt())
    }
}