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
import dev.cognitivity.chronal.metronome.sound.SoundPack
import dev.cognitivity.chronal.metronome.tracks.ClickTrack
import dev.cognitivity.chronal.settings.types.json.SimpleRhythm

data class MetronomeConfigClickTrack(
    override val name: String,
    override val color: TrackColor,
    override val enabled: Boolean,
    val vibrate: Boolean,
    val rhythm: String,
    val simpleRhythm: SimpleRhythm,
    val beatValue: Float,
    val soundPackId: String = SoundPack.DEFAULT_ID,
) : MetronomeConfigTrack(name, color, enabled) {
    companion object {
        fun fromJson(jsonObject: JsonObject): MetronomeConfigClickTrack {
            return MetronomeConfigClickTrack(
                name = jsonObject.get("name")?.asString ?: "New click track",
                color = TrackColor.fromJson(jsonObject.get("color").asJsonObject),
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
                soundPackId = jsonObject.get("soundPackId")?.asString
                    ?.takeIf { SoundPack.byId(it) != null }
                    ?: SoundPack.DEFAULT_ID,
            )
        }
        fun fromTrack(track: ClickTrack): MetronomeConfigClickTrack {
            return MetronomeConfigClickTrack(
                name = track.name,
                color = track.color,
                enabled = track.enabled,
                vibrate = track.vibrate,
                rhythm = track.getRhythm().serialize(),
                simpleRhythm = track.simpleRhythm,
                beatValue = track.beatValue,
                soundPackId = track.soundPack.id,
            )
        }
    }

    override fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("type", "click")
            addProperty("name", name)
            add("color", color.toJson())
            addProperty("enabled", enabled)
            addProperty("vibrate", vibrate)
            addProperty("rhythm", rhythm)
            add("simpleRhythm", simpleRhythm.toJson())
            addProperty("beatValue", beatValue)
            addProperty("soundPackId", soundPackId)
        }
    }
}