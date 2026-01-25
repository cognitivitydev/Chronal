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

import com.google.gson.Gson
import com.google.gson.JsonObject

data class MetronomeState(
    val bpm: Float,
    val beatValuePrimary: Float,
    val beatValueSecondary: Float,
    val secondaryEnabled: Boolean
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): MetronomeState {
            return MetronomeState(
                bpm = jsonObject.get("bpm").asFloat,
                beatValuePrimary = jsonObject.get("valuePrimary").asFloat,
                beatValueSecondary = jsonObject.get("valueSecondary").asFloat,
                secondaryEnabled = jsonObject.get("secondaryEnabled").asBoolean
            )
        }
        fun fromJson(json: String): MetronomeState {
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)
            return fromJson(jsonObject)
        }
    }
    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("bpm", bpm)
            addProperty("valuePrimary", beatValuePrimary)
            addProperty("valueSecondary", beatValueSecondary)
            addProperty("secondaryEnabled", secondaryEnabled)
        }
    }
}
