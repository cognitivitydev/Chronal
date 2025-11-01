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
        var globalIndex = 0
        for (measure in measures) {
            for (element in measure.elements) {
                when (element) {
                    is RhythmAtom -> {
                        if (globalIndex == index) return element
                        globalIndex++
                    }
                    is RhythmTuplet -> {
                        for (note in element.notes) {
                            if (globalIndex == index) return note
                            globalIndex++
                        }
                    }
                }
            }
        }
        return null
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
        var valueDuration = when (newElement) {
            is RhythmAtom -> newElement.getDuration()
            is RhythmTuplet -> newElement.getDuration()
        }

        var newMeasure: Measure? = null
        var newMeasureIndex = -1

        var globalIndex = 0
        for ((measureIndex, measure) in measures.withIndex()) {
            if (newMeasure != null) break

            val timeSig = measure.timeSig
            var currentBeat = 0.0

            val newElements = mutableListOf<RhythmElement>()

            for ((index, element) in measure.elements.withIndex()) {
                when (element) {
                    is RhythmAtom -> {
                        if (globalIndex == noteIndex) {
                            newElements.add(newElement)

                            if (element.getDuration() > valueDuration) { // add rests
                                val remaining = element.getDuration() - valueDuration

                                val newRests = fillRests(remaining)
                                newElements.addAll(newRests)

                                // add rest of measure
                                for (extraElement in measure.elements.subList(index + 1, measure.elements.size)) {
                                    newElements.add(extraElement)
                                }

                                newMeasure = Measure(
                                    timeSig = timeSig,
                                    elements = newElements
                                )
                                newMeasureIndex = measureIndex
                                break
                            } else if (element.getDuration() < valueDuration) { // remove extra notes
                                var remainingDuration = valueDuration
                                var offset = 0
                                for (extraElement in measure.elements.subList(index, measure.elements.size)) {
                                    when (extraElement) {
                                        is RhythmAtom -> {
                                            if (remainingDuration <= 0) { // keep
                                                break
                                            } else { // remove
                                                remainingDuration -= extraElement.getDuration()
                                                offset++
                                            }
                                        }
                                        is RhythmTuplet -> {
                                            if (remainingDuration <= 0) { // keep
                                                break
                                            } else { // remove
                                                remainingDuration -= extraElement.getDuration()
                                                offset++
                                            }
                                        }
                                    }
                                }
                                if (remainingDuration <= 0) { // add rests to fill measure
                                    remainingDuration *= -1

                                    val newRests = fillRests(remainingDuration)
                                    newElements.addAll(newRests)

                                } else {
                                    return this
                                }

                                // add rest of measure
                                for (extraElement in measure.elements.subList(index + offset, measure.elements.size)) {
                                    newElements.add(extraElement)
                                }

                                newMeasure = Measure(
                                    timeSig = timeSig,
                                    elements = newElements
                                )
                                newMeasureIndex = measureIndex
                                break
                            } else { // same note duration
                                for (extraElement in measure.elements.subList(index + 1, measure.elements.size)) {
                                    newElements.add(extraElement)
                                }

                                newMeasure = Measure(
                                    timeSig = timeSig,
                                    elements = newElements
                                )
                                newMeasureIndex = measureIndex
                                break
                            }
                        } else {
                            newElements.add(element)
                        }
                        currentBeat += element.getDuration()
                        globalIndex++
                    }

                    is RhythmTuplet -> {
                        val scale = element.ratio.second.toDouble() / element.ratio.first
                        var isFound = false
                        val newTupletElements = mutableListOf<RhythmAtom>()
                        var currentTupleBeat = 0.0
                        for ((tupleIndex, tuple) in element.notes.withIndex()) {
                            if (globalIndex == noteIndex) {
                                isFound = true
                                if (newElement is RhythmAtom) {
                                    if (isScaled) {
                                        newTupletElements.add(newElement)
                                    } else {
                                        valueDuration *= scale
                                        if (newElement is RhythmNote) {
                                            newTupletElements.add(newElement.copy(
                                                baseDuration = newElement.baseDuration,
                                                tupletRatio = element.ratio
                                            ))
                                        } else if (newElement is RhythmRest) {
                                            newTupletElements.add(newElement.copy(
                                                baseDuration = newElement.baseDuration,
                                                tupletRatio = element.ratio
                                            ))
                                        }
                                    }

                                    if (tuple.getDuration() > valueDuration) { // add rests
                                        val remaining = (tuple.getDuration() - valueDuration) / scale

                                        val newRests = fillRests(remaining, element.ratio)
                                        newTupletElements.addAll(newRests)

                                        // add rest of measure
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
                            currentTupleBeat += tuple.getDuration()
                            currentBeat += tuple.getDuration()
                            globalIndex++
                        }

                        if (isFound) {

                            if (newElement is RhythmTuplet) {
                                newElements.add(newElement)
                            } else {
                                newElements.add(
                                    RhythmTuplet(
                                        ratio = element.ratio,
                                        notes = newTupletElements.toList()
                                    )
                                )
                            }

                            // add rest of measure
                            for (extraElement in measure.elements.subList(index + 1, measure.elements.size)) {
                                newElements.add(extraElement)
                            }

                            newMeasure = Measure(
                                timeSig = timeSig,
                                elements = newElements
                            )
                            newMeasureIndex = measureIndex
                            break
                        } else {
                            newElements.add(
                                RhythmTuplet(
                                    ratio = element.ratio,
                                    notes = element.notes
                                )
                            )
                        }
                    }
                }
            }
        }
        if (newMeasure == null) return this

        val newRhythm = Rhythm(
            measures.mapIndexed { index, measure ->
                if (index == newMeasureIndex) {
                    newMeasure
                } else {
                    measure
                }
            }
        )

        return newRhythm
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

    fun createTupletAt(index: Int, defaultRatio: Pair<Int, Int>? = null): RhythmTuplet? {
        var globalIndex = 0
        var foundElement: RhythmElement? = null
        for (measure in measures) {
            for (element in measure.elements) {
                if (element is RhythmNote) {
                    if (globalIndex == index) {
                        foundElement = element
                        break
                    }
                    globalIndex++
                } else if (element is RhythmTuplet) {
                    for (note in element.notes) {
                        if (globalIndex == index) {
                            foundElement = element
                            break
                        }
                        globalIndex++
                    }
                }
                if (foundElement != null) break
            }
            if (foundElement != null) break
        }
        if (foundElement == null) {
            Log.e("Rhythm", "No note found at index $index")
            return null
        }
        val elementDuration = when (foundElement) {
            is RhythmTuplet -> foundElement.getDuration()
            is RhythmAtom -> foundElement.getDuration()
        }
        val dottedModifier = if (foundElement is RhythmNote) {
            1 + (1..foundElement.dots).sumOf { 1.0 / (2.0.pow(it)) }
        } else {
            1.0
        }

        val ratio = defaultRatio ?: if (foundElement is RhythmTuplet) {
            foundElement.ratio
        } else {
            3 to 2
        }

        // convert duration to note
        var value = 1
        while (value < 1024) {
            val noteDuration = 1.0 / value
            if (noteDuration * dottedModifier * ratio.second.toDouble() <= elementDuration) {
                break
            }
            value *= 2
        }

        val tupleElement = RhythmNote(
            stemDirection = if (foundElement is RhythmNote) foundElement.stemDirection else StemDirection.UP,
            baseDuration = 1.0 / value,
            tupletRatio = ratio,
            dots = 0
        )

        return RhythmTuplet(
            ratio = ratio,
            notes = arrayListOf<RhythmNote>().apply {
                repeat(ratio.first) {
                    add(tupleElement)
                }
            }
        )
    }
}