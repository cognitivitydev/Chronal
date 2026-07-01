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

package dev.cognitivity.chronal.metronome.modifiers

import dev.cognitivity.chronal.metronome.Metronome
import dev.cognitivity.chronal.metronome.MetronomeTrack

/**
 * Gradually increases the tempo over a specified duration (in beats, measures, or milliseconds).
 */
class Accelerando(
    metronome: Metronome,
    duration: TempoChangeDuration,
    tempoRate: Float,
    maxBpm: Float = MetronomeTrack.MAX_BPM
) : TempoChange(metronome, duration, tempoRate, maxBpm = maxBpm)