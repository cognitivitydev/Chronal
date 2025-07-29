package dev.cognitivity.chronal

import android.app.Notification.Action
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.activity.editor.Beat
import dev.cognitivity.chronal.activity.editor.Rhythm
import dev.cognitivity.chronal.activity.editor.RhythmNote
import dev.cognitivity.chronal.activity.editor.RhythmTuplet
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
import kotlin.math.min

class Metronome(private var rhythm: Rhythm, private val sendNotifications: Boolean = true) : BroadcastReceiver() {
    private val sampleRate = 48000

    private var audioManager: AudioManager? = null
    private var audioTrack = getTrack()
    var playing = false
    var active = true
    var timestamp = 0L
    private var handlerThread: HandlerThread
    private var handler: Handler

    var bpm by mutableIntStateOf(60)
    var beatValue by mutableFloatStateOf(4f)

    private var intervals = getIntervals(rhythm)
    private var index = 0
    private var scheduled = 0
    private var nextScheduledTime = 0L

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        handlerThread = HandlerThread("metronome")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        if(sendNotifications) {
            ContextCompat.registerReceiver(context, this, IntentFilter("dev.cognitivity.chronal.PlayPause"), ContextCompat.RECEIVER_EXPORTED)
            ContextCompat.registerReceiver(context, this, IntentFilter("dev.cognitivity.chronal.Stop"), ContextCompat.RECEIVER_EXPORTED)
        }
    }
    fun setRhythm(rhythm: Rhythm) {
        if(playing) stop()
        this.rhythm = rhythm
        this.intervals = getIntervals(rhythm)
        listenerEdit.forEach { it.value(rhythm) }
    }
    fun getRhythm(): Rhythm { return rhythm }

    fun getIntervals(): List<Beat> { return intervals }

    fun start() {
        if (playing || !active) return

        CoroutineScope(Dispatchers.IO).launch {
            timestamp = System.currentTimeMillis()
            playing = true
            index = -1
            scheduled = 0
            nextScheduledTime = 0L

            scheduleTicks()

            audioTrack.play()

            listenerPause.forEach { it.value(false) }
            if (sendNotifications) sendRunningNotification()
        }
    }

    fun stop() {
        playing = false
        index = -1
        scheduled = 0
        nextScheduledTime = 0L
        listenerPause.forEach { listener -> listener.value(true) }
        audioTrack.pause()
        audioTrack.flush()
        if(sendNotifications) sendRunningNotification()
    }

    private fun getTrack(): AudioTrack {
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
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .build()
    }

    fun getIntervals(rhythm: Rhythm): List<Beat> {
        val intervals = mutableListOf<Beat>()
        for((measureIndex, measure) in rhythm.measures.withIndex()) {
            var index = 0
            for(element in measure.elements) {
                when(element) {
                    is RhythmNote -> {
                        intervals.add(Beat(element.duration, !element.isInverted, measureIndex, index))
                        index++
                    }
                    is RhythmTuplet -> {
                        for(note in element.notes) {
                            intervals.add(Beat(note.duration, !note.isInverted, measureIndex, index))
                            index++
                        }
                    }
                }
            }
        }
        return intervals
    }

    private fun scheduleTicks() {
        if (!playing) return

        repeat(4 - scheduled) {
            val timestamp = this.timestamp
            val newIndex = (index + 1).mod(intervals.size)
            index = newIndex
            val interval = intervals[index]
            val delay = (abs(interval.duration * 1000000) * 60000 / bpm * beatValue).toLong()

            val now = System.nanoTime()
            if (nextScheduledTime < now) nextScheduledTime = now

            val scheduledDelay = nextScheduledTime - now
            nextScheduledTime += delay

            scheduled++

            handler.postDelayed({
                if (!playing || timestamp != this.timestamp) return@postDelayed

                listenerUpdate.forEach { it.value(interval) }
                writePeriod(delay, interval.isHigh, interval.duration < 0)

                scheduled--
                scheduleTicks()
            }, scheduledDelay / 1000000L)
        }
    }

    private fun writePeriod(nanos: Long, isHigh: Boolean, silent: Boolean) {
        val periodSize = ((nanos / 1_000_000_000.0) * sampleRate).toInt()

        handler.post {
            if (index == -1) return@post
            val tickSound = if (!silent) getTickSound(isHigh) else floatArrayOf()
            var sizeWritten = writeAudio(audioTrack, tickSound, periodSize = periodSize, 0)
            Log.d("a", "wrote $isHigh after ${System.currentTimeMillis() - timestamp}ms")
            while(sizeWritten < periodSize) {
                sizeWritten += writeAudio(audioTrack, FloatArray(periodSize), periodSize = periodSize, sizeWritten)
            }
        }
    }

    private fun writeAudio(track: AudioTrack, data: FloatArray, periodSize: Int, sizeWritten: Int): Int {
        val size = min(data.size - sizeWritten, periodSize - sizeWritten)
        if (playing) {
            writeAudioWithOffset(track, data, sizeWritten, size)
        }
        return size
    }

    private fun writeAudioWithOffset(track: AudioTrack, data: FloatArray, offset: Int, size: Int) {
        try {
            val result = track.write(data, offset, size, AudioTrack.WRITE_BLOCKING)
            if (result < 0) {
                stop()
                throw IllegalStateException("Failed to write audio data ($result)")
            }
        } catch (e: Exception) {
            Log.e("Metronome", "Failed to write audio data", e)
        }
    }

    private fun getTickSound(high: Boolean): FloatArray {
        val setting = ChronalApp.getInstance().settings.metronomeSounds.value
        val id = if(high) setting.first else setting.second
        val sound = when(id) {
            0 -> if(high) R.raw.click_hi else R.raw.click_lo
            1 -> if(high) R.raw.sine_hi else R.raw.sine_lo
            2 -> if(high) R.raw.square_hi else R.raw.square_lo
            3 -> if(high) R.raw.clap_hi else R.raw.clap_lo
            4 -> if(high) R.raw.bell_hi else R.raw.bell_lo
            5 -> if(high) R.raw.tambourine_hi else R.raw.tambourine_lo
            6 -> if(high) R.raw.block_hi else R.raw.block_lo
            else -> null
        }
        if(sound == null) {
            return FloatArray(0)
        }

        try {
            ChronalApp.context.resources.openRawResource(sound).use { stream ->
                return readWavStream(stream)
            }
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
        outer@ for (i in 0..<array.size - data.size + 1) {
            for (j in data.indices) {
                if (array[i + j] != data[j]) {
                    continue@outer
                }
            }
            return i
        }
        return -1
    }
    private var listenerUpdate = mutableMapOf<Int, (Beat) -> Unit>()
    private val listenerPause = mutableMapOf<Int, (Boolean) -> Unit>()
    private val listenerEdit = mutableMapOf<Int, (Rhythm) -> Unit>()

    fun setUpdateListener(id: Int, listener: (Beat) -> Unit) {
        this.listenerUpdate[id] = listener
    }
    fun setPauseListener(id: Int, listener: (Boolean) -> Unit) {
        this.listenerPause[id] = listener
    }
    fun setEditListener(id: Int, listener: (Rhythm) -> Unit) {
        this.listenerEdit[id] = listener
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

        val builder = NotificationCompat.Builder(context, "PlayingBackground")
            .setSmallIcon(R.drawable.baseline_music_note_24)
            .setContentTitle(context.getString(R.string.metronome_notification_title, bpm))
            .setContentText(context.getString(if(this.playing) R.string.metronome_notification_playing else R.string.metronome_notification_paused))
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(if(this.playing) Action.SEMANTIC_ACTION_MUTE else Action.SEMANTIC_ACTION_UNMUTE,
                context.getString(if(this.playing) R.string.generic_pause else R.string.generic_resume), pausePendingIntent)
            .addAction(Action.SEMANTIC_ACTION_DELETE, context.getString(R.string.generic_stop), stopPendingIntent)
            .setUsesChronometer(true)

        notificationManager.notify(1, builder.build())
    }

    private fun createNotificationChannel() {
        val name = "Metronome controls"
        val descriptionText = "Metronome controls for background playback"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel("PlayingBackground", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Metronome", "Received intent: ${intent?.action}")
        if (intent?.action == "dev.cognitivity.metronome.PlayPause") {
            if(playing) {
                stop()
                paused = true
            } else {
                start()
                paused = false
            }
        }
        if (intent?.action == "dev.cognitivity.metronome.Stop") {
            if(playing) stop()
            paused = true
            val notificationManager = ChronalApp.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1)
        }
    }
}