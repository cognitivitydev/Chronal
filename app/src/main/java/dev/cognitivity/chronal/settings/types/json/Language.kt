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

data class Language(
    val key: String,
    val androidCode: String?,
    val name: String,
    val progress: Float,
    val contributors: List<String>
) {
    constructor(jsonObject: JsonObject) : this(
        key = jsonObject["key"].asString,
        androidCode = jsonObject["android"]?.asString,
        name = jsonObject["name"].asString,
        progress = jsonObject["progress"].asFloat,
        contributors = jsonObject["contributors"]?.asJsonArray?.map { it.asString }?.toList()
            ?: emptyList()
    )
}
