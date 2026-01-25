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

data class ColorScheme(
    val color: Color,
    val theme: Theme,
    val contrast: Contrast
) {
    enum class Color {
        /** Defaults to AQUA when API < 31 */
        SYSTEM,
        RED,
        ORANGE,
        YELLOW,
        GREEN,
        AQUA,
        BLUE,
        PURPLE
    }
    enum class Theme {
        SYSTEM,
        LIGHT,
        DARK
    }
    enum class Contrast {
        /** Only when color is SYSTEM */
        SYSTEM,
        /** Unavailable when color is SYSTEM */
        LOW,
        /** Unavailable when color is SYSTEM */
        MEDIUM,
        /** Unavailable when color is SYSTEM */
        HIGH
    }
    companion object {
        fun fromJson(jsonObject: JsonObject): ColorScheme {
            return ColorScheme(
                color = Color.entries[jsonObject.get("color").asInt],
                theme = Theme.entries[jsonObject.get("theme").asInt],
                contrast = Contrast.entries[jsonObject.get("contrast").asInt]
            )
        }
        fun fromJson(json: String): ColorScheme {
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)
            return fromJson(jsonObject)
        }
        fun default(): ColorScheme {
            return ColorScheme(
                color = Color.SYSTEM,
                theme = Theme.SYSTEM,
                contrast = Contrast.SYSTEM
            )
        }
    }
    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("color", color.ordinal)
            addProperty("theme", theme.ordinal)
            addProperty("contrast", contrast.ordinal)
        }
    }
}
