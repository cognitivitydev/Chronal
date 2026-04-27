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

package dev.cognitivity.chronal.ui.metronome.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.metronome.MetronomeTrack
import dev.cognitivity.chronal.rhythm.metronome.Measure
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmElement
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmNote
import dev.cognitivity.chronal.rhythm.metronome.elements.StemDirection
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.SimpleRhythm
import kotlinx.coroutines.launch

@Composable
fun TrackSettingsSwitchDialog(index: Int, track: MetronomeTrack, onDismissRequest: () -> Unit, onConfirm: () -> Unit) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(painter = painterResource(R.drawable.outline_warning_24), contentDescription = stringResource(R.string.generic_warning)) },
        title = { Text(stringResource(R.string.track_settings_switch_title)) },
        text = { Text(stringResource(R.string.track_settings_switch_text)) },
        confirmButton = {
            TextButton(onClick = {
                val isAdvanced = track.simpleRhythm == SimpleRhythm.DISABLED
                if(isAdvanced) {
                    switchToSimple(index, track)
                } else {
                    switchToAdvanced(index, track)
                }
                scope.launch {
                    Settings.METRONOME_CONFIG.save()
                    onConfirm()
                }
            }) {
                Text(stringResource(R.string.generic_switch))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.generic_cancel))
            }
        },
    )
}

private fun switchToSimple(index: Int, track: MetronomeTrack) {
    val rhythm = track.getRhythm()
    val newRhythm = Rhythm(listOf(
        Measure(
            timeSig = rhythm.measures[0].timeSig,
            elements = arrayListOf<RhythmElement>().apply {
                repeat(rhythm.measures[0].timeSig.first) {
                    add(
                        RhythmNote(
                            stemDirection = StemDirection.UP,
                            baseDuration = 1.0 / rhythm.measures[0].timeSig.second,
                            dots = 0
                        )
                    )
                }
            }
        )
    ))
    val newSimpleRhythm = SimpleRhythm(
        timeSignature = rhythm.measures[0].timeSig,
        subdivision = rhythm.measures[0].timeSig.second,
        emphasis = 0
    )
    Settings.updateTrack(index) { configTrack ->
        configTrack.copy(
            beatValue = 4f,
            rhythm = newRhythm.serialize(),
            simpleRhythm = newSimpleRhythm
        )
    }

    track.beatValue = 4f
    track.simpleRhythm = newSimpleRhythm
    track.setRhythm(newRhythm)
    ChronalApp.getInstance().metronome.tracks[index] = track
}

private fun switchToAdvanced(index: Int, track: MetronomeTrack) {
    val simpleRhythm = track.simpleRhythm
    val newRhythm = simpleRhythm.asRhythm()
    Settings.updateTrack(index) { configTrack ->
        configTrack.copy(
            simpleRhythm = SimpleRhythm.DISABLED,
            rhythm = newRhythm.serialize()
        )
    }

    track.simpleRhythm = SimpleRhythm.DISABLED
    track.setRhythm(newRhythm)
    ChronalApp.getInstance().metronome.tracks[index] = track
}