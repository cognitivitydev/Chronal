package dev.cognitivity.chronal

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor

class Tuner {
    var hz by mutableFloatStateOf(0f)
    var probability by mutableFloatStateOf(0f)
    var lastUpdate = 0L
    var threshold = ChronalApp.getInstance().settings.audioThreshold

    private val dispatcher: AudioDispatcher
    private val pitchDetectionHandler = PitchDetectionHandler { res, event ->

        if (res.pitch <= -1) return@PitchDetectionHandler
        val newProbability = normalizeThreshold(res.probability.coerceIn(0.8f, 1f))
        if(newProbability > threshold.value && res.pitch >= 25 && res.pitch <= 10000) {
            if(System.currentTimeMillis()-lastUpdate < 100 && res.pitch - hz < 20) {
                hz = (res.pitch + hz) / 2
                probability = newProbability
                lastUpdate = System.currentTimeMillis()
            } else {
                hz = res.pitch
                probability = newProbability
                lastUpdate = System.currentTimeMillis()
            }
        } else if(System.currentTimeMillis()-lastUpdate > 5000) {
            hz = -1f
            probability = 0f
            lastUpdate = System.currentTimeMillis()
        }
    }
    private val pitchProcessor: AudioProcessor =
        PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050F, 4096, pitchDetectionHandler
        )
    private var audioThread: Thread

    init {
        if (ActivityCompat.checkSelfPermission(ChronalApp.getInstance(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            throw IllegalStateException("Tuner is missing RECORD_AUDIO permission")
        }
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 4096, 3072)
        dispatcher.addAudioProcessor(pitchProcessor)
        audioThread = Thread(dispatcher, "Audio Thread")
        Thread({ dispatcher.run() }, "Audio Thread").start()
        if(ChronalApp.getInstance().tuner != null) {
            ChronalApp.getInstance().tuner!!.stop()
        }
        ChronalApp.getInstance().tuner = this
    }

    fun stop() {
        dispatcher.removeAudioProcessor(pitchProcessor)
        dispatcher.stop()
        if(audioThread.isAlive) audioThread.interrupt()
        if(ChronalApp.getInstance().tuner == this) {
            ChronalApp.getInstance().tuner = null
        }
    }

    fun normalizeThreshold(threshold: Float): Float {
        return (threshold-0.8f)/0.2f
    }
}