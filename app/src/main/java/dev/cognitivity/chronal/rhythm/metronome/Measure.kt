package dev.cognitivity.chronal.rhythm.metronome

import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmElement

data class Measure(
    val timeSig: Pair<Int, Int>,
    val elements: List<RhythmElement>
)
