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

package dev.cognitivity.chronal.metronome

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import dev.cognitivity.chronal.metronome.tracks.AudioTrack
import java.nio.ByteOrder
import kotlin.math.abs

class AudioTrackStreamer(
    private val context: Context,
    private val track: AudioTrack,
    private val targetSampleRate: Int,
    @Volatile private var metronomeBpm: Float
) {
    companion object {
        private const val RING_BUFFER_SIZE = 32768
        private const val MIN_SPEED = 0.05f
    }

    private val lock = Any()

    private val ringBuffer = FloatArray(RING_BUFFER_SIZE)
    private var writeCursor = 0L
    private var readCursor = 0L

    private var extractor: MediaExtractor? = null
    private var decoder: MediaCodec? = null
    private val bufferInfo = MediaCodec.BufferInfo()

    private var currentStretchFactor = 1.0
    private val stretchFactor: Double
        get() {
            return metronomeBpm / (track.bpm ?: metronomeBpm).toDouble()
        }

    private val audioFormat = TarsosDSPAudioFormat(targetSampleRate.toFloat(), 16, 1, true, false)
    private var processor = WaveformSimilarityBasedOverlapAdd(
        WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(currentStretchFactor, targetSampleRate.toDouble())
    )

    private var stretchStagingBuffer = FloatArray(16384)
    private var stretchStagingPos = 0
    private var isFirstStretchedFrame = true

    private var monoBuffer = FloatArray(16384)
    private var resampleBuffer = FloatArray(16384)

    private var sourceChannels = 1
    private var sourceSampleRate = targetSampleRate
    private var trackDuration = 0L

    private var delaySamplesRemaining = 0L
    private var skipSamplesRemaining = 0L
    private var endTrimLimitSamples = Long.MAX_VALUE
    private var endSilenceSamplesRemaining = 0L
    private var hasReachedAudioEnd = false

    @Volatile private var isReleased = false
    @Volatile var isEOF = false
        private set

    fun prepare() {
        val newExtractor = MediaExtractor().apply {
            setDataSource(context, track.uri, null)
        }
        this.extractor = newExtractor
        this.isFirstStretchedFrame = true

        var trackIndex = -1
        for(i in 0 until newExtractor.trackCount) {
            val mime = newExtractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME) ?: ""
            if(mime.startsWith("audio/")) {
                trackIndex = i
                break
            }
        }
        if(trackIndex == -1) return
        newExtractor.selectTrack(trackIndex)

        val format = newExtractor.getTrackFormat(trackIndex)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: return

        sourceChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        sourceSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        trackDuration = format.getLong(MediaFormat.KEY_DURATION)

        seekToStart(newExtractor)

        decoder = MediaCodec.createDecoderByType(mime).apply {
            configure(format, null, null, 0)
            start()
        }
    }

    private fun seekToStart(extractor: MediaExtractor) {
        val startTrim = track.startTrim

        when {
            (startTrim > 0f) -> {
                val targetMicros = (startTrim * 1_000_000).toLong()
                extractor.seekTo(targetMicros, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

                val actualMicros = extractor.sampleTime.coerceAtLeast(0L)
                val deltaSeconds = ((targetMicros - actualMicros) / 1_000_000.0).coerceAtLeast(0.0)

                skipSamplesRemaining = (deltaSeconds * targetSampleRate).toLong()
                delaySamplesRemaining = 0L
            }
            (startTrim < 0f) -> {
                extractor.seekTo(0L, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                val secondsToDelay = abs(startTrim.toDouble())
                delaySamplesRemaining = (secondsToDelay * targetSampleRate / stretchFactor).toLong()
                skipSamplesRemaining = 0L
            }
            else -> {
                extractor.seekTo(0L, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                skipSamplesRemaining = 0L
                delaySamplesRemaining = 0L
            }
        }

        if(track.endTrim > 0f && trackDuration > 0L) {
            val durationSeconds = trackDuration / 1_000_000.0
            val effectiveDuration = (durationSeconds - track.endTrim - track.startTrim).coerceAtLeast(0.1)
            endTrimLimitSamples = (effectiveDuration * targetSampleRate).toLong()
        } else {
            endTrimLimitSamples = Long.MAX_VALUE
        }
    }

    fun decodeChunk() {
        if(isEOF || isReleased) return

        synchronized(lock) {
            if((writeCursor - readCursor) >= RING_BUFFER_SIZE / 2) return
        }

        if(delaySamplesRemaining > 0L) {
            writeStartSilence()
            return
        }
        if(skipSamplesRemaining > 0L) {
            writeEndSilence()
            return
        }
        if(hasReachedAudioEnd) return

        val (currentExtractor, currentDecoder) = synchronized(lock) {
            if(isReleased) return
            (extractor ?: return) to (decoder ?: return)
        }

        try {
            feedInputBuffer(currentExtractor, currentDecoder)
            drainOutputBuffer(currentExtractor, currentDecoder)
        } catch(exception: Exception) {
            Log.e("AudioStreamer", "Error processing audio chunk", exception)
        }
    }

    private fun feedInputBuffer(extractor: MediaExtractor, decoder: MediaCodec) {
        val inIndex = decoder.dequeueInputBuffer(1000L)
        if(inIndex < 0) return

        val inputBuffer = decoder.getInputBuffer(inIndex) ?: return
        val sampleSize = extractor.readSampleData(inputBuffer, 0)

        if(sampleSize < 0) {
            decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
        } else {
            decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
            extractor.advance()
        }
    }

    private fun drainOutputBuffer(extractor: MediaExtractor, decoder: MediaCodec) {
        val outIndex = decoder.dequeueOutputBuffer(bufferInfo, 1000L)
        if(outIndex < 0) return

        val outputBuffer = decoder.getOutputBuffer(outIndex)
        if(outputBuffer != null && bufferInfo.size > 0) {
            outputBuffer.position(bufferInfo.offset)
            outputBuffer.limit(bufferInfo.offset + bufferInfo.size)

            val shortBuf = outputBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            val rawShorts = ShortArray(shortBuf.remaining())
            shortBuf.get(rawShorts)

            processAudioData(rawShorts)
        }

        if(!isReleased) {
            decoder.releaseOutputBuffer(outIndex, false)
        }

        if((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            handleEndOfStream(extractor, decoder)
        }
    }

    private fun writeStartSilence() {
        val count = minOf(delaySamplesRemaining, 512L).toInt()
        val written = writeSilence(count)
        delaySamplesRemaining -= written
    }

    private fun writeEndSilence() {
        val count = minOf(endSilenceSamplesRemaining, 512L).toInt()
        val written = writeSilence(count)
        endSilenceSamplesRemaining -= written

        if(endSilenceSamplesRemaining <= 0L) {
            val currentExtractor = extractor
            val currentDecoder = decoder
            if(currentExtractor != null && currentDecoder != null) {
                finishStream(currentExtractor, currentDecoder)
            } else {
                isEOF = true
            }
        }
    }

    private fun writeSilence(count: Int): Int = synchronized(lock) {
        if(isReleased) return 0
        val availableCapacity = RING_BUFFER_SIZE - (writeCursor - readCursor).toInt()
        val toWrite = minOf(count, availableCapacity)

        repeat(toWrite) {
            ringBuffer[(writeCursor and (RING_BUFFER_SIZE-1).toLong()).toInt()] = 0f
            writeCursor++
        }
        return toWrite
    }

    private fun processAudioData(rawShorts: ShortArray) {
        var sampleCount = convertToMonoFloats(rawShorts, sourceChannels)
        var pcmFloats = monoBuffer

        if(sourceSampleRate != targetSampleRate) {
            sampleCount = linearResample(pcmFloats, sampleCount, sourceSampleRate, targetSampleRate)
            pcmFloats = resampleBuffer
        }

        if(skipSamplesRemaining > 0L) {
            if(sampleCount <= skipSamplesRemaining) {
                skipSamplesRemaining -= sampleCount
                return
            }
            val trimAmount = skipSamplesRemaining.toInt()
            sampleCount -= trimAmount
            System.arraycopy(pcmFloats, trimAmount, pcmFloats, 0, sampleCount)
            skipSamplesRemaining = 0L
        }

        if(endTrimLimitSamples != Long.MAX_VALUE) {
            if(sampleCount >= endTrimLimitSamples) {
                sampleCount = endTrimLimitSamples.toInt()
                endTrimLimitSamples = 0L

                applyTimeStretch(pcmFloats, sampleCount)
                handleEndOfStream(extractor ?: return, decoder ?: return)
                return
            }
            endTrimLimitSamples -= sampleCount
        }

        applyTimeStretch(pcmFloats, sampleCount)
    }

    private fun convertToMonoFloats(shorts: ShortArray, channels: Int): Int {
        val frameCount = shorts.size / channels
        if(monoBuffer.size < frameCount) {
            monoBuffer = FloatArray(frameCount)
        }

        if(channels == 2) {
            for(i in 0 until frameCount) {
                val left = shorts[i * 2] / 32768.0f
                val right = shorts[i * 2 + 1] / 32768.0f
                monoBuffer[i] = (left + right) * 0.5f
            }
        } else {
            for(i in 0 until frameCount) {
                monoBuffer[i] = shorts[i * channels] / 32768.0f
            }
        }
        return frameCount
    }

    private fun linearResample(input: FloatArray, inputLength: Int, srcRate: Int, dstRate: Int): Int {
        if(srcRate == dstRate || inputLength == 0) return inputLength

        val outputFrames = (inputLength.toLong() * dstRate / srcRate).toInt()
        if(resampleBuffer.size < outputFrames) {
            resampleBuffer = FloatArray(outputFrames)
        }

        val ratio = srcRate.toDouble() / dstRate.toDouble()
        val lastIndex = inputLength - 1

        for(i in 0 until outputFrames) {
            val srcPos = i * ratio
            val index0 = srcPos.toInt()
            val index1 = (index0 + 1).coerceAtMost(lastIndex)
            val fraction = (srcPos - index0).toFloat()

            resampleBuffer[i] = input[index0] + fraction * (input[index1] - input[index0])
        }
        return outputFrames
    }

    private fun applyTimeStretch(inputFloats: FloatArray, length: Int) {
        if(length <= 0) return

        val targetFactor = stretchFactor
        if(targetFactor != currentStretchFactor) {
            currentStretchFactor = targetFactor
            processor = WaveformSimilarityBasedOverlapAdd(
                WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(
                    targetFactor,
                    targetSampleRate.toDouble()
                )
            )
        }

        if(targetFactor == 1.0 || targetFactor < MIN_SPEED) {
            synchronized(lock) { pushToRingBufferLocked(inputFloats, 0, length) }
            return
        }

        if(stretchStagingPos + length > stretchStagingBuffer.size) {
            val newCapacity = stretchStagingPos + length + 8192
            stretchStagingBuffer = stretchStagingBuffer.copyOf(newCapacity)
        }
        System.arraycopy(inputFloats, 0, stretchStagingBuffer, stretchStagingPos, length)
        stretchStagingPos += length

        processStretchedFrames()
    }

    private fun processStretchedFrames() {
        val inputBufferSize = processor.inputBufferSize
        val stepSize = inputBufferSize - processor.overlap
        val chunk = FloatArray(inputBufferSize)

        while(stretchStagingPos >= inputBufferSize) {
            System.arraycopy(stretchStagingBuffer, 0, chunk, 0, inputBufferSize)

            val remainder = stretchStagingPos - stepSize
            System.arraycopy(stretchStagingBuffer, stepSize, stretchStagingBuffer, 0, remainder)
            stretchStagingPos = remainder

            pushStretchedChunk(chunk)
        }
    }

    private fun pushStretchedChunk(chunk: FloatArray) {
        val event = AudioEvent(audioFormat)
        event.floatBuffer = chunk
        processor.process(event)

        val output = event.floatBuffer
        if(output.isNotEmpty()) {
            synchronized(lock) {
                pushToRingBufferLocked(output, 0, output.size)
            }
        }
    }

    private fun pushToRingBufferLocked(samples: FloatArray, offset: Int, length: Int) {
        val maxToWrite = minOf(length, RING_BUFFER_SIZE - (writeCursor - readCursor).toInt())
        for(i in 0 until maxToWrite) {
            ringBuffer[(writeCursor and (RING_BUFFER_SIZE-1).toLong()).toInt()] = samples[offset + i]
            writeCursor++
        }
    }

    private fun handleEndOfStream(extractor: MediaExtractor, decoder: MediaCodec) {
        if(track.endTrim < 0f && !hasReachedAudioEnd) {
            val secondsToSilence = abs(track.endTrim.toDouble())
            endSilenceSamplesRemaining = (secondsToSilence * targetSampleRate / stretchFactor).toLong()
            hasReachedAudioEnd = true
            return
        }

        finishStream(extractor, decoder)
    }

    private fun finishStream(extractor: MediaExtractor, decoder: MediaCodec) {
        if(track.loop) {
            isFirstStretchedFrame = true
            hasReachedAudioEnd = false

            seekToStart(extractor)
            decoder.flush()
        } else {
            isEOF = true
        }
    }

    fun getAvailableSamples(): Int {
        return synchronized(lock) {
            (writeCursor - readCursor).toInt()
        }
    }

    fun readSamples(out: FloatArray, frameSize: Int, volume: Float) {
        synchronized(lock) {
            val available = (writeCursor - readCursor).toInt()
            if(available <= 0) return

            val samplesToRead = minOf(available, frameSize)
            for(i in 0 until samplesToRead) {
                val index = (readCursor and (RING_BUFFER_SIZE-1).toLong()).toInt()
                out[i] += ringBuffer[index] * volume
                readCursor++
            }
        }
    }

    fun release() {
        synchronized(lock) {
            if(isReleased) return
            isReleased = true

            try {
                decoder?.apply {
                    stop()
                    release()
                }
                extractor?.release()
            } catch(exception: Exception) {
                Log.e("AudioStreamer", "Error releasing audio resources", exception)
            } finally {
                decoder = null
                extractor = null
            }
        }
    }

    fun updateBpm(newBpm: Float) {
        synchronized(lock) {
            if (metronomeBpm == newBpm) return
            metronomeBpm = newBpm

            val newFactor = stretchFactor
            if(newFactor != currentStretchFactor) {
                currentStretchFactor = newFactor
                processor = WaveformSimilarityBasedOverlapAdd(
                    WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(
                        newFactor,
                        targetSampleRate.toDouble()
                    )
                )
            }
        }
    }
}