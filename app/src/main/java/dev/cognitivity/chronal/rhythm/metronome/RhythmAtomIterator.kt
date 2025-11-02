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

import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmAtom
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmTuplet

class RhythmAtomIterator(private val rhythm: Rhythm) : Iterator<IndexedValue<RhythmAtom>> {
    private var measureIndex = 0
    private var elementIndex = 0
    private var tupletNoteIndex = 0
    private var globalIndex = 0
    private var nextValue: IndexedValue<RhythmAtom>? = null

    init { advance() }

    private fun advance() {
        nextValue = null
        while (measureIndex < rhythm.measures.size) {
            val measure = rhythm.measures[measureIndex]
            if (elementIndex >= measure.elements.size) {
                measureIndex++
                elementIndex = 0
                tupletNoteIndex = 0
                continue
            }

            val element = measure.elements[elementIndex]
            when (element) {
                is RhythmAtom -> {
                    nextValue = IndexedValue(globalIndex, element)
                    globalIndex++
                    elementIndex++
                    return
                }
                is RhythmTuplet -> {
                    if (tupletNoteIndex >= element.notes.size) {
                        tupletNoteIndex = 0
                        elementIndex++
                        continue
                    }
                    val atom = element.notes[tupletNoteIndex]
                    nextValue = IndexedValue(globalIndex, atom)
                    globalIndex++
                    tupletNoteIndex++
                    if (tupletNoteIndex >= element.notes.size) {
                        tupletNoteIndex = 0
                        elementIndex++
                    }
                    return
                }
            }
        }
    }

    override fun hasNext(): Boolean = nextValue != null

    override fun next(): IndexedValue<RhythmAtom> {
        val result = nextValue ?: throw NoSuchElementException()
        advance()
        return result
    }
}

fun Rhythm.atomIterator(): Iterator<IndexedValue<RhythmAtom>> = RhythmAtomIterator(this)

fun Rhythm.atoms(): Sequence<IndexedValue<RhythmAtom>> = Sequence { atomIterator() }