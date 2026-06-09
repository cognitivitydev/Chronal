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

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.TarsosDSPAudioInputStream

class AndroidAudioInputStream(
    private val audioRecord: AudioRecord
) : TarsosDSPAudioInputStream {
    companion object {
        @RequiresPermission(Manifest.permission.RECORD_AUDIO)
        fun getAudioRecord(sampleRate: Int, bufferSize: Int): AudioRecord {
            val minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            return AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize.coerceAtLeast(bufferSize * 2)
            )
        }
    }

    private val format = TarsosDSPAudioFormat(
        audioRecord.sampleRate.toFloat(),
        16,
        1,
        true,
        false
    )
    override fun getFormat(): TarsosDSPAudioFormat { return format }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return audioRecord.read(b, off, len)
    }

    override fun close() {
        audioRecord.stop()
        audioRecord.release()
        Log.i("AndroidAudioInputStream", "Stopped AudioRecord")
    }

    override fun getFrameLength(): Long { return -1 }
    override fun skip(bytesToSkip: Long): Long { return 0 }
}