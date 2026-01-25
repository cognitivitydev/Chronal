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

package dev.cognitivity.chronal.rhythm.player

import android.content.Context
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MetronomePcmSource(
    private val context: Context,
    initialRhythm: PlayerRhythm
) {
    private var rhythm = initialRhythm
    private var samplePos = 0L
    private var samples: FloatArray = FloatArray(0)

    init {
        rebuildSamples()
    }

    fun updateRhythm(newRhythm: PlayerRhythm) {
        rhythm = newRhythm
        rebuildSamples()
        samplePos = 0L
    }

    fun seekTo(frame: Long) {
        samplePos = frame.coerceAtMost(samples.size.toLong())
    }

    fun read(out: FloatArray, frames: Int) {
        out.fill(0f)
        if (samples.isEmpty()) return

        val endPos = (samplePos + frames).coerceAtMost(samples.size.toLong())
        val len = (endPos - samplePos).toInt()
        System.arraycopy(samples, samplePos.toInt(), out, 0, len)
        samplePos += frames
    }

    private fun rebuildSamples() {
        val tempFile = File(context.cacheDir, "metronome_temp.wav")
        rhythm.toWav(tempFile)
        samples = readWavStream(tempFile.inputStream())
    }

    private fun readWavStream(inputStream: InputStream): FloatArray {
        val content = inputStream.readBytes()
        val dataIndex = content.indexOfSequence("data".toByteArray())
        if (dataIndex < 0) return FloatArray(0)

        val dataSize = ByteBuffer.wrap(content, dataIndex + 4, 4)
            .order(ByteOrder.LITTLE_ENDIAN).int
        val dataStart = dataIndex + 8
        val byteBuffer = ByteBuffer.wrap(content, dataStart, dataSize).order(ByteOrder.LITTLE_ENDIAN)

        val floatData = FloatArray(dataSize / 2)
        var i = 0
        while (byteBuffer.remaining() >= 2) {
            val sample = byteBuffer.short
            floatData[i++] = sample / 32768f
        }
        return floatData
    }

    private fun ByteArray.indexOfSequence(seq: ByteArray): Int {
        outer@ for (i in 0..this.size - seq.size) {
            for (j in seq.indices) if (this[i + j] != seq[j]) continue@outer
            return i
        }
        return -1
    }
}
