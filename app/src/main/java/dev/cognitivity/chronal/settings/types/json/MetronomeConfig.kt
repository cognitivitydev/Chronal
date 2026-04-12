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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.materialkolor.dynamicColorScheme
import dev.cognitivity.chronal.metronome.sound.SoundPack
import dev.cognitivity.chronal.settings.Settings

data class MetronomeConfigTrack(
    val name: String,
    val enabled: Boolean,
    val vibrate: Boolean,
    val rhythm: String,
    val simpleRhythm: SimpleRhythm,
    val beatValue: Float,
    val color: TrackColor,
    val soundPackId: String = SoundPack.DEFAULT_ID,
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
                beatValue = jsonObject.get("beatValue")?.asFloat ?: 4f,
                color = TrackColor.fromJson(jsonObject.get("color").asJsonObject),
                soundPackId = jsonObject.get("soundPackId")?.asString
                    ?.takeIf { SoundPack.byId(it) != null }
                    ?: SoundPack.DEFAULT_ID,
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
            add("color", color.toJson())
            addProperty("soundPackId", soundPackId)
        }
    }
}

sealed class TrackColor {
    object Primary : TrackColor()
    object Secondary : TrackColor()
    data class Custom(val value: Int) : TrackColor()

    companion object {
        fun fromJson(jsonObject: JsonObject): TrackColor {
            return when(jsonObject.get("type").asString) {
                "Primary" -> Primary
                "Secondary" -> Secondary
                "Custom" -> Custom(jsonObject.get("value").asInt)
                else -> throw IllegalArgumentException("Unknown TrackColor type")
            }
        }
    }

    fun toJson(): JsonObject {
        return JsonObject().apply {
            when(this@TrackColor) {
                is Primary -> addProperty("type", "Primary")
                is Secondary -> addProperty("type", "Secondary")
                is Custom -> {
                    addProperty("type", "Custom")
                    addProperty("value", value)
                }
            }
        }
    }

    @Composable
    fun getPalette(): TrackColorPalette {
        val theme = Settings.COLOR_SCHEME.get().theme
        val isDark = if(theme == ColorScheme.Theme.SYSTEM) isSystemInDarkTheme() else theme == ColorScheme.Theme.DARK

        return when(this) {
            is Custom -> {
                val seedColor = Color(value)
                val colorScheme = dynamicColorScheme(
                    seedColor = seedColor,
                    isDark = isDark
                )
                TrackColorPalette(
                    color = colorScheme.primary,
                    onColor = colorScheme.onPrimary,
                    colorContainer = colorScheme.primaryContainer,
                    onColorContainer = colorScheme.onPrimaryContainer
                )
            }

            is Primary -> {
                TrackColorPalette(
                    color = MaterialTheme.colorScheme.primary,
                    onColor = MaterialTheme.colorScheme.onPrimary,
                    colorContainer = MaterialTheme.colorScheme.primaryContainer,
                    onColorContainer = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            is Secondary -> {
                TrackColorPalette(
                    color = MaterialTheme.colorScheme.tertiary,
                    onColor = MaterialTheme.colorScheme.onTertiary,
                    colorContainer = MaterialTheme.colorScheme.tertiaryContainer,
                    onColorContainer = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
data class TrackColorPalette(val color: Color, val onColor: Color, val colorContainer: Color, val onColorContainer: Color)

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
                        beatValue = 4f,
                        color = TrackColor.Primary
                    ),
                    MetronomeConfigTrack(
                        name = "Secondary track",
                        enabled = false,
                        vibrate = true,
                        rhythm = "{4/4}Q;q;q;q;",
                        simpleRhythm = SimpleRhythm(4 to 4, 4, 2),
                        beatValue = 4f,
                        color = TrackColor.Secondary
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

