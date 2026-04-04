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
import dev.cognitivity.chronal.rhythm.metronome.Measure
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmElement
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmNote
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmTuplet
import dev.cognitivity.chronal.rhythm.metronome.elements.StemDirection

data class SimpleRhythm(
    val timeSignature: Pair<Int, Int>,
    val subdivision: Int,
    val emphasis: Int
) {
    companion object {
        val DISABLED = SimpleRhythm(0 to 0, 0, 0)

        fun fromJson(jsonObject: JsonObject): SimpleRhythm {
            return SimpleRhythm(
                timeSignature = Pair(
                    jsonObject.getAsJsonObject("timeSignature").get("numerator").asInt,
                    jsonObject.getAsJsonObject("timeSignature").get("denominator").asInt
                ),
                subdivision = jsonObject.get("subdivision").asInt,
                emphasis = jsonObject.get("emphasis").asInt
            )
        }
        fun fromJson(json: String): SimpleRhythm {
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)
            return fromJson(jsonObject)
        }
    }
    fun toJson(): JsonObject {
        return JsonObject().apply {
            add("timeSignature", JsonObject().apply {
                addProperty("numerator", timeSignature.first)
                addProperty("denominator", timeSignature.second)
            })
            addProperty("subdivision", subdivision)
            addProperty("emphasis", emphasis)
        }
    }

    fun asRhythm(): Rhythm {
        val timeSignature = if(timeSignature.second == 0) (timeSignature.first to 4) else timeSignature
        val subdivision = if(subdivision == 0) timeSignature.second else subdivision
        val isTuplet = (subdivision and (subdivision - 1)) != 0
        val duration = 1.0 / subdivision
        val baseDuration = if(!isTuplet) duration else 1.0 / (subdivision * 2 / 3.0)
        val measureDuration = timeSignature.first / timeSignature.second.toDouble()

        var remaining = measureDuration
        var emphasizeNext = emphasis != 1
        val newMeasure = Measure(timeSignature, arrayListOf<RhythmElement>().apply {
            while(remaining > 1e-6) {
                if(isTuplet) {
                    add(RhythmTuplet(
                        ratio = 3 to 2,
                        notes = ArrayList<RhythmNote>().apply {
                            repeat(3) {
                                if (remaining <= 0) return@repeat
                                add(RhythmNote(
                                    stemDirection = if(emphasizeNext) StemDirection.UP else StemDirection.DOWN,
                                    baseDuration = baseDuration,
                                    tupletRatio = 3 to 2,
                                    dots = 0
                                ))
                                remaining -= duration
                                emphasizeNext = when (emphasis) {
                                    0 -> true
                                    3 -> !emphasizeNext
                                    else -> false
                                }
                            }
                        }
                    ))
                } else {
                    add(RhythmNote(
                        stemDirection = if(emphasizeNext) StemDirection.UP else StemDirection.DOWN,
                        baseDuration = duration,
                        dots = 0
                    ))
                    remaining -= duration
                    emphasizeNext = when (emphasis) {
                        0 -> true
                        3 -> !emphasizeNext
                        else -> false
                    }
                }
            }
        })

        if(remaining < -1e-6) {
            throw IllegalStateException("Generated rhythm exceeds measure by ${-remaining} beats")
        }

        return Rhythm(listOf(newMeasure))
    }
}
