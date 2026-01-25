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
    val primaryRhythm: Rhythm,
    val primarySimpleRhythm: SimpleRhythm,
    val secondaryRhythm: Rhythm,
    val secondarySimpleRhythm: SimpleRhythm,
    val state: MetronomeState
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): MetronomePreset {
            return MetronomePreset(
                timestamp = jsonObject.get("timestamp")?.asLong ?: System.currentTimeMillis(),
                name = jsonObject.get("name").asString,
                primaryRhythm = Rhythm.deserialize(jsonObject["primaryRhythm"].asString),
                primarySimpleRhythm = SimpleRhythm.fromJson(jsonObject["primarySimpleRhythm"].asJsonObject),
                secondaryRhythm = Rhythm.deserialize(jsonObject["secondaryRhythm"].asString),
                secondarySimpleRhythm = SimpleRhythm.fromJson(jsonObject["secondarySimpleRhythm"].asJsonObject),
                state = MetronomeState.fromJson(jsonObject["state"].asJsonObject)
            )
        }
        fun exampleDefault(): MetronomePreset {
            return MetronomePreset(
                name = context.getString(R.string.presets_example_default),
                primaryRhythm = Rhythm.deserialize("{4/4}Q;q;q;q;"),
                primarySimpleRhythm = SimpleRhythm(2 to 4, 4, 3),
                secondaryRhythm = Rhythm.deserialize("{4/4}Q;q;q;q;"),
                secondarySimpleRhythm = SimpleRhythm(4 to 4, 4, 1),
                state = MetronomeState(bpm = 120f, beatValuePrimary = 4f, beatValueSecondary = 4f, secondaryEnabled = true)
            )
        }
        fun examplePolyrhythm(): MetronomePreset {
            return MetronomePreset(
                name = context.getString(R.string.presets_example_3_2),
                primaryRhythm = Rhythm.deserialize("{2/4}Q;Q;"),
                primarySimpleRhythm = SimpleRhythm(2 to 4, 4, 0),
                secondaryRhythm = Rhythm.deserialize("{2/4}3:2[q:q:q];"),
                secondarySimpleRhythm = SimpleRhythm(2 to 4, 6, 1),
                state = MetronomeState(bpm = 120f, beatValuePrimary = 4f, beatValueSecondary = 4f, secondaryEnabled = true)
            )
        }
    }
    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("timestamp", timestamp)
            addProperty("name", name)
            addProperty("primaryRhythm", primaryRhythm.serialize())
            add("primarySimpleRhythm", primarySimpleRhythm.toJson())
            addProperty("secondaryRhythm", secondaryRhythm.serialize())
            add("secondarySimpleRhythm", secondarySimpleRhythm.toJson())
            add("state", state.toJson())
        }
    }
}
