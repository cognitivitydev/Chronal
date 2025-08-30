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

class SetTempo(
    startTime: Long,
    endTime: Long,
    beats: Int? = null,
    val tempo: Int,
) : PlayerRhythmElement(startTime, endTime, beats) {

    constructor(startTime: Long, tempo: Int, beats: Int, maxEnd: Long = Long.MAX_VALUE) : this(
        startTime,
        endTime = minOf((startTime + (beats * (60.0 / tempo * 1000)).toLong()), maxEnd),
        beats = minOf(beats, ((maxEnd - startTime) / (60.0 / tempo * 1000)).toInt()),
        tempo,
    )

    override fun writePeriod(sound: FloatArray): FloatArray {
        val sampleRate = 48000
        val durationMs = endTime - startTime
        val totalSamples = (durationMs * sampleRate / 1000).toInt()
        val output = FloatArray(totalSamples)

        val beatIntervalMs = 60_000.0 / tempo
        val beatIntervalSamples = (beatIntervalMs * sampleRate / 1000).toInt()

        var pos = 0
        while (pos < totalSamples) {
            val copyLen = minOf(sound.size, totalSamples - pos)
            System.arraycopy(sound, 0, output, pos, copyLen)
            pos += beatIntervalSamples
        }

        return output
    }
}