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
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import dev.cognitivity.chronal.R
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

class SyncedAudioEngine(
    private val context: Context,
    initialRhythm: PlayerRhythm,
    mediaUri: Uri,
    mediaVolume: Float = 0.5f,
    metronomeVolume: Float = 0.5f
) {
    private val sampleRate = 48000
    private val frameSize = 256

    private val handlerThread = HandlerThread("SyncedAudioEngine").apply { start() }
    private val handler = Handler(handlerThread.looper)

    private var audioTrack: AudioTrack = createAudioTrack()

    private var musicData: FloatArray = FloatArray(0)
    private var musicSamplePos = 0L

    private var rhythm = initialRhythm
    private var metronomeSamples: FloatArray = FloatArray(0)
    private var ongoingClicks = mutableListOf<OngoingSound>()
    private var metronomeSamplePos = 0L

    private var playingInternal = false
    private var paused = false

    private var musicVolumeInternal = mediaVolume
    private var metronomeVolumeInternal = metronomeVolume

    private var nextBeatSample = 0L
    private var sampleRemainder = 0.0

    private data class OngoingSound(var samples: FloatArray, var pos: Int)

    init {
        loadMusic(mediaUri)
        metronomeSamples = getBaseTick() // base click for rhythm
    }

    // PUBLIC API

    fun play() {
        if (playingInternal) return
        playingInternal = true
        paused = false
        audioTrack.play()
        handler.post(audioRunnable)
    }

    fun pause() {
        if (!playingInternal) return
        playingInternal = false
        paused = true
        audioTrack.pause()
    }

    fun stop() {
        playingInternal = false
        paused = false
        musicSamplePos = 0
        metronomeSamplePos = 0
        ongoingClicks.clear()
        audioTrack.pause()
        audioTrack.flush()
        nextBeatSample = 0
        sampleRemainder = 0.0
    }

    fun seekToMs(ms: Long) {
        val targetSample = (ms * sampleRate / 1000).toLong()
        musicSamplePos = targetSample.coerceIn(0, musicData.size.toLong())
        metronomeSamplePos = targetSample
        ongoingClicks.clear()
        nextBeatSample = targetSample
    }

    fun updateRhythm(newRhythm: PlayerRhythm) {
        rhythm = newRhythm
        metronomeSamplePos = musicSamplePos
        nextBeatSample = musicSamplePos
        ongoingClicks.clear()
    }

    fun setVolumes(music: Float, metronome: Float) {
        musicVolumeInternal = music
        metronomeVolumeInternal = metronome
    }

    // AUDIO LOOP

    private val audioRunnable = object : Runnable {
        override fun run() {
            if (!playingInternal) return

            val buffer = FloatArray(frameSize)
            mixMusic(buffer)
            mixMetronome(buffer)
            writeAudio(buffer)

            handler.post(this)
        }
    }

    // MUSIC

    private fun mixMusic(outputBuffer: FloatArray) {
        val remaining = musicData.size - musicSamplePos.toInt()
        val count = min(frameSize, remaining)
        for (i in 0 until count) {
            outputBuffer[i] += musicData[musicSamplePos.toInt() + i] * musicVolumeInternal
        }
        musicSamplePos += count
    }

    private fun loadMusic(uri: Uri) {
        musicData = decodeToFloatArray(uri)
        musicSamplePos = 0
    }

    private fun decodeToFloatArray(uri: Uri): FloatArray {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)
        val trackIndex = (0 until extractor.trackCount).firstOrNull {
            extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
        } ?: throw IllegalStateException("No audio track found")

        extractor.selectTrack(trackIndex)
        val format = extractor.getTrackFormat(trackIndex)
        val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
        decoder.configure(format, null, null, 0)
        decoder.start()

        val bufferInfo = MediaCodec.BufferInfo()
        val outList = mutableListOf<Float>()
        var isEOS = false

        while (!isEOS) {
            val inIndex = decoder.dequeueInputBuffer(10000)
            if (inIndex >= 0) {
                val inBuf = decoder.getInputBuffer(inIndex)!!
                val sampleSize = extractor.readSampleData(inBuf, 0)
                if (sampleSize < 0) {
                    decoder.queueInputBuffer(inIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    isEOS = true
                } else {
                    decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                    extractor.advance()
                }
            }

            var outIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
            while (outIndex >= 0) {
                val outBuf = decoder.getOutputBuffer(outIndex)!!
                val shorts = ShortArray(bufferInfo.size / 2)
                outBuf.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
                if (channels == 2) {
                    for (i in 0 until shorts.size step 2) {
                        outList.add(((shorts[i] + shorts[i + 1]) * 0.5f) / Short.MAX_VALUE)
                    }
                } else {
                    for (s in shorts) outList.add(s / Short.MAX_VALUE.toFloat())
                }
                decoder.releaseOutputBuffer(outIndex, false)
                outIndex = decoder.dequeueOutputBuffer(bufferInfo, 0)
            }
        }

        decoder.stop()
        decoder.release()
        extractor.release()

        // resample if needed
        if (format.getInteger(MediaFormat.KEY_SAMPLE_RATE) != sampleRate) {
            return Resampler.resample(outList.toFloatArray(), format.getInteger(MediaFormat.KEY_SAMPLE_RATE), sampleRate)
        }
        return outList.toFloatArray()
    }

    // METRONOME

    private fun mixMetronome(outputBuffer: FloatArray) {
        val frameStart = metronomeSamplePos
        val frameEnd = frameStart + frameSize

        while (nextBeatSample < frameEnd) {
            val beatIndex = 0 // or implement pattern iteration
            val tick = metronomeSamples
            val offset = (nextBeatSample - frameStart).toInt()
            for (i in 0 until tick.size) {
                val idx = offset + i
                if (idx in outputBuffer.indices) outputBuffer[idx] += tick[i] * metronomeVolumeInternal
                else ongoingClicks.add(OngoingSound(tick, i))
            }
            nextBeatSample += (sampleRate * 60 / 120) // example 120bpm, replace with rhythm
        }

        // mix ongoing clicks
        val iter = ongoingClicks.iterator()
        while (iter.hasNext()) {
            val s = iter.next()
            val remaining = s.samples.size - s.pos
            val mixLength = min(remaining, frameSize)
            for (i in 0 until mixLength) outputBuffer[i] += s.samples[s.pos + i] * metronomeVolumeInternal
            s.pos += mixLength
            if (s.pos >= s.samples.size) iter.remove()
        }

        metronomeSamplePos += frameSize
    }

    private fun getBaseTick(): FloatArray {
        val res = R.raw.click_hi
        val input = context.resources.openRawResource(res)
        return readWavStream(input)
    }

    private fun readWavStream(inputStream: InputStream): FloatArray {
        val content = inputStream.readBytes()
        val dataIndex = "data".toByteArray().let { data -> content.indexOfSubArray(data) }
        if (dataIndex < 0) throw RuntimeException("No data chunk found")

        val dataStart = dataIndex + 8
        val dataSize = ByteBuffer.wrap(content, dataIndex + 4, 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .int

        val shortBuffer = ByteBuffer.wrap(content, dataStart, dataSize)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()

        val shortArray = ShortArray(shortBuffer.remaining())
        shortBuffer.get(shortArray)

        return shortArray.map { it / Short.MAX_VALUE.toFloat() }.toFloatArray()
    }

    // AUDIO TRACK

    private fun createAudioTrack(): AudioTrack {
        val minBuf = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    private fun writeAudio(buffer: FloatArray) {
        var written = 0
        while (written < buffer.size && playingInternal) {
            written += audioTrack.write(buffer, written, buffer.size - written, AudioTrack.WRITE_BLOCKING)
        }
    }
}

// Helper to find subarray index
private fun ByteArray.indexOfSubArray(sub: ByteArray): Int {
    outer@ for (i in 0..size - sub.size) {
        for (j in sub.indices) if (this[i + j] != sub[j]) continue@outer
        return i
    }
    return -1
}

object Resampler {
    fun resample(input: FloatArray, inputRate: Int, outputRate: Int): FloatArray {
        if (inputRate == outputRate) return input
        val ratio = outputRate.toDouble() / inputRate
        val outputSize = (input.size * ratio).toInt()
        val output = FloatArray(outputSize)
        for (i in output.indices) {
            val srcPos = i / ratio
            val srcIndex = srcPos.toInt()
            val frac = srcPos - srcIndex
            val next = (srcIndex + 1).coerceAtMost(input.lastIndex)
            output[i] = input[srcIndex] * (1 - frac).toFloat() + input[next] * frac.toFloat()
        }
        return output
    }
}