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

data class TempoMarking(
    val name: String,
    val range: IntRange,
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): TempoMarking {
            return TempoMarking(
                name = jsonObject.get("name").asString,
                range = IntRange(
                    jsonObject.getAsJsonObject("range").get("min").asInt,
                    jsonObject.getAsJsonObject("range").get("max").asInt
                )
            )
        }
        fun default(): MutableList<TempoMarking> {
            return mutableListOf(
                TempoMarking("Larghissimo", 1..24),
                TempoMarking("Grave", 25..39),
                TempoMarking("Lento", 40..49),
                TempoMarking("Largo", 50..59),
                TempoMarking("Larghetto", 60..66),
                TempoMarking("Adagio", 67..76),
                TempoMarking("Andante", 77..108),
                TempoMarking("Moderato", 109..120),
                TempoMarking("Allegretto", 121..132),
                TempoMarking("Allegro", 133..143),
                TempoMarking("Vivace", 144..159),
                TempoMarking("Presto", 160..199),
                TempoMarking("Prestissimo", 200..800)
            )
        }
    }
    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("name", name)
            add("range", JsonObject().apply {
                addProperty("min", range.first)
                addProperty("max", range.last)
            })
        }
    }
}