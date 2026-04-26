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

package dev.cognitivity.chronal.ui.metronome

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.metronome.MetronomeTrack
import dev.cognitivity.chronal.activity.vibratorManager
import dev.cognitivity.chronal.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DisplayMode {
    CLOCK,
    CONDUCTOR,
    GRID
}

class MetronomeViewModel: ViewModel() {
    private val metronome = ChronalApp.getInstance().metronome

    private val _playing = MutableStateFlow(false)
    val playing: StateFlow<Boolean> = _playing.asStateFlow()

    private val _bpm = MutableStateFlow(0f)
    val bpm: StateFlow<Float> = _bpm.asStateFlow()

    private val _tracks = MutableStateFlow<List<MetronomeTrack>>(emptyList())
    val tracks: StateFlow<List<MetronomeTrack>> = _tracks.asStateFlow()

    private val _settingsExpanded = MutableStateFlow(false)
    val settingsExpanded: StateFlow<Boolean> = _settingsExpanded.asStateFlow()

    private val _modesExpanded = MutableStateFlow(false)
    val modesExpanded: StateFlow<Boolean> = _modesExpanded.asStateFlow()

    private val _displayMode = MutableStateFlow(DisplayMode.CLOCK)
    val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

    private val _fullscreenMode = MutableStateFlow(false)
    val fullscreenMode: StateFlow<Boolean> = _fullscreenMode.asStateFlow()

    private val _flipConductor = MutableStateFlow(false)
    val flipConductor: StateFlow<Boolean> = _flipConductor.asStateFlow()

    private val _showBpmDialog = MutableStateFlow(false)
    val showBpmDialog: StateFlow<Boolean> = _showBpmDialog.asStateFlow()

    private val _bpmDialogTab = MutableStateFlow(0)
    val bpmDialogTab: StateFlow<Int> = _bpmDialogTab.asStateFlow()

    private val _intervals = MutableStateFlow(listOf<Long>())
    val intervals: StateFlow<List<Long>> = _intervals.asStateFlow()

    private val _lastTapTime = MutableStateFlow(0L)
    val lastTapTime: StateFlow<Long> = _lastTapTime.asStateFlow()

    init {
        syncMetronomeState()
    }

    fun syncMetronomeState() {
        _bpm.value = metronome.bpm
        _tracks.value = metronome.tracks.toList()
    }

    fun setPlaying(newValue: Boolean) {
        _playing.value = newValue
        if(newValue) metronome.start() else metronome.stop()
    }

    private var lastVibration = 0L
    fun setBpm(newValue: Float, vibrate: Boolean = true) {
        if(bpm.value == newValue) return
        _bpm.value = newValue

        metronome.bpm = newValue

        CoroutineScope(Dispatchers.Main).launch {
            Settings.setBpm(newValue)
            Settings.METRONOME_CONFIG.save()
        }

        if(!vibrate) return

        if(newValue <= MetronomeTrack.MIN_BPM || newValue >= MetronomeTrack.MAX_BPM) {
            if(System.currentTimeMillis() - lastVibration < 100) return
            lastVibration = System.currentTimeMillis()
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && vibratorManager != null)
                vibratorManager!!.vibrate(
                    CombinedVibration.createParallel(
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    )
                ) else {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(10)
            }
        } else {
            val tickPattern = longArrayOf(5)
            val tickAmplitude = intArrayOf((newValue / 2).toInt().coerceIn(1, 255))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && vibratorManager != null) {
                vibratorManager!!.vibrate(
                    CombinedVibration.createParallel(
                        VibrationEffect.createWaveform(tickPattern,tickAmplitude, -1)
                    )
                )
            } else {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(5)
            }
        }
    }

    fun addTrack(track: MetronomeTrack): Int {
        metronome.tracks.add(track)
        _tracks.value = metronome.tracks.toList()
        return metronome.tracks.lastIndex
    }

    fun setTrackEnabled(index: Int, enabled: Boolean) {
        val track = metronome.tracks.getOrNull(index) ?: return
        if(!enabled && metronome.tracks.count { it.enabled } <= 1) return
        track.enabled = enabled
        _tracks.value = metronome.tracks.toList()
        Settings.updateTrack(index) { it.copy(enabled = enabled) }
        CoroutineScope(Dispatchers.Main).launch {
            Settings.METRONOME_CONFIG.save()
        }
    }

    fun setSettingsExpanded(newValue: Boolean) { _settingsExpanded.value = newValue }
    fun setModesExpanded(newValue: Boolean) { _modesExpanded.value = newValue }
    fun setDisplayMode(newValue: DisplayMode) { _displayMode.value = newValue }
    fun setFullscreenMode(newValue: Boolean) { _fullscreenMode.value = newValue }
    fun setFlipConductor(newValue: Boolean) { _flipConductor.value = newValue }
    fun setShowBpmDialog(newValue: Boolean) { _showBpmDialog.value = newValue }
    fun setBpmDialogTab(newValue: Int) { _bpmDialogTab.value = newValue }
    fun setIntervals(newValue: List<Long>) { _intervals.value = newValue }
    fun addInterval(newValue: Long) { _intervals.value += newValue }
    fun setLastTapTime(newValue: Long) { _lastTapTime.value = newValue }
}