package dev.cognitivity.chronal.rhythm.metronome.elements

data class RhythmTuplet(
    val ratio: Pair<Int, Int>,
    val notes: List<RhythmNote>
) : RhythmElement()