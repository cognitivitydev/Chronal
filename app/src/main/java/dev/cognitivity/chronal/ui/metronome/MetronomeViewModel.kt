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
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.vibratorManager
import dev.cognitivity.chronal.metronome.MetronomeTrack
import dev.cognitivity.chronal.metronome.SequencePosition
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.MetronomeSequence
import dev.cognitivity.chronal.settings.types.json.SequenceStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DisplayMode {
    CLOCK,
    CONDUCTOR,
    GRID,
    PIE
}

class MetronomeViewModel: ViewModel() {
    private val metronome = ChronalApp.getInstance().metronome

    private val _playing = MutableStateFlow(false)
    val playing: StateFlow<Boolean> = _playing.asStateFlow()

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

    private val _sequence = MutableStateFlow(Settings.METRONOME_CONFIG.get().sequence)
    val sequence: StateFlow<MetronomeSequence> = _sequence.asStateFlow()

    private val _showSequenceSheet = MutableStateFlow(false)
    val showSequenceSheet: StateFlow<Boolean> = _showSequenceSheet.asStateFlow()

    private val _sequencePosition = MutableStateFlow<SequencePosition?>(null)
    val sequencePosition: StateFlow<SequencePosition?> = _sequencePosition.asStateFlow()

    init {
        syncMetronomeState()

        viewModelScope.launch {
            metronome.sequencer.positionEvents.collect { position ->
                if (position == null) {
                    _sequencePosition.value = null
                    return@collect
                }
                val timestamp = metronome.timestamp
                launch {
                    delay(Settings.VISUAL_LATENCY.get().toLong())
                    if (metronome.playing && timestamp == metronome.timestamp) {
                        _sequencePosition.value = position
                    }
                }
            }
        }
    }

    fun syncMetronomeState() {
        _tracks.value = metronome.tracks.toList()
        _sequence.value = Settings.METRONOME_CONFIG.get().sequence

        CoroutineScope(Dispatchers.Main).launch {
            metronome.tracks[0].pauseEvents.collect { paused ->
                _playing.value = !paused
            }
        }
    }

    fun reloadMetronomeState() {
        val config = Settings.METRONOME_CONFIG.get()
        val tracks = config.tracks.map { MetronomeTrack.fromSetting(it) }
        metronome.bpm = config.bpm
        _tracks.value = tracks.toMutableList()
        metronome.tracks = tracks.toMutableList()
        metronome.setSequence(config.sequence)
        _sequence.value = config.sequence
        setPlaying(ChronalApp.getInstance().metronome.playing)
    }

    fun setPlaying(newValue: Boolean) {
        _playing.value = newValue
        if(newValue) metronome.start() else metronome.stop()
    }

    private var lastVibration = 0L
    fun setBpm(newValue: Float, vibrate: Boolean = true) {
        if(metronome.bpm == newValue) return
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
                (vibratorManager ?: return).vibrate(
                    CombinedVibration.createParallel(
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    )
                ) else {
                val vibrator = ChronalApp.getInstance().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(10)
            }
        } else {
            val tickPattern = longArrayOf(5)
            val tickAmplitude = intArrayOf((newValue / 2).toInt().coerceIn(1, 255))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && vibratorManager != null) {
                (vibratorManager ?: return).vibrate(
                    CombinedVibration.createParallel(
                        VibrationEffect.createWaveform(tickPattern,tickAmplitude, -1)
                    )
                )
            } else {
                val vibrator = ChronalApp.getInstance().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(5)
            }
        }
    }

    fun setTrackEnabled(index: Int, enabled: Boolean) {
        val track = metronome.tracks.getOrNull(index) ?: return
        if(!enabled && metronome.tracks.count { it.enabled } <= 1) {
            Toast.makeText(ChronalApp.getInstance(), R.string.track_settings_disable_failed, Toast.LENGTH_SHORT).show()
            return
        }
        track.enabled = enabled
        _tracks.value = metronome.tracks.toList()
        Settings.updateTrack(index) { it.copy(enabled = enabled) }
        CoroutineScope(Dispatchers.Main).launch {
            Settings.METRONOME_CONFIG.save()
        }
    }

    fun setSequence(newValue: MetronomeSequence) {
        _sequence.value = newValue
        Settings.setSequence(newValue)
        CoroutineScope(Dispatchers.Main).launch {
            Settings.METRONOME_CONFIG.save()
        }
    }
    fun setSequenceEnabled(enabled: Boolean) = setSequence(sequence.value.copy(enabled = enabled))
    fun setSequenceSteps(steps: List<SequenceStep>) {
        val current = sequence.value
        setSequence(current.copy(enabled = current.enabled && steps.isNotEmpty(), steps = steps))
    }
    fun setShowSequenceSheet(newValue: Boolean) { _showSequenceSheet.value = newValue }

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

/**
 * The track the visualizer should show while sequence mode is enabled:
 * the active step's track while playing, otherwise the first valid step's track.
 * Null when sequence mode is disabled or has no valid steps.
 */
@Composable
fun MetronomeViewModel.sequenceDisplayTrack(tracks: List<MetronomeTrack>): MetronomeTrack? {
    val sequence by sequence.collectAsState()
    val position by sequencePosition.collectAsState()
    if (!sequence.enabled) return null
    val index = position?.trackIndex
        ?: sequence.validSteps(tracks.size).firstOrNull()?.value?.trackIndex
        ?: return null
    return tracks.getOrNull(index)
}