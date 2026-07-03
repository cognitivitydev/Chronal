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

package dev.cognitivity.chronal.metronome

import dev.cognitivity.chronal.settings.types.json.MetronomeSequence
import dev.cognitivity.chronal.settings.types.json.SequenceStep
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class SequencePosition(
    val stepIndex: Int,
    val trackIndex: Int,
    val bar: Int,
    val totalBars: Int,
)

class MetronomeSequencer {
    var sequence: MetronomeSequence = MetronomeSequence()
        private set

    var stepIndex: Int = -1
    var barsStarted: Int = 0

    private val _positionEvents = MutableSharedFlow<SequencePosition?>(replay = 1)
    val positionEvents: SharedFlow<SequencePosition?> = _positionEvents.asSharedFlow()

    fun setSequence(newSequence: MetronomeSequence) {
        sequence = newSequence
        reset()
    }

    fun isActive(trackCount: Int): Boolean {
        return sequence.enabled && sequence.validSteps(trackCount).isNotEmpty()
    }

    fun reset() {
        stepIndex = -1
        barsStarted = 0
        _positionEvents.tryEmit(null)
    }

    fun currentStep(): SequenceStep? = sequence.steps.getOrNull(stepIndex)

    fun advanceToNextValidStep(trackCount: Int): SequenceStep {
        val steps = sequence.steps
        for (offset in 1..steps.size) {
            val index = (stepIndex + offset).mod(steps.size)
            val step = steps[index]
            if (step.trackIndex in 0 until trackCount && step.bars > 0) {
                stepIndex = index
                barsStarted = 0
                return step
            }
        }
        throw IllegalStateException("No valid sequence step found")
    }

    fun onBarStart(trackIndex: Int) {
        barsStarted++
        val step = currentStep() ?: return
        _positionEvents.tryEmit(
            SequencePosition(
                stepIndex = stepIndex,
                trackIndex = trackIndex,
                bar = barsStarted,
                totalBars = step.bars,
            )
        )
    }
}
