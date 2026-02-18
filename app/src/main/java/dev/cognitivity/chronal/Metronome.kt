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

package dev.cognitivity.chronal

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
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.rhythm.metronome.Beat
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.metronome.windows.paused
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class Metronome(private val sendNotifications: Boolean = true) : BroadcastReceiver() {
    private val sampleRate = 48000

    private var audioTrack = getAudioTrack()

    var playing = false
    var active = true
    var timestamp = 0L

    private var handlerThread: HandlerThread
    private var handler: Handler

    private val tracks = mutableMapOf<Int, MetronomeTrack>()

    private val tickSoundCache = mutableMapOf<Int, FloatArray>()

    private val frameSize = 256
    private var writeHeadSample = 0L

    private class OngoingSound(var samples: FloatArray, var pos: Int)
    private val ongoingSounds = mutableListOf<OngoingSound>()

    init {
        handlerThread = HandlerThread("metronome")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        if (sendNotifications) {
            ContextCompat.registerReceiver(context, this, IntentFilter("dev.cognitivity.chronal.PlayPause"), ContextCompat.RECEIVER_EXPORTED)
            ContextCompat.registerReceiver(context, this, IntentFilter("dev.cognitivity.chronal.Stop"), ContextCompat.RECEIVER_EXPORTED)
        }
    }

    fun addTrack(id: Int, track: MetronomeTrack) {
        tracks.put(id, track)
    }

    fun removeTrack(id: Int) {
        tracks.remove(id)
    }

    fun getTrack(id: Int): MetronomeTrack = tracks[id]!! // TEMP: always returns non-null for 0 and 1 for now
    fun getTracks(): List<MetronomeTrack> = tracks.values.toList()

    fun start() {
        if (playing || !active) return

        CoroutineScope(Dispatchers.IO).launch {
            timestamp = System.currentTimeMillis()
            playing = true

            val currentSamplePos = 0L
            tracks.values.forEach { track ->
                track.index = -1
                track.nextBeatSample = currentSamplePos
                track.sampleRemainder = 0.0
            }

            ongoingSounds.clear()
            audioTrack.play()
            handler.post(audioRunnable)

            tracks.values.forEach { it.onPause(false) }
            if (sendNotifications) sendRunningNotification()
        }
    }

    fun stop() {
        playing = false
        handler.removeCallbacks(audioRunnable)
        ongoingSounds.clear()
        tracks.values.forEach { it.onPause(true) }
        audioTrack.pause()
        audioTrack.flush()
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
        if (!playing) return@Runnable

        val outputBuffer = FloatArray(frameSize)

        val played = audioTrack.playbackHeadPosition.toLong()
        if (writeHeadSample < played) writeHeadSample = played

        val frameStartSample = writeHeadSample
        val frameEndSample = frameStartSample + frameSize

        mixOngoingSounds(outputBuffer)
        mixTracks(outputBuffer, frameStartSample, frameEndSample)
        writeAudio(outputBuffer)

        writeHeadSample += frameSize
        handler.post(audioRunnable)
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
        for (track in tracks.values) {
            if (!track.enabled) continue

            val pattern = track.getIntervals()
            if (pattern.isEmpty()) continue

            while (track.nextBeatSample < frameEndSample) {
                if (track.nextBeatSample < frameStartSample - sampleRate) {
                    track.nextBeatSample = frameStartSample
                }

                val beatIndex = (track.index + 1).mod(pattern.size)
                val beat = pattern[beatIndex]
                track.index = beatIndex

                if (beat.duration >= 0) {
                    mixTick(outputBuffer, frameStartSample, track.nextBeatSample, beat.isHigh)
                }
                track.onUpdate(beat)

                nextBeat(track, beat)

                if (track.nextBeatSample - frameStartSample > frameSize * 1024L) break
            }
        }
    }

    private fun mixTick(outputBuffer: FloatArray, frameStartSample: Long, beatSample: Long, isHigh: Boolean) {
        val tickSamples = getTickSound(isHigh)
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

    private fun nextBeat(track: MetronomeTrack, beat: Beat) {
        val beatLength = abs(beat.duration) * track.beatValue * 60.0 / track.bpm.toDouble()

        val exactSamples = beatLength * sampleRate + track.sampleRemainder
        val roundedSamples = round(exactSamples).toLong()

        track.sampleRemainder = exactSamples - roundedSamples
        track.nextBeatSample += max(1L, roundedSamples)
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

    private fun getTickSound(high: Boolean): FloatArray {
        val setting = Settings.METRONOME_SOUNDS.get()
        val id = if (high) setting.first else setting.second
        val soundRes = when (id) {
            0 -> if (high) R.raw.click_hi else R.raw.click_lo
            1 -> if (high) R.raw.sine_hi else R.raw.sine_lo
            2 -> if (high) R.raw.square_hi else R.raw.square_lo
            3 -> if (high) R.raw.clap_hi else R.raw.clap_lo
            4 -> if (high) R.raw.bell_hi else R.raw.bell_lo
            5 -> if (high) R.raw.tambourine_hi else R.raw.tambourine_lo
            6 -> if (high) R.raw.block_hi else R.raw.block_lo
            else -> null
        }
        if (soundRes == null) {
            return FloatArray(0)
        }

        tickSoundCache[soundRes]?.let { return it }

        try {
            val data = context.resources.openRawResource(soundRes).use { stream ->
                readWavStream(stream)
            }
            tickSoundCache[soundRes] = data
            return data
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun readWavStream(inputStream: InputStream): FloatArray {
        val content = readStreamToBytes(inputStream)
        val dataIndex = getDataIndex(content)
        if (dataIndex < 0) {
            throw RuntimeException("No data header found")
        }

        if (dataIndex + 8 > content.size) {
            throw RuntimeException("WAV file truncated")
        }

        val dataSizeBytes = ByteBuffer.wrap(content, dataIndex + 4, 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .int

        val dataStart = dataIndex + 8

        if (dataStart + dataSizeBytes > content.size) {
            throw RuntimeException("Data chunk size exceeds file size")
        }

        val byteBuffer = ByteBuffer.wrap(content, dataStart, dataSizeBytes)
            .order(ByteOrder.LITTLE_ENDIAN)
        val floatBuffer = byteBuffer.asFloatBuffer()

        val data = FloatArray(floatBuffer.remaining())
        floatBuffer[data]
        return data
    }

    @Throws(IOException::class)
    private fun readStreamToBytes(input: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(4096)
        var read: Int

        while ((input.read(data, 0, data.size).also { read = it }) != -1) {
            buffer.write(data, 0, read)
        }
        return buffer.toByteArray()
    }

    private fun getDataIndex(array: ByteArray): Int {
        val data = "data".toByteArray(StandardCharsets.US_ASCII)
        outer@ for (i in 0 until array.size - data.size + 1) {
            for (j in data.indices) {
                if (array[i + j] != data[j]) {
                    continue@outer
                }
            }
            return i
        }
        return -1
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Metronome controls"
            val descriptionText = "Metronome controls for background playback"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("PlayingBackground", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendRunningNotification() {
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
            .setContentTitle(context.getString(R.string.metronome_notification_title, tracks.values.firstOrNull()?.bpm?.toInt() ?: 120))
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
                paused = true
            } else {
                start()
                paused = false
            }
        }
        if (intent?.action == "dev.cognitivity.chronal.Stop") {
            if (playing) stop()
            paused = true
            val notificationManager = ChronalApp.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1)
        }
    }
}