/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2025  cognitivity
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

package dev.cognitivity.chronal.rhythm.metronome

import android.util.Log
import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmAtom
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmElement
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmNote
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmRest
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmTuplet
import dev.cognitivity.chronal.rhythm.metronome.elements.StemDirection

/**
 * Format:
 * {timeSignature}element1;element2;...|{timeSignature}tuplet[element1:element2:...]...
 *
 * {timeSignature} = {4/4}, {3/4}, {6/8}, etc.
 *
 * element = w (1/1), h (1/2), q (1/4), e (1/8), s (1/16), t (1/32), x (1/64), o (1/128), z (1/256), f (1/512), m (1/1024)
 * Capitalization indicates an emphasized note and an "!" before the element indicates a rest
 * Elements in measures are separated by ";"
 * Dotted notes are indicated by a "." (1 dot) or "," (2 dots) at the end of the element
 * tuplet = 3:2, 4:3, etc. Elements inside a tuplet are separated by ":"
 * | = measure break
 *
 * Example:
 * {4/4}Q;!e;E;3:2[!q:q:q];|{3/4}H;e.;s;
 */
data class Rhythm(
    val measures: List<Measure>
) {

    companion object {
        fun deserialize(rhythm: String): Rhythm {
            Log.d("a", "Deserializing $rhythm")
            val measures = arrayListOf<Measure>()
            val rawMeasures = rhythm.split("|").filter { it.isNotBlank() }

            for (rawMeasure in rawMeasures) {
                val timeSigRegex = "^\\{(\\d+)/(\\d+)\\}".toRegex()
                val timeSigMatch = timeSigRegex.find(rawMeasure)
                val timeSig = if (timeSigMatch != null) {
                    timeSigMatch.groupValues[1].toInt() to timeSigMatch.groupValues[2].toInt()
                } else {
                    0 to 0
                }

                // remove time signature from measure
                val measureBody = rawMeasure.replace(timeSigRegex, "")

                val elements = mutableListOf<RhythmElement>()
                val tokens = measureBody.split(";").filter { it.isNotBlank() }

                for (token in tokens) {
                    if (token.isEmpty()) continue

                    val tupletRegex = "^(\\d+):(\\d+)\\[(.*)]$".toRegex()
                    val tupletMatch = tupletRegex.matchEntire(token)

                    if (tupletMatch != null) {
                        val tupletCount = tupletMatch.groupValues[1].toInt()
                        val tupletValue = tupletMatch.groupValues[2].toInt()
                        val tupletNotesString = tupletMatch.groupValues[3]
                        val parsedTupletNotes = mutableListOf<RhythmAtom>()

                        val innerTokens = tupletNotesString.split(":")

                        for (innerNote in innerTokens) {
                            if(innerNote.isEmpty()) continue

                            val isRest = innerNote.startsWith("!")
                            val noteChar = MusicFont.Notation.convert(innerNote[if (isRest) 1 else 0], isRest)
                            val length = MusicFont.Notation.toLength(noteChar)
                            val dots = if(innerNote.endsWith(",")) 2 else if(innerNote.endsWith(".")) 1 else 0

                            if(isRest) {
                                parsedTupletNotes.add(
                                    RhythmRest(
                                        baseDuration = length,
                                        dots = dots
                                    )
                                )
                            } else {
                                val stemDirection = if(innerNote[0].isLowerCase()) StemDirection.DOWN else StemDirection.UP

                                parsedTupletNotes.add(
                                    RhythmNote(
                                        stemDirection = stemDirection,
                                        baseDuration = length,
                                        dots = dots,
                                        tupletRatio = tupletValue to tupletCount
                                    )
                                )
                            }
                        }

                        elements.add(RhythmTuplet(tupletCount to tupletValue, parsedTupletNotes))
                        continue
                    }

                    val isRest = token.startsWith("!")
                    val isInverted = token[if (isRest) 1 else 0].isLowerCase()
                    val noteChar = MusicFont.Notation.convert(token[if (isRest) 1 else 0], isRest)
                    val length = MusicFont.Notation.toLength(noteChar)
                    val dots = if(token.endsWith(",")) 2 else if(token.endsWith(".")) 1 else 0

                    if(isRest) {
                        elements.add(
                            RhythmRest(
                                baseDuration = length,
                                dots = dots
                            )
                        )
                    } else {
                        val stemDirection = if(isInverted) StemDirection.DOWN else StemDirection.UP

                        elements.add(
                            RhythmNote(
                                stemDirection = stemDirection,
                                baseDuration = length,
                                dots = dots
                            )
                        )
                    }
                }

                measures.add(Measure(timeSig, elements))
            }

            return Rhythm(measures)
        }
    }
    fun serialize(): String {
        val builder = StringBuilder()

        for ((index, measure) in measures.withIndex()) {
            builder.append("{${measure.timeSig.first}/${measure.timeSig.second}}")

            for (element in measure.elements) {
                when (element) {
                    is RhythmAtom -> {
                        val symbol = MusicFont.Notation.toLetter(element.getDisplay().first())
                        val dot = when(element.dots) {
                            1 -> "."
                            2 -> ","
                            else -> ""
                        }
                        when(element) {
                            is RhythmNote -> builder.append("$symbol$dot;")
                            is RhythmRest -> builder.append("!$symbol$dot;")
                        }
                    }

                    is RhythmTuplet -> {
                        val content = element.notes.joinToString(":") { note ->
                            val symbol = MusicFont.Notation.toLetter(note.getDisplay().first())
                            val dot = when(note.dots) {
                                1 -> "."
                                2 -> ","
                                else -> ""
                            }
                            when (note) {
                                is RhythmNote -> "$symbol$dot;"
                                is RhythmRest -> "!$symbol$dot;"
                            }
                        }
                        builder.append("${element.ratio.first}:${element.ratio.second}[$content];")
                    }
                }
            }

            if (index < measures.lastIndex) {
                builder.append("|")
            }
        }
        return builder.toString()
    }

}