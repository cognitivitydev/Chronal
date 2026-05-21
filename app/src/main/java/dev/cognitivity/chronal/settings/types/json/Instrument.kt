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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.cognitivity.chronal.tuner.Pitch

data class Instrument(
    var name: String,
    var shortened: String,
    var transposition: Int,
    var strings: List<Pitch> = emptyList(),
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): Instrument {
            return Instrument(
                name = jsonObject.get("name").asString,
                shortened = jsonObject.get("shortened").asString,
                transposition = jsonObject.get("transposition").asInt,
                strings = jsonObject.getAsJsonArray("strings")?.map { Pitch.fromMidi(it.asInt) }?.toList() ?: emptyList(),
            )
        }
        fun fromJson(json: String): Instrument {
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)
            return fromJson(jsonObject)
        }
    }
    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("name", name)
            addProperty("shortened", shortened)
            addProperty("transposition", transposition)
            if(strings.isNotEmpty()) add("strings", JsonArray().apply {
                for(string in strings.map { it.toMidi() }) add(string)
            })
        }
    }
}
