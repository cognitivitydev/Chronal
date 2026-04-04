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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.cognitivity.chronal.settings.Settings

data class MetronomeConfigTrack(
    val name: String,
    val enabled: Boolean,
    val vibrate: Boolean,
    val rhythm: String,
    val simpleRhythm: SimpleRhythm,
    val beatValue: Float
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): MetronomeConfigTrack {
            return MetronomeConfigTrack(
                name = jsonObject.get("name")?.asString ?: "New track",
                enabled = jsonObject.get("enabled")?.asBoolean ?: true,
                vibrate = jsonObject.get("vibrate")?.asBoolean ?: true,
                rhythm = jsonObject.get("rhythm")?.asString ?: "{4/4}Q;q;q;q;",
                simpleRhythm = jsonObject.get("simpleRhythm")?.let {
                    try {
                        SimpleRhythm.fromJson(it.asJsonObject)
                    } catch (_: Exception) {
                        SimpleRhythm(4 to 4, 4, 2)
                    }
                } ?: SimpleRhythm(4 to 4, 4, 2),
                beatValue = jsonObject.get("beatValue")?.asFloat ?: 4f
            )
        }
    }

    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("name", name)
            addProperty("enabled", enabled)
            addProperty("vibrate", vibrate)
            addProperty("rhythm", rhythm)
            add("simpleRhythm", simpleRhythm.toJson())
            addProperty("beatValue", beatValue)
        }
    }
}

data class MetronomeConfig(
    val version: Int = Settings.getCurrentVersionCode(),
    val bpm: Float,
    val tracks: List<MetronomeConfigTrack>
) {
    companion object {
        fun default(): MetronomeConfig {
            return MetronomeConfig(
                bpm = 120f,
                tracks = listOf(
                    MetronomeConfigTrack(
                        name = "Primary track",
                        enabled = true,
                        vibrate = true,
                        rhythm = "{4/4}Q;q;q;q;",
                        simpleRhythm = SimpleRhythm(4 to 4, 4, 2),
                        beatValue = 4f
                    ),
                    MetronomeConfigTrack(
                        name = "Secondary track",
                        enabled = false,
                        vibrate = true,
                        rhythm = "{4/4}Q;q;q;q;",
                        simpleRhythm = SimpleRhythm(4 to 4, 4, 2),
                        beatValue = 4f
                    )
                )
            )
        }

        fun fromJson(jsonObject: JsonObject): MetronomeConfig {
            val tracks = jsonObject.get("tracks")
                ?.asJsonArray
                ?.mapNotNull {
                    try {
                        MetronomeConfigTrack.fromJson(it.asJsonObject)
                    } catch (_: Exception) {
                        null
                    }
                }
                ?: emptyList()

            return MetronomeConfig(
                version = jsonObject.get("version")?.asInt ?: 0,
                bpm = jsonObject.get("bpm")?.asFloat ?: 0f,
                tracks = tracks.ifEmpty { default().tracks }
            )
        }
    }

    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("version", version)
            addProperty("bpm", bpm)
            add("tracks", JsonArray().apply {
                tracks.forEach { add(it.toJson()) }
            })
        }
    }
}

