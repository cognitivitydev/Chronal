/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2025-2026  cognitivity
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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.metronome.modifiers.CountIn
import dev.cognitivity.chronal.metronome.sound.Sound
import dev.cognitivity.chronal.metronome.tracks.ClickTrack
import dev.cognitivity.chronal.metronome.tracks.ClickTrack.Companion.MAX_BPM
import dev.cognitivity.chronal.metronome.tracks.ClickTrack.Companion.MIN_BPM
import dev.cognitivity.chronal.metronome.tracks.MetronomeTrack
import dev.cognitivity.chronal.rhythm.metronome.Beat
import dev.cognitivity.chronal.round
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.SimpleRhythm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import dev.cognitivity.chronal.metronome.tracks.AudioTrack as MetronomeAudioTrack

class Metronome(
    private val context: Context,
    private val sendNotifications: Boolean = true,
    bpm: Float = 60f,
    tracks: MutableList<MetronomeTrack>
) : BroadcastReceiver() {

    private val sampleRate = 48000
    private var audioTrack = getAudioTrack()
    private val frameSize = 256
    private var writeHeadSample = 0L

    private var handlerThread = HandlerThread("metronome")
    private var handler: Handler

    var playing = false
    var timestamp = 0L

    var preparing by mutableStateOf(false)
    var preparationProgress by mutableFloatStateOf(0f)

    private val tickSoundCache = mutableMapOf<String, FloatArray>()

    private class OngoingSound(var samples: FloatArray, var pos: Int)
    private val ongoingSounds = mutableListOf<OngoingSound>()

    private val schedulerLock = Any()

    private var _bpm = mutableFloatStateOf(bpm.coerceIn(MIN_BPM, MAX_BPM))
    var bpm: Float
        get() = synchronized(schedulerLock) { _bpm.floatValue }
        set(value) {
            val newValue = value.round(2).coerceIn(MIN_BPM, MAX_BPM)
            synchronized(schedulerLock) {
                val oldValue = _bpm.floatValue
                if (oldValue == newValue) return

                _bpm.floatValue = newValue
                if (playing) {
                    resyncTempo(oldValue, newValue)
                }
            }
        }

    private var _tracks = tracks.toMutableStateList()
    var tracks: MutableList<MetronomeTrack>
        get() = _tracks
        set(value) {
            _tracks = value.toMutableStateList()
        }

    private val audioStreams = mutableMapOf<MetronomeAudioTrack, AudioTrackStreamer>()

    var modifiers: MutableSet<MetronomeModifier> = mutableSetOf()

    private val _countInBeats = MutableStateFlow(0)
    val countInBeats: StateFlow<Int> = _countInBeats

    private val _countInTotalBeats = MutableStateFlow(0)
    val countInTotalBeats: StateFlow<Int> = _countInTotalBeats

    private val _countInActive = MutableStateFlow(false)
    val countInActive: StateFlow<Boolean> = _countInActive

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        if (sendNotifications) {
            ContextCompat.registerReceiver(context, this, IntentFilter("dev.cognitivity.chronal.PlayPause"), ContextCompat.RECEIVER_EXPORTED)
            ContextCompat.registerReceiver(context, this, IntentFilter("dev.cognitivity.chronal.Stop"), ContextCompat.RECEIVER_EXPORTED)
        }

        CoroutineScope(Dispatchers.IO).launch {
            prepareSounds()
        }
    }

    fun start() {
        if(playing || preparing) return

        CoroutineScope(Dispatchers.IO).launch {
            prepareSounds()

            modifiers.forEach { it.onStart() }

            synchronized(schedulerLock) {
                timestamp = System.currentTimeMillis()
                playing = true

                val countInModifier = modifiers.find { it is CountIn } as? CountIn
                if(countInModifier != null) {
                    _countInTotalBeats.value = countInModifier.beats
                    _countInBeats.value = 0
                    _countInActive.value = true
                }

                writeHeadSample = 0L

                val currentSamplePos = 0L
                tracks.forEach { track ->
                    if(track is ClickTrack) {
                        track.index = -1
                        track.nextBeatSample = currentSamplePos
                        track.sampleRemainder = 0.0
                    } else if(track is MetronomeAudioTrack) {
                        val streamer = AudioTrackStreamer(context, track, sampleRate, bpm)
                        streamer.prepare()
                        streamer.decodeChunk()
                        audioStreams[track] = streamer
                    }
                }

                ongoingSounds.clear()
            }

            audioTrack.play()
            handler.post(audioRunnable)

            tracks.forEach { it.onPause(false) }
            if (sendNotifications) sendRunningNotification()
        }
    }

    fun stop() {
        playing = false
        handler.removeCallbacks(audioRunnable)

        synchronized(schedulerLock) {
            ongoingSounds.clear()
        }

        audioStreams.values.forEach { it.release() }
        audioStreams.clear()

        tracks.forEach { it.onPause(true) }
        modifiers.forEach { it.onStop() }
        audioTrack.pause()
        audioTrack.flush()

        _countInBeats.value = 0
        _countInTotalBeats.value = 0
        _countInActive.value = false

        if (sendNotifications) sendRunningNotification()
    }

    private fun getAudioTrack(): AudioTrack {
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
            .setBufferSizeInBytes(AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT))
            .setTransferMode(AudioTrack.MODE_STREAM).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                }
            }
            .build()
    }

    private val audioRunnable: Runnable = Runnable {
        val outputBuffer = FloatArray(frameSize)

        synchronized(schedulerLock) {
            if(!playing) return@Runnable

            val played = audioTrack.playbackHeadPosition.toLong()
            if (writeHeadSample < played) writeHeadSample = played

            val frameStartSample = writeHeadSample
            val frameEndSample = frameStartSample + frameSize

            mixOngoingSounds(outputBuffer)
            mixTracks(outputBuffer, frameStartSample, frameEndSample)

            writeHeadSample += frameSize
        }

        writeAudio(outputBuffer)
        if(playing) {
            handler.post(audioRunnable)
        }
    }

    private suspend fun prepareSounds() {
        if(preparing) return
        preparing = true

        val clickTracks = tracks.filterIsInstance<ClickTrack>()
        val totalSounds = clickTracks.sumOf { it.soundPack.assets.size }
        var preparedSounds = 0

        withContext(Dispatchers.IO) {
            clickTracks.forEachIndexed { trackIndex, track ->
                val pack = track.soundPack
                for(sound in pack.assets) {
                    if(!tickSoundCache.containsKey(sound.key)) {
                        getTickSound(trackIndex, sound.pitch)
                    }
                    preparationProgress = ++preparedSounds / totalSounds.toFloat()
                }
            }
            preparing = false
            preparationProgress = 0f
        }
    }

    private fun mixOngoingSounds(outputBuffer: FloatArray) {
        val iterator = ongoingSounds.iterator()
        while (iterator.hasNext()) {
            val sound = iterator.next()
            val remainingSamples = sound.samples.size - sound.pos
            if (remainingSamples <= 0) {
                iterator.remove()
                continue
            }

            val samplesToMix = min(remainingSamples, frameSize)
            for (i in 0 until samplesToMix) {
                outputBuffer[i] += sound.samples[sound.pos + i]
            }

            sound.pos += samplesToMix
            if (sound.pos >= sound.samples.size) iterator.remove()
        }
    }

    private fun mixTracks(outputBuffer: FloatArray, frameStartSample: Long, frameEndSample: Long) {
        if(_countInActive.value) {
            mixCountIn(outputBuffer, frameStartSample, frameEndSample)
            return
        }
        for((trackIndex, track) in tracks.withIndex()) {
            if (!track.enabled) continue

            when(track) {
                is ClickTrack -> {
                    mixClickTrack(track, outputBuffer, frameStartSample, frameEndSample, trackIndex)
                }
                is MetronomeAudioTrack -> {
                    mixAudioTrack(track, outputBuffer)
                }
            }
        }
    }

    private fun mixClickTrack(track: ClickTrack, outputBuffer: FloatArray, frameStartSample: Long, frameEndSample: Long, trackIndex: Int) {
        val pattern = track.getIntervals()
        if (pattern.isEmpty()) return

        while (track.nextBeatSample < frameEndSample) {
            if (track.nextBeatSample < frameStartSample - sampleRate) {
                track.nextBeatSample = frameStartSample
            }

            val beatIndex = (track.index + 1).mod(pattern.size)
            val beat = pattern[beatIndex]
            track.index = beatIndex

            if (beat.duration >= 0) {
                mixTick(outputBuffer, frameStartSample, track.nextBeatSample, trackIndex, beat.pitch)
            }
            track.onUpdate(beat)
            modifiers.forEach { it.onTick(track, beat) }

            nextBeat(track, beat)

            if (track.nextBeatSample - frameStartSample > frameSize * 1024L) break
        }
    }

    private fun mixAudioTrack(track: MetronomeAudioTrack, outputBuffer: FloatArray) {
        val streamer = audioStreams[track] ?: return
        while(streamer.getAvailableSamples() < frameSize && !streamer.isEOF) {
            streamer.decodeChunk()
        }
        streamer.readSamples(outputBuffer, frameSize, track.volume)
    }

    private fun mixCountIn(outputBuffer: FloatArray, frameStartSample: Long, frameEndSample: Long) {
        if(_countInActive.value) {
            val mainClickTrack = tracks.first { it is ClickTrack } as ClickTrack

            val subdivision = mainClickTrack.getRhythm().measures[0].timeSig.second
            val rhythm = SimpleRhythm(
                timeSignature = _countInTotalBeats.value to subdivision,
                subdivision = subdivision,
                emphasis = 1
            ).asRhythm()
            val pattern = mainClickTrack.calculateIntervals(rhythm)

            while(mainClickTrack.nextBeatSample < frameEndSample) {
                if(_countInBeats.value >= _countInTotalBeats.value) {
                    _countInActive.value = false

                    tracks.forEach { track ->
                        if(track !is ClickTrack) return@forEach
                        track.nextBeatSample = mainClickTrack.nextBeatSample
                        track.index = -1
                        track.sampleRemainder = 0.0
                    }
                    break
                }

                if(frameStartSample - sampleRate > mainClickTrack.nextBeatSample) {
                    mainClickTrack.nextBeatSample = frameStartSample
                }

                val pitch = if(_countInBeats.value == 0) 0 else 1
                mixTick(outputBuffer, frameStartSample, mainClickTrack.nextBeatSample, 0, pitch)

                _countInBeats.value++

                val currentBeat = pattern.getOrNull((mainClickTrack.index + 1).mod(pattern.size)) ?: continue
                nextBeat(mainClickTrack, currentBeat)
            }
            return
        }
    }

    private fun mixTick(outputBuffer: FloatArray, frameStartSample: Long, beatSample: Long, trackIndex: Int, pitch: Int) {
        val tickSamples = getTickSound(trackIndex, pitch)
        if (tickSamples.isEmpty()) return

        val frameOffset = (beatSample - frameStartSample).toInt()

        if (frameOffset >= 0) {
            val mixLength = min(tickSamples.size, frameSize - frameOffset)
            for (i in 0 until mixLength) {
                outputBuffer[frameOffset + i] += tickSamples[i]
            }
            if (mixLength < tickSamples.size) {
                ongoingSounds.add(OngoingSound(tickSamples, mixLength))
            }
        } else {
            val tickStart = -frameOffset
            if (tickStart >= tickSamples.size) return

            val mixLength = min(tickSamples.size - tickStart, frameSize)
            for (i in 0 until mixLength) {
                outputBuffer[i] += tickSamples[tickStart + i]
            }
            if (tickStart + mixLength < tickSamples.size) {
                ongoingSounds.add(OngoingSound(tickSamples, tickStart + mixLength))
            }
        }
    }

    private fun nextBeat(track: ClickTrack, beat: Beat) {
        val beatLength = abs(beat.duration) * track.beatValue * 60.0 / bpm.toDouble()

        val exactSamples = beatLength * sampleRate + track.sampleRemainder
        val roundedSamples = round(exactSamples).toLong()

        track.sampleRemainder = exactSamples - roundedSamples
        track.nextBeatSample += max(1L, roundedSamples)
    }

    private fun resyncTempo(oldBpm: Float, newBpm: Float) {
        tracks.forEach { track ->
            when(track) {
                is ClickTrack -> {
                    val pattern = track.getIntervals()
                    if(pattern.isEmpty()) return@forEach

                    val currentBeatIndex = track.index.coerceAtLeast(0).coerceAtMost(pattern.lastIndex)
                    val currentBeat = pattern[currentBeatIndex]

                    val oldBeatSamples = beatLengthToSamples(currentBeat, track.beatValue, oldBpm)
                    val newBeatSamples = beatLengthToSamples(currentBeat, track.beatValue, newBpm)

                    if(oldBeatSamples <= 0.0 || newBeatSamples <= 0.0) {
                        track.nextBeatSample = writeHeadSample + 1
                        track.sampleRemainder = 0.0
                        return@forEach
                    }

                    val currentBeatStart = track.nextBeatSample.toDouble() - oldBeatSamples
                    val elapsedSamples = (writeHeadSample.toDouble() - currentBeatStart).coerceIn(0.0, oldBeatSamples)
                    val remainingFraction = 1.0 - (elapsedSamples / oldBeatSamples)
                    val remainingSamples = max(1.0, newBeatSamples * remainingFraction)

                    track.nextBeatSample = writeHeadSample + round(remainingSamples).toLong()
                    track.sampleRemainder = 0.0
                }
                is MetronomeAudioTrack -> {
                    audioStreams[track]?.updateBpm(newBpm)
                }
            }
        }
    }

    private fun beatLengthToSamples(beat: Beat, beatValue: Float, bpm: Float): Double {
        return abs(beat.duration) * beatValue * 60.0 / bpm.toDouble() * sampleRate
    }

    private fun writeAudio(buffer: FloatArray) {
        try {
            var writtenSamples = 0
            while (writtenSamples < buffer.size && playing) {
                val result = audioTrack.write(
                    buffer,
                    writtenSamples,
                    buffer.size - writtenSamples,
                    AudioTrack.WRITE_BLOCKING
                )
                if (result < 0) {
                    stop()
                    throw IllegalStateException("Failed to write audio data ($result)")
                }
                writtenSamples += result
            }
        } catch (e: Exception) {
            Log.e("Metronome", "Failed to write mixed audio data", e)
        }
    }

    private fun getTickSound(trackIndex: Int, pitch: Int): FloatArray {
        val pack = (tracks[trackIndex] as? ClickTrack)?.soundPack ?: return FloatArray(0)
        val sound = pack.getSound(pitch) ?: return FloatArray(0)
        tickSoundCache[sound.key]?.let { return it }

        val extractor = MediaExtractor()
        when(sound) {
            is Sound.Resource -> {
                if(sound.resId == 0) return FloatArray(0)
                val afd = context.resources.openRawResourceFd(sound.resId) ?: return FloatArray(0)
                afd.use { descriptor ->
                    extractor.setDataSource(
                        descriptor.fileDescriptor,
                        descriptor.startOffset,
                        descriptor.length
                    )
                }
            }
            is Sound.File -> {
                val file = File(context.filesDir, sound.relativePath)
                if(!file.isFile) return FloatArray(0)
                extractor.setDataSource(file.absolutePath)
            }
        }

        return try {
            val data = decodeAudio(extractor, sampleRate, 1)

            tickSoundCache[sound.key] = data
            data
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun decodeAudio(extractor: MediaExtractor, targetSampleRate: Int, targetChannels: Int): FloatArray {
        var trackIndex = -1
        var trackFormat: MediaFormat? = null

        for(i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if(mime.startsWith("audio/")) {
                trackIndex = i
                trackFormat = format
                break
            }
        }
        if(trackIndex == -1 || trackFormat == null) {
            extractor.release()
            return FloatArray(0)
        }
        extractor.selectTrack(trackIndex)

        val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""
        val decoder = MediaCodec.createDecoderByType(mime)

        decoder.configure(trackFormat, null, null, 0)
        decoder.start()

        val byteBufferStream = ByteArrayOutputStream()
        val info = MediaCodec.BufferInfo()
        var isEOS = false

        var sourceSampleRate = trackFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        var sourceChannels = trackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        while(true) {
            if(!isEOS) {
                val inIndex = decoder.dequeueInputBuffer(10000)
                if(inIndex >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inIndex)
                    if(inputBuffer != null) {
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if(sampleSize < 0) {
                            decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEOS = true
                        } else {
                            decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }
            }

            val outIndex = decoder.dequeueOutputBuffer(info, 10000)
            if(outIndex >= 0) {
                val outputFormat = decoder.getOutputFormat(outIndex)
                if(outputFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                    sourceSampleRate = outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                }
                if(outputFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                    sourceChannels = outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                }

                val outputBuffer = decoder.getOutputBuffer(outIndex)
                if(outputBuffer != null && info.size > 0) {
                    outputBuffer.position(info.offset)
                    outputBuffer.limit(info.offset + info.size)

                    val chunk = ByteArray(info.size)
                    outputBuffer.get(chunk)
                    byteBufferStream.write(chunk)
                }
                decoder.releaseOutputBuffer(outIndex, false)

                if((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
            }
        }

        decoder.stop()
        decoder.release()
        extractor.release()

        val shortBuffer = ByteBuffer.wrap(byteBufferStream.toByteArray())
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()

        val rawFloatSamples = FloatArray(shortBuffer.remaining())
        for(i in rawFloatSamples.indices) {
            rawFloatSamples[i] = shortBuffer.get() / 32768.0f
        }

        val channelAdjusted = convertChannels(rawFloatSamples, sourceChannels, targetChannels)

        return resampleAudio(channelAdjusted, sourceSampleRate, targetSampleRate, targetChannels)
    }

    private fun convertChannels(input: FloatArray, srcChannels: Int, dstChannels: Int): FloatArray {
        if(srcChannels == dstChannels) return input

        val frameCount = input.size / srcChannels
        val output = FloatArray(frameCount * dstChannels)

        when(srcChannels) {
            2 if dstChannels == 1 -> {
                for (i in 0 until frameCount) {
                    val left = input[i * 2]
                    val right = input[i * 2 + 1]
                    output[i] = (left + right) / 2.0f
                }
            }
            1 if dstChannels == 2 -> {
                for (i in 0 until frameCount) {
                    val sample = input[i]
                    output[i * 2] = sample
                    output[i * 2 + 1] = sample
                }
            }
            else -> {
                for (i in 0 until frameCount) {
                    for (ch in 0 until dstChannels) {
                        output[i * dstChannels + ch] = input[i * srcChannels]
                    }
                }
            }
        }

        return output
    }

    private fun resampleAudio(
        input: FloatArray,
        srcRate: Int,
        dstRate: Int,
        channels: Int
    ): FloatArray {
        if (srcRate == dstRate) return input

        val inputFrames = input.size / channels
        val outputFrames = (inputFrames.toLong() * dstRate / srcRate).toInt()
        val output = FloatArray(outputFrames * channels)

        val ratio = srcRate.toDouble() / dstRate.toDouble()

        for (outFrame in 0 until outputFrames) {
            val srcPos = outFrame * ratio
            val index0 = srcPos.toInt()
            val index1 = (index0 + 1).coerceAtMost(inputFrames - 1)
            val fraction = (srcPos - index0).toFloat()

            for (ch in 0 until channels) {
                val sample0 = input[index0 * channels + ch]
                val sample1 = input[index1 * channels + ch]

                output[outFrame * channels + ch] = sample0 + fraction * (sample1 - sample0)
            }
        }

        return output
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Metronome controls"
            val descriptionText = "Metronome controls for background playback"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("PlayingBackground", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendRunningNotification() {
        if(!Settings.METRONOME_CONTROLS_NOTIFICATION.get()) return
        createNotificationChannel()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val pauseIntent = Intent("dev.cognitivity.chronal.PlayPause")
        val pausePendingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent("dev.cognitivity.chronal.Stop")
        val stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) "PlayingBackground" else ""

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_music_note_24)
            .setContentTitle(context.getString(R.string.metronome_notification_title, bpm.toInt()))
            .setContentText(context.getString(if (this.playing) R.string.metronome_notification_playing else R.string.metronome_notification_paused))
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, context.getString(if (this.playing) R.string.generic_pause else R.string.generic_resume), pausePendingIntent)
            .addAction(0, context.getString(R.string.generic_stop), stopPendingIntent)
            .setUsesChronometer(true)

        notificationManager.notify(1, builder.build())
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "dev.cognitivity.chronal.PlayPause") {
            if (playing) {
                stop()
            } else {
                start()
            }
        }
        if (intent?.action == "dev.cognitivity.chronal.Stop") {
            if (playing) stop()
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(1)
        }
    }
}