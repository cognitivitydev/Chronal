package dev.cognitivity.chronal.activity.editor

import dev.cognitivity.chronal.MusicFont
import kotlin.math.pow

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
                        val parsedTupletNotes = mutableListOf<RhythmNote>()

                        val innerTokens = tupletNotesString.split(":")

                        for (innerNote in innerTokens) {
                            if(innerNote.isEmpty()) continue

                            val isRest = innerNote.startsWith("!")
                            val isInverted = innerNote[if (isRest) 1 else 0].isLowerCase()
                            val noteChar = MusicFont.Notation.convert(innerNote[if (isRest) 1 else 0], isRest)
                            val length = MusicFont.Notation.toLength(noteChar)
                            val dots = if(innerNote.endsWith(",")) 2 else if(innerNote.endsWith(".")) 1 else 0
                            val dotModifier = 1 + (1..dots).sumOf { 1.0 / (2.0.pow(it)) }
                            val dottedString = (" " + MusicFont.Notation.DOT.char).repeat(dots)

                            val display = noteChar.toString() + dottedString

                            parsedTupletNotes.add(
                                RhythmNote(
                                    display = display,
                                    isRest = isRest,
                                    isInverted = isInverted,
                                    duration = length * dotModifier * (tupletValue.toDouble() / tupletCount),
                                    dots = dots
                                )
                            )
                        }

                        elements.add(RhythmTuplet(tupletCount to tupletValue, parsedTupletNotes))
                        continue
                    }

                    val isRest = token.startsWith("!")
                    val isInverted = token[if (isRest) 1 else 0].isLowerCase()
                    val noteChar = MusicFont.Notation.convert(token[if (isRest) 1 else 0], isRest)
                    val length = MusicFont.Notation.toLength(noteChar)
                    val dots = if(token.endsWith(",")) 2 else if(token.endsWith(".")) 1 else 0
                    val dotModifier = 1 + (1..dots).sumOf { 1.0 / (2.0.pow(it)) }
                    val dottedString = (" " + MusicFont.Notation.DOT.char).repeat(dots)

                    val display = noteChar.toString() + dottedString

                    elements.add(
                        RhythmNote(
                            display = display,
                            isRest = isRest,
                            isInverted = isInverted,
                            duration = length * dotModifier,
                            dots = dots
                        )
                    )
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
                    is RhythmNote -> {
                        val symbol = MusicFont.Notation.toLetter(element.display.first())
                        val dot = when(element.dots) {
                            1 -> "."
                            2 -> ","
                            else -> ""
                        }
                        builder.append(if (element.isRest) ("!$symbol$dot;") else "$symbol$dot;")
                    }

                    is RhythmTuplet -> {
                        val content = element.notes.joinToString(":") { note ->
                            val symbol = MusicFont.Notation.toLetter(note.display.first())
                            val dot = when(note.dots) {
                                1 -> "."
                                2 -> ","
                                else -> ""
                            }
                            if (note.isRest) "!$symbol$dot" else "$symbol$dot"
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

data class Measure(
    val timeSig: Pair<Int, Int>,
    val elements: List<RhythmElement>
)

sealed class RhythmElement

data class RhythmNote(
    val display: String,
    val isRest: Boolean,
    val isInverted: Boolean,
    val duration: Double,
    val dots: Int
) : RhythmElement()

data class RhythmTuplet(
    val ratio: Pair<Int, Int>,
    val notes: List<RhythmNote>
) : RhythmElement()

data class Beat(
    val duration: Double,
    val isHigh: Boolean,
    val measure: Int,
    val index: Int
)