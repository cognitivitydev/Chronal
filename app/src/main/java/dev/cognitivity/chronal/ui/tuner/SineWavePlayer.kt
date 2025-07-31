package dev.cognitivity.chronal.ui.tuner

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SineWavePlayer(
    private var frequency: Double,
    private val sampleRate: Int = 44100,
    private val fadeDurationMs: Int = 200
) {
    val bufferSize = 8192

    private var audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    private var phase = 0.0
    private var playJob: Job? = null
    private var stopJob: Job? = null
    private var isStopping = false
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        val silentBuffer = ShortArray(bufferSize * 4)
        audioTrack.write(silentBuffer, 0, silentBuffer.size)
        audioTrack.play()
    }

    suspend fun setFrequency(newFrequency: Double) {
        if (newFrequency <= 0) return

        if(playJob == null && !isStopping) {
            frequency = newFrequency
            return
        }
        stop()
        stopJob?.join()
        frequency = newFrequency
        start()
    }

    fun start() {
        if (playJob != null || isStopping) return

        playJob = scope.launch {
            stopJob?.join()
            stopJob = null

            val fadeSamples = (fadeDurationMs * sampleRate) / 1000
            var amplitude = 0.0
            val amplitudeStep = 1.0 / fadeSamples

            val phaseIncrement = 2 * PI * frequency / sampleRate

            while (isActive) {
                val buffer = ShortArray(bufferSize)

                for (i in buffer.indices) {
                    val sample = (sin(phase) * Short.MAX_VALUE * amplitude).toInt().toShort()
                    buffer[i] = sample
                    phase += phaseIncrement
                    if (phase > 2 * PI) phase -= 2 * PI

                    if (amplitude < 1.0) {
                        amplitude += amplitudeStep
                        if (amplitude > 1.0) amplitude = 1.0
                    }
                }

                audioTrack.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stop() {
        if (playJob == null || isStopping) return

        isStopping = true

        stopJob = scope.launch {
            playJob?.cancelAndJoin()
            playJob = null

            val fadeSamples = (fadeDurationMs * sampleRate) / 1000
            var amplitude = 1.0
            val amplitudeStep = 1.0 / fadeSamples

            val phaseIncrement = 2 * PI * frequency / sampleRate

            while (amplitude > 0.0) {
                val buffer = ShortArray(bufferSize)

                for (i in buffer.indices) {
                    val sample = (sin(phase) * Short.MAX_VALUE * amplitude).toInt().toShort()
                    buffer[i] = sample
                    phase += phaseIncrement
                    if (phase > 2 * PI) phase -= 2 * PI

                    amplitude -= amplitudeStep
                    if (amplitude < 0.0) amplitude = 0.0
                }

                audioTrack.write(buffer, 0, buffer.size)
            }

            audioTrack.flush()
            isStopping = false
        }
    }
}