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

package dev.cognitivity.chronal.settings.types.json.metronome

import com.google.gson.JsonObject
import dev.cognitivity.chronal.metronome.tracks.AudioTrack

data class MetronomeConfigAudioTrack(
    override val name: String,
    override val color: TrackColor,
    override val enabled: Boolean,
    val uri: String,
    val fileName: String,
    val startTrim: Float,
    val endTrim: Float,
    val volume: Float,
    val loop: Boolean,
    val bpm: Float?
) : MetronomeConfigTrack(name, color, enabled) {
    companion object {
        fun fromJson(jsonObject: JsonObject): MetronomeConfigAudioTrack {
            return MetronomeConfigAudioTrack(
                name = jsonObject.get("name")?.asString ?: "New audio track",
                color = TrackColor.fromJson(jsonObject.get("color").asJsonObject),
                enabled = jsonObject.get("enabled")?.asBoolean ?: true,
                uri = jsonObject.get("uri")?.asString ?: "",
                fileName = jsonObject.get("fileName")?.asString ?: "Unknown",
                startTrim = jsonObject.get("startTrim")?.asFloat ?: 0f,
                endTrim = jsonObject.get("endTrim")?.asFloat ?: 0f,
                volume = jsonObject.get("volume")?.asFloat ?: 1f,
                loop = jsonObject.get("loop")?.asBoolean ?: false,
                bpm = jsonObject.get("bpm")?.asFloat
            )
        }
        fun fromTrack(track: AudioTrack): MetronomeConfigAudioTrack {
            return MetronomeConfigAudioTrack(
                name = track.name,
                color = track.color,
                enabled = track.enabled,
                fileName = track.fileName,
                uri = track.uri.toString(),
                startTrim = track.startTrim,
                endTrim = track.endTrim,
                volume = track.volume,
                loop = track.loop,
                bpm = track.bpm
            )
        }
    }

    override fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("type", "audio")
            addProperty("name", name)
            add("color", color.toJson())
            addProperty("enabled", enabled)
            addProperty("uri", uri)
            addProperty("fileName", fileName)
            addProperty("startTrim", startTrim)
            addProperty("endTrim", endTrim)
            addProperty("volume", volume)
            addProperty("loop", loop)
            addProperty("bpm", bpm)
        }
    }
}