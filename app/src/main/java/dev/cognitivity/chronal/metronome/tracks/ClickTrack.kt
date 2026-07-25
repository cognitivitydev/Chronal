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

package dev.cognitivity.chronal.metronome.tracks

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.metronome.sound.SoundPack
import dev.cognitivity.chronal.metronome.sound.SoundType
import dev.cognitivity.chronal.rhythm.metronome.Beat
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmAtom
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmTuplet
import dev.cognitivity.chronal.settings.types.json.SimpleRhythm
import dev.cognitivity.chronal.settings.types.json.metronome.MetronomeConfigClickTrack
import dev.cognitivity.chronal.settings.types.json.metronome.TrackColor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ClickTrack(
    override var name: String = "New click track",
    override var color: TrackColor = TrackColor.Primary,
    enabled: Boolean = true,
    private var rhythm: Rhythm,
    var beatValue: Float = 4f,
    var vibrate: Boolean = true,
    var simpleRhythm: SimpleRhythm = SimpleRhythm(4 to 4, 4, 2),
    var soundPack: SoundPack = SoundPack.default(),
) : MetronomeTrack(name, color, enabled) {
    companion object {
        const val MIN_BPM = 1f
        const val MAX_BPM = 16000f

        fun fromSetting(setting: MetronomeConfigClickTrack): ClickTrack {
            return ClickTrack(
                name = setting.name,
                color = setting.color,
                enabled = setting.enabled,
                rhythm = Rhythm.deserialize(setting.rhythm),
                simpleRhythm = setting.simpleRhythm,
                beatValue = setting.beatValue,
                vibrate = setting.vibrate,
                soundPack = SoundPack.byId(setting.soundPackId) ?: SoundPack.default(),
            )
        }
    }

    override var enabled by mutableStateOf(enabled)

    private var intervals: List<Beat> = calculateIntervals(rhythm)

    var index: Int = -1

    var nextBeatSample: Long = 0L
    var sampleRemainder: Double = 0.0

    fun setRhythm(newRhythm: Rhythm) {
        this.rhythm = newRhythm
        this.intervals = calculateIntervals(rhythm)
        this.index = -1
        this.nextBeatSample = 0L
        this.sampleRemainder = 0.0

        onEdit(newRhythm)
    }

    fun getRhythm() = rhythm
    fun getIntervals() = intervals

    fun calculateIntervals(rhythm: Rhythm): List<Beat> {
        val list = mutableListOf<Beat>()
        for ((measureIndex, measure) in rhythm.measures.withIndex()) {
            var i = 0
            for (element in measure.elements) {
                when (element) {
                    is RhythmAtom -> {
                        list.add(Beat(element, measureIndex, i))
                        i++
                    }
                    is RhythmTuplet -> {
                        for (note in element.notes) {
                            list.add(Beat(note, measureIndex, i))
                            i++
                        }
                    }
                }
            }
        }
        return list
    }

    private val _updateEvents = MutableSharedFlow<Beat>(replay = 1)
    val updateEvents: SharedFlow<Beat> = _updateEvents.asSharedFlow()

    private val _editEvents = MutableSharedFlow<Rhythm>(replay = 1)
    val editEvents: SharedFlow<Rhythm> = _editEvents.asSharedFlow()

    fun onUpdate(beat: Beat) {
        _updateEvents.tryEmit(beat)
    }
    fun onEdit(rhythm: Rhythm) {
        _editEvents.tryEmit(rhythm)
    }

    fun vibrate(beat: Beat) {
        if(!this.vibrate || beat.duration < 0f) return

        val strong = if(soundPack.type == SoundType.ATONAL) beat.pitch == 0 else false

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibration = if(strong) {
                VibrationEffect.createOneShot(10, 255)
            } else {
                VibrationEffect.createOneShot(3, 255)
            }

            val vibratorManager = ChronalApp.getInstance().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.vibrate(CombinedVibration.createParallel(vibration))
        } else {
            val milliseconds = if(strong) 10L else 3L

            val vibrator = ChronalApp.getInstance().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(milliseconds)
        }
    }
}