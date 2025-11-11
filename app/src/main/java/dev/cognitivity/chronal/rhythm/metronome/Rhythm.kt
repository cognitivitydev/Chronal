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

import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmAtom
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmElement
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmNote
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmRest
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmTuplet
import dev.cognitivity.chronal.rhythm.metronome.elements.StemDirection
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

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
                                        tupletRatio = tupletCount to tupletValue
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
                                is RhythmNote -> "$symbol$dot"
                                is RhythmRest -> "!$symbol$dot"
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

    fun getNoteAt(index: Int): RhythmAtom? {
        return this.atoms().firstOrNull { it.index == index }?.value
    }

    private fun fillRests(
        initialRemaining: Double,
        tupletRatio: Pair<Int, Int>? = null,
        scale: Double = 1.0,
        maxBaseValue: Int = 1024,
        eps: Double = 1e-6
    ): List<RhythmRest> {
        var remaining = initialRemaining
        var restValue = 1
        while (1.0 / restValue > remaining + eps) {
            restValue *= 2
        }
        val newRests = arrayListOf<RhythmRest>()

        while (restValue <= maxBaseValue && remaining > eps) {
            val duration = 1.0 / restValue
            val contribution = duration * scale
            if (contribution <= remaining + eps) {
                newRests.add(
                    RhythmRest(
                        baseDuration = duration,
                        tupletRatio = tupletRatio,
                        dots = 0
                    )
                )
                remaining -= contribution
            } else {
                restValue *= 2
            }
        }

        return newRests.reversed()
    }

    fun replaceNote(noteIndex: Int, newElement: RhythmElement, isScaled: Boolean): Rhythm {
        val targetAtom = getNoteAt(noteIndex) ?: return this

        var foundMeasureIndex = -1
        var foundElementIndex = -1
        var foundTupletInnerIndex: Int? = null

        loop@ for ((mIndex, measure) in measures.withIndex()) {
            for ((eIndex, element) in measure.elements.withIndex()) {
                when (element) {
                    is RhythmTuplet -> {
                        for ((nIndex, note) in element.notes.withIndex()) {
                            if (note === targetAtom) {
                                foundMeasureIndex = mIndex
                                foundElementIndex = eIndex
                                foundTupletInnerIndex = nIndex
                                break@loop
                            }
                        }
                    }
                    is RhythmAtom -> {
                        if (element === targetAtom) {
                            foundMeasureIndex = mIndex
                            foundElementIndex = eIndex
                            break@loop
                        }
                    }
                }
            }
        }

        if (foundMeasureIndex == -1 || foundElementIndex == -1) return this

        val measure = measures[foundMeasureIndex]
        val timeSig = measure.timeSig

        var valueDuration = when (newElement) {
            is RhythmAtom -> newElement.getDuration()
            is RhythmTuplet -> newElement.getDuration()
        }

        val newElements = mutableListOf<RhythmElement>()

        // copy elements before the target element
        for (i in 0 until foundElementIndex) {
            newElements.add(measure.elements[i])
        }

        val targetElement = measure.elements[foundElementIndex]

        if (targetElement is RhythmAtom && foundTupletInnerIndex == null) {
            val element = targetElement
            newElements.add(newElement)

            if (element.getDuration() > valueDuration) { // add rests
                val remaining = element.getDuration() - valueDuration
                val newRests = fillRests(remaining)
                newElements.addAll(newRests)

                // add rest of measure
                for (extraElement in measure.elements.subList(foundElementIndex + 1, measure.elements.size)) {
                    newElements.add(extraElement)
                }

                val newMeasure = Measure(timeSig = timeSig, elements = newElements)
                val newMeasures = measures.toMutableList().apply {
                    this[foundMeasureIndex] = newMeasure
                }
                return Rhythm(newMeasures)
            } else if (element.getDuration() < valueDuration) { // remove extra notes
                var remainingDuration = valueDuration
                var offset = 0
                for (extraElement in measure.elements.subList(foundElementIndex, measure.elements.size)) {
                    when (extraElement) {
                        is RhythmAtom -> {
                            if (remainingDuration <= 0) break else {
                                remainingDuration -= extraElement.getDuration()
                                offset++
                            }
                        }
                        is RhythmTuplet -> {
                            if (remainingDuration <= 0) break else {
                                remainingDuration -= extraElement.getDuration()
                                offset++
                            }
                        }
                    }
                }
                if (remainingDuration <= 0) {
                    remainingDuration *= -1
                    val newRests = fillRests(remainingDuration)
                    newElements.addAll(newRests)
                } else {
                    return this
                }

                // add rest of measure
                for (extraElement in measure.elements.subList(foundElementIndex + offset, measure.elements.size)) {
                    newElements.add(extraElement)
                }

                val newMeasure = Measure(timeSig = timeSig, elements = newElements)
                val newMeasures = measures.toMutableList().apply { this[foundMeasureIndex] = newMeasure }
                return Rhythm(newMeasures)
            } else { // same note duration
                for (extraElement in measure.elements.subList(foundElementIndex + 1, measure.elements.size)) {
                    newElements.add(extraElement)
                }

                val newMeasure = Measure(timeSig = timeSig, elements = newElements)
                val newMeasures = measures.toMutableList().apply { this[foundMeasureIndex] = newMeasure }
                return Rhythm(newMeasures)
            }
        }

        if (targetElement is RhythmTuplet && foundTupletInnerIndex != null) {
            val element = targetElement
            val scale = element.ratio.second.toDouble() / element.ratio.first
            var isFound = false
            val newTupletElements = mutableListOf<RhythmAtom>()

            for ((tupleIndex, tuple) in element.notes.withIndex()) {
                if (tupleIndex == foundTupletInnerIndex) {
                    isFound = true
                    if (newElement is RhythmAtom) {
                        if (isScaled) {
                            newTupletElements.add(newElement)
                        } else {
                            valueDuration *= scale
                            when (newElement) {
                                is RhythmNote -> newTupletElements.add(newElement.copy(
                                    baseDuration = newElement.baseDuration,
                                    tupletRatio = element.ratio
                                ))
                                is RhythmRest -> newTupletElements.add(newElement.copy(
                                    baseDuration = newElement.baseDuration,
                                    tupletRatio = element.ratio
                                ))
                            }
                        }

                        if (tuple.getDuration() > valueDuration) { // add rests
                            val remaining = (tuple.getDuration() - valueDuration) / scale
                            val newRests = fillRests(remaining, element.ratio)
                            newTupletElements.addAll(newRests)

                            // add rest of tuplet
                            for (extraElement in element.notes.subList(tupleIndex + 1, element.notes.size)) {
                                newTupletElements.add(extraElement)
                            }
                            break
                        } else if (tuple.getDuration() < valueDuration) { // remove extra notes
                            var remainingDuration = valueDuration
                            var offset = 0
                            for (extraElement in element.notes.subList(tupleIndex, element.notes.size)) {
                                if (remainingDuration <= 0) { // keep
                                    break
                                } else { // remove
                                    remainingDuration -= extraElement.getDuration()
                                    offset++
                                }
                            }
                            if (remainingDuration <= 1e-10) {
                                remainingDuration *= -1
                                val newRests = fillRests(remainingDuration, element.ratio, scale)
                                newTupletElements.addAll(newRests)
                            } else {
                                return this
                            }

                            for (extraElement in element.notes.subList(tupleIndex + offset, element.notes.size)) {
                                newTupletElements.add(extraElement)
                            }
                            break
                        } else { // same note duration
                            for (extraElement in element.notes.subList(tupleIndex + 1, element.notes.size)) {
                                newTupletElements.add(extraElement)
                            }
                            break
                        }
                    }
                } else {
                    newTupletElements.add(tuple)
                }
            }

            if (isFound) {
                if (newElement is RhythmTuplet) {
                    newElements.add(newElement)
                } else {
                    newElements.add(RhythmTuplet(ratio = element.ratio, notes = newTupletElements.toList()))
                }

                // add rest of measure
                for (extraElement in measure.elements.subList(foundElementIndex + 1, measure.elements.size)) {
                    newElements.add(extraElement)
                }

                val newMeasure = Measure(timeSig = timeSig, elements = newElements)
                val newMeasures = measures.toMutableList().apply {
                    this[foundMeasureIndex] = newMeasure
                }
                return Rhythm(newMeasures)
            } else {
                return this
            }
        }

        return this
    }

    fun setTimeSignature(measureIndex: Int, new: Pair<Int, Int>): Rhythm {
        val measure = measures[measureIndex]
        val newElements = mutableListOf<RhythmElement>()
        val oldDuration = measure.timeSig.first / measure.timeSig.second.toDouble()
        val newDuration = new.first / new.second.toDouble()
        if (newDuration > oldDuration) { // extend measure
            newElements.addAll(measure.elements)
            val remaining = newDuration - oldDuration

            val newRests = fillRests(remaining)
            newElements.addAll(newRests)
        } else if (newDuration < oldDuration) { // shorten measure
            var remaining = newDuration
            for (element in measure.elements) {
                when (element) {
                    is RhythmAtom -> {
                        if (remaining - element.getDuration() >= 0) { // keep
                            newElements.add(element)
                            remaining -= element.getDuration()
                        } else { // remove
                            break
                        }
                    }
                    is RhythmTuplet -> {
                        if (remaining - element.getDuration() >= 0) { // keep
                            newElements.add(element)
                            remaining -= element.getDuration()
                        } else { // remove
                            break
                        }
                    }
                }
            }

            if (remaining >= 0) { // add rests to fill measure
                val newRests = fillRests(remaining)
                newElements.addAll(newRests)
            } else {
                return this
            }
        } else { // same length
            newElements.addAll(measure.elements)
        }

        val newMeasure = Measure(
            timeSig = new,
            elements = newElements
        )
        val newMeasures = measures.toMutableList().apply {
            this[measureIndex] = newMeasure
        }
        return Rhythm(newMeasures)
    }


    private fun getTupletInfo(baseNote: Double, tupleCount: Int): Pair<Int, Double> {
        // find base numerator
        var scaled = baseNote
        var power = 0
        while (abs(scaled - scaled.roundToInt()) > 1e-9 && power < 40) {
            scaled *= 2.0
            power++
        }
        val numerator = scaled.roundToInt()

        // calc how many times numerator fits into tupleCount
        val ratio = tupleCount.toDouble() / numerator.toDouble()
        val powerSteps = if (ratio >= 1) floor(log2(ratio)) else 0.0
        val denominator = (numerator * 2.0.pow(powerSteps)).roundToInt()

        val noteValue = baseNote / denominator.toDouble()

        return denominator to noteValue
    }

    fun createTupletAt(index: Int, numerator: Int): RhythmTuplet? {
        val note = getNoteAt(index) ?: return null

        val (denominator, noteValue) = getTupletInfo(note.getDuration(), numerator)

        val tupleElement = RhythmNote(
            stemDirection = if (note is RhythmNote) note.stemDirection else StemDirection.UP,
            baseDuration = noteValue,
            tupletRatio = numerator to denominator,
            dots = 0
        )
        return RhythmTuplet(
            ratio = numerator to denominator,
            notes = arrayListOf<RhythmNote>().apply {
                repeat(numerator) {
                    add(tupleElement)
                }
            }
        )
    }
}