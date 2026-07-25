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

package dev.cognitivity.chronal.metronome

import androidx.compose.runtime.State
import dev.cognitivity.chronal.metronome.tracks.ClickTrack
import dev.cognitivity.chronal.rhythm.metronome.Beat

abstract class MetronomeModifier {
    val metronome: Metronome

    constructor(metronome: Metronome) {
        this.metronome = metronome
    }

    open fun onStart() {}
    open fun onStop() {}
    open fun onTick(track: ClickTrack, beat: Beat) {}

    abstract val isEnabled: State<Boolean>
}