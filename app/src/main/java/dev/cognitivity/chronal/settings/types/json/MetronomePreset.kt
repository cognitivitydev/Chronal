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

package dev.cognitivity.chronal.settings.types.json

import com.google.gson.JsonObject
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.rhythm.metronome.Rhythm

data class MetronomePreset(
    val timestamp: Long = System.currentTimeMillis(),
    val name: String,
    val config: MetronomeConfig
) {

    val primaryRhythm: Rhythm
        get() = Rhythm.deserialize(config.tracks.getOrNull(0)?.rhythm ?: "{4/4}Q;q;q;q;")

    val secondaryRhythm: Rhythm
        get() = Rhythm.deserialize(config.tracks.getOrNull(1)?.rhythm ?: "{4/4}Q;q;q;q;")

    val primarySimpleRhythm: SimpleRhythm
        get() = config.tracks.getOrNull(0)?.simpleRhythm ?: SimpleRhythm(4 to 4, 4, 2)

    val secondarySimpleRhythm: SimpleRhythm
        get() = config.tracks.getOrNull(1)?.simpleRhythm ?: SimpleRhythm(4 to 4, 4, 2)

    companion object {
        fun fromJson(jsonObject: JsonObject): MetronomePreset {
            val config = jsonObject.get("config")?.asJsonObject?.let {
                MetronomeConfig.fromJson(it)
            } ?: MetronomeConfig.default()

            return MetronomePreset(
                timestamp = jsonObject.get("timestamp")?.asLong ?: System.currentTimeMillis(),
                name = jsonObject.get("name")?.asString ?: context.getString(R.string.presets_example_default),
                config = config
            )
        }

        fun exampleDefault(): MetronomePreset {
            return MetronomePreset(
                name = context.getString(R.string.presets_example_default),
                config = MetronomeConfig.default()
            )
        }

        fun examplePolyrhythm(): MetronomePreset {
            return MetronomePreset(
                name = context.getString(R.string.presets_example_3_2),
                config = MetronomeConfig(
                    bpm = 120f,
                    tracks = listOf(
                        MetronomeConfigTrack(
                            name = "Primary track",
                            enabled = true,
                            vibrate = true,
                            rhythm = "{2/4}Q;Q;",
                            beatValue = 4f,
                            simpleRhythm = SimpleRhythm(2 to 4, 4, 0),
                            color = TrackColor.Primary
                        ),
                        MetronomeConfigTrack(
                            name = "Secondary track",
                            enabled = true,
                            vibrate = true,
                            rhythm = "{2/4}3:2[q:q:q];",
                            beatValue = 4f,
                            simpleRhythm = SimpleRhythm(2 to 4, 6, 1),
                            color = TrackColor.Secondary
                        )
                    )
                )
            )
        }
    }

    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("timestamp", timestamp)
            addProperty("name", name)
            add("config", config.toJson())
        }
    }
}
