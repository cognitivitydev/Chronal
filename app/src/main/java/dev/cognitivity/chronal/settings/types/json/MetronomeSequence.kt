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

import com.google.gson.JsonArray
import com.google.gson.JsonObject

data class SequenceStep(
    val trackIndex: Int,
    val bars: Int,
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): SequenceStep {
            return SequenceStep(
                trackIndex = jsonObject.get("trackIndex")?.asInt ?: 0,
                bars = (jsonObject.get("bars")?.asInt ?: 1).coerceAtLeast(1),
            )
        }
    }

    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("trackIndex", trackIndex)
            addProperty("bars", bars)
        }
    }
}

data class MetronomeSequence(
    val enabled: Boolean = false,
    val steps: List<SequenceStep> = emptyList(),
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): MetronomeSequence {
            val steps = jsonObject.get("steps")
                ?.asJsonArray
                ?.mapNotNull {
                    try {
                        SequenceStep.fromJson(it.asJsonObject)
                    } catch (_: Exception) {
                        null
                    }
                }
                ?: emptyList()

            return MetronomeSequence(
                enabled = jsonObject.get("enabled")?.asBoolean ?: false,
                steps = steps,
            )
        }
    }

    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("enabled", enabled)
            add("steps", JsonArray().apply {
                steps.forEach { add(it.toJson()) }
            })
        }
    }

    fun validSteps(trackCount: Int): List<IndexedValue<SequenceStep>> {
        return steps.withIndex().filter { it.value.trackIndex in 0 until trackCount && it.value.bars > 0 }
    }
}
