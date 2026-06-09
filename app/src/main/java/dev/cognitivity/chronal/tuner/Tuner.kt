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
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.round
import dev.cognitivity.chronal.settings.Settings

class Tuner {
    private val sampleRate = 44100
    private val bufferSize = 2048
    private val bufferOverlap = (bufferSize * 0.5).toInt()

    var hz by mutableFloatStateOf(0f)
    var history = mutableListOf<Pair<Long, Float>>() // max 100
    var probability by mutableFloatStateOf(0f)
    var lastUpdate = 0L
    var threshold = Settings.AUDIO_THRESHOLD.get()

    private val dispatcher: AudioDispatcher
    private val pitchDetectionHandler = PitchDetectionHandler { res, event ->
        if(event.getdBSPL() == Double.NEGATIVE_INFINITY) { // mic access failure
            hz = Float.NaN
            Log.w("Tuner", "Failed to access microphone. elapsed=${event.timeStamp.round(2)}s pitch=${res.pitch.round(2)}Hz")
        } else if(hz.isNaN()) hz = 0f

        if (res.pitch <= -1) return@PitchDetectionHandler
        if(res.pitch !in 25.0..10000.0) return@PitchDetectionHandler

        history.add(Pair(System.currentTimeMillis(), res.pitch))
        if(history.size > 100) history = history.subList(history.size - 100, history.size)

        val newProbability = normalizeThreshold(res.probability.coerceIn(0.8f, 1f))
        if(newProbability > threshold) {
            if(System.currentTimeMillis() - lastUpdate < 100 && res.pitch - hz < 20) {
                hz = (res.pitch + hz) / 2
                probability = newProbability
                lastUpdate = System.currentTimeMillis()
            } else {
                hz = res.pitch
                probability = newProbability
                lastUpdate = System.currentTimeMillis()
            }
        } else if(System.currentTimeMillis() - lastUpdate > 5000) {
            hz = -1f
            probability = 0f
            lastUpdate = System.currentTimeMillis()
        }
    }
    private val pitchProcessor: AudioProcessor =
        PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, sampleRate.toFloat(), bufferSize, pitchDetectionHandler
        )
    private var audioThread: Thread

    init {
        if (ActivityCompat.checkSelfPermission(ChronalApp.getInstance(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            throw IllegalStateException("Tuner is missing RECORD_AUDIO permission")
        }
        if(ChronalApp.getInstance().tuner != null) {
            ChronalApp.getInstance().tuner!!.stop()
        }

        val audioRecord = AndroidAudioInputStream.getAudioRecord(sampleRate, bufferSize)
        val audioStream = AndroidAudioInputStream(audioRecord)
        audioRecord.startRecording()

        dispatcher = AudioDispatcher(audioStream, bufferSize, bufferOverlap)
        dispatcher.addAudioProcessor(pitchProcessor)
        audioThread = Thread(dispatcher, "Audio Thread")
        audioThread.start()

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

    private fun normalizeThreshold(threshold: Float): Float {
        return (threshold-0.8f)/0.2f
    }
}