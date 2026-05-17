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
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R

data class Instruments(
    val categories: List<InstrumentCategory>
) {
    companion object {
        fun fromJson(jsonArray: JsonArray): Instruments {
            return Instruments(
                categories = jsonArray.map { InstrumentCategory.fromJson(it.asJsonObject) }
            )
        }
        fun fromJson(json: String): Instruments {
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)
            return fromJson(jsonObject.getAsJsonArray("categories"))
        }

        fun default(): Instruments {
            val defaultTxt = ChronalApp.getInstance().resources.openRawResource(R.raw.instruments).bufferedReader().use { it.readText() }
            return fromJson(Gson().fromJson(defaultTxt, JsonArray::class.java))
        }
    }
    fun toJson(): JsonArray {
        return JsonArray().apply {
            categories.forEach { add(it.toJson()) }
        }
    }
}

data class InstrumentCategory(
    val name: String,
    val instruments: List<Instrument>
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): InstrumentCategory {
            return InstrumentCategory(
                name = jsonObject.get("name").asString,
                instruments = jsonObject.getAsJsonArray("instruments").map { Instrument.fromJson(it.asJsonObject) }
            )
        }
        fun fromJson(json: String): InstrumentCategory {
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)
            return fromJson(jsonObject)
        }
    }
    fun toJson(): JsonObject {
        val array = JsonArray().apply {
            for(instrument in instruments) {
                add(instrument.toJson())
            }
        }
        return JsonObject().apply {
            addProperty("name", name)
            add("instruments", array)
        }
    }
}
