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

package dev.cognitivity.chronal.settings

import com.google.gson.Gson
import com.google.gson.JsonArray
import dev.cognitivity.chronal.settings.types.BooleanSetting
import dev.cognitivity.chronal.settings.types.FloatSetting
import dev.cognitivity.chronal.settings.types.IntSetting
import dev.cognitivity.chronal.settings.types.JsonSetting
import dev.cognitivity.chronal.settings.types.StringSetting
import dev.cognitivity.chronal.settings.types.json.ColorScheme
import dev.cognitivity.chronal.settings.types.json.Instrument
import dev.cognitivity.chronal.settings.types.json.MetronomePreset
import dev.cognitivity.chronal.settings.types.json.MetronomeState
import dev.cognitivity.chronal.settings.types.json.SimpleRhythm
import dev.cognitivity.chronal.settings.types.json.TempoMarking

object Settings {
    val COLOR_SCHEME = JsonSetting(
        key = "color_scheme",
        defaultValue = ColorScheme.default(),
        serializer = { it.toJson() },
        deserializer = { ColorScheme.fromJson(it.asJsonObject) }
    )

    val PRIMARY_INSTRUMENT = JsonSetting(
        key = "primary_instrument",
        defaultValue = Instrument("B♭ Trumpet", "B♭ Tpt.", -1),
        serializer = { it.toJson() },
        deserializer = { Instrument.fromJson(it.asJsonObject) }
    )

    // Metronome
    val VISUAL_LATENCY = IntSetting("visual_latency", 100)
    val SHOW_BEATS = BooleanSetting("show_beats", true)
    val SHOW_SUBDIVISIONS = BooleanSetting("show_subdivisions", true)
    val HIGH_CONTRAST = BooleanSetting("high_contrast", false)
    val NO_ANIMATION = BooleanSetting("no_animation", false)

    val TEMPO_MARKINGS = JsonSetting(
        key = "tempo_markings",
        defaultValue = TempoMarking.default(),
        serializer = { Gson().toJsonTree(it).asJsonArray },
        deserializer = { json ->
            json.asJsonArray.map { TempoMarking.fromJson(it.asJsonObject) }.toMutableList()
        }
    )

    // Tuner
    val TUNER_FREQUENCY = IntSetting("tuner_frequency", 440)
    val SHOW_OCTAVE = BooleanSetting("show_octave", false)
    val TRANSPOSE_NOTES = BooleanSetting("transpose_notes", false)
    val AUDIO_THRESHOLD = FloatSetting("audio_threshold", 0.5f)
    val ACCIDENTALS = IntSetting("accidentals", 2)
    val NOTE_NAMES = IntSetting("note_names", 0)
    val TUNER_LAYOUT = FloatSetting("tuner_layout", 0.33f)

    // Internal
    val SHOW_DEVELOPER_OPTIONS = BooleanSetting("show_developer_options", false)
    val METRONOME_RHYTHM = StringSetting("metronome_rhythm", "{4/4}Q;q;q;q;")
    val METRONOME_SIMPLE_RHYTHM = JsonSetting(
        key = "metronome_simple_rhythm",
        defaultValue = SimpleRhythm(4 to 4, 4, 2),
        serializer = { it.toJson() },
        deserializer = { SimpleRhythm.fromJson(it.asJsonObject) }
    )
    val METRONOME_VIBRATIONS = BooleanSetting("metronome_vibrations", true)
    val METRONOME_RHYTHM_SECONDARY = StringSetting("metronome_rhythm_secondary", "{4/4}Q;q;q;q;")
    val METRONOME_SIMPLE_RHYTHM_SECONDARY = JsonSetting(
        key = "metronome_simple_rhythm_secondary",
        defaultValue = SimpleRhythm(4 to 4, 4, 2),
        serializer = { it.toJson() },
        deserializer = { SimpleRhythm.fromJson(it.asJsonObject) }
    )
    val METRONOME_VIBRATIONS_SECONDARY = BooleanSetting("metronome_vibrations_secondary", true)
    val METRONOME_SOUNDS = JsonSetting(
        key = "metronome_sounds",
        defaultValue = 0 to 0,
        serializer = {
            JsonArray().apply {
                add(it.first)
                add(it.second)
            }
        },
        deserializer = { json ->
            val arr = json.asJsonArray
            (arr[0].asInt to arr[1].asInt)
        }
    )
    val METRONOME_STATE = JsonSetting(
        key = "metronome_state",
        defaultValue = MetronomeState(120f, 4f, 4f, false),
        serializer = { it.toJson() },
        deserializer = { MetronomeState.fromJson(it.asJsonObject) }
    )
    val METRONOME_PRESETS = JsonSetting(
        key = "metronome_presets",
        defaultValue = mutableListOf(
            MetronomePreset.exampleDefault(),
            MetronomePreset.examplePolyrhythm()
        ),
        serializer = { Gson().toJsonTree(it).asJsonArray },
        deserializer = { json -> json.asJsonArray.map { MetronomePreset.fromJson(it.asJsonObject) }.toMutableList() }
    )
    val FULLSCREEN_WARNING = BooleanSetting("fullscreen_warning", true)

    val VERSION = StringSetting("version", "0.0.0")
    val VERSION_CODE = IntSetting("version_code", 0)
}